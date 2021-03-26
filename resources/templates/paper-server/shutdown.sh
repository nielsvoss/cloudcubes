#!/bin/bash
cd /home/ec2-user
screen -S minecraft -p 0 -X stuff 'tellraw @a {"text":"Server will shutdown in 1 minute","color":"red"}^M'
sleep 30
screen -S minecraft -p 0 -X stuff 'tellraw @a {"text":"Server will shutdown in 30 seconds","color":"red"}^M'
sleep 20
screen -S minecraft -p 0 -X stuff 'tellraw @a {"text":"Server will shutdown in 10 seconds","color":"red"}^M'
sleep 10
screen -S minecraft -p 0 -X stuff "stop^M"
sleep 30
sh server/backup.sh
sleep 30
sudo shutdown -h now
