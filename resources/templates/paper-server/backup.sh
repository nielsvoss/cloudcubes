#!/bin/bash
name=$(date +%Y.%m.%d.%H.%M.%S)

cd /home/ec2-user
sudo zip -r backup.zip server/minecraft
aws s3 cp backup.zip s3://$1/backups/$name.zip
sudo rm backup.zip
