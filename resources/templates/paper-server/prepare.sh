#!/bin/bash
# Add cron job to crontab
(crontab -l 2>/dev/null; echo "0 22 * * * /home/ec2-user/server/backup.sh $DATA_BUCKET") | crontab -
