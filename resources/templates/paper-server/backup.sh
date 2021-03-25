#!/bin/bash
data_bucket_name=$(cat /home/ec2-user/info/DATA_BUCKET_NAME)
name=$(date +%Y.%m.%d.%H.%M.%S)

cd /home/ec2-user
sudo zip -r backup.zip server/minecraft
aws s3 cp backup.zip s3://$data_bucket_name/backups/$name.zip
sudo rm backup.zip
