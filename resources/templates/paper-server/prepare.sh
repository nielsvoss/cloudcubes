#!/bin/bash
# Write data bucket name to file
mkdir /home/ec2-user/info
echo $DATA_BUCKET > /home/ec2-user/info/DATA_BUCKET_NAME
# Add cron job to crontab
(crontab -l 2>/dev/null; echo "0 22 * * * /home/ec2-user/server/backup.sh") | crontab -
(crontab -l 2>/dev/null; echo "0 */4 * * * /home/ec2-user/server/backup.sh shifting") | crontab -
sudo chmod +x /home/ec2-user/server/backup.sh
