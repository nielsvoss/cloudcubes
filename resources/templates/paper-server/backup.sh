#!/bin/bash
data_bucket_name=$(cat /home/ec2-user/info/DATA_BUCKET_NAME)

if [ "$1" = "shifting" ]; then
  aws s3 rm s3://$data_bucket_name/backups/backup-5.zip
  aws s3 mv s3://$data_bucket_name/backups/backup-4.zip s3://$data_bucket_name/backups/backup-5.zip
  aws s3 mv s3://$data_bucket_name/backups/backup-3.zip s3://$data_bucket_name/backups/backup-4.zip
  aws s3 mv s3://$data_bucket_name/backups/backup-2.zip s3://$data_bucket_name/backups/backup-3.zip
  aws s3 mv s3://$data_bucket_name/backups/backup-1.zip s3://$data_bucket_name/backups/backup-2.zip
  aws s3 mv s3://$data_bucket_name/backups/latest-backup.zip s3://$data_bucket_name/backups/backup-1.zip
  name="latest-backup"
else
  name=$(date +%Y.%m.%d.%H.%M.%S)
fi

cd /home/ec2-user/server/minecraft
sudo zip -r /home/ec2-user/backup.zip \
  world world_nether world_the_end logs \
  server.properties bukkit.yml spigot.yml paper.yml help.yml eula.txt \
  permissions.yml ops.json banned-players.json banned-ips.json \
  whitelist.json usercache.json version_history.json start.sh
aws s3 cp /home/ec2-user/backup.zip s3://$data_bucket_name/backups/$name.zip --acl public-read
sudo rm /home/ec2-user/backup.zip

# Uncomment to send message to webhook when backup is created
# webhook_url=https://example.com/webhookurl
# username=Backups
# message="Backup with name $name created.\nhttps://$data_bucket_name.s3.amazonaws.com/backups/$name.zip"
# curl -X POST -H "Content-Type: application/json" -d "{\"username\": \"$username\", \"content\":\"$message\"}" $webhook_url
