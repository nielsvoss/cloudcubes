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

cd /home/ec2-user
sudo zip -r backup.zip server/minecraft
aws s3 cp backup.zip s3://$data_bucket_name/backups/$name.zip
sudo rm backup.zip
