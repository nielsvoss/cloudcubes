#!/bin/bash

# Get instance id
ec2_instance_metadata_command_result=($(ec2-metadata -i))
export EC2_ID=${ec2_instance_metadata_command_result[1]}
ec2_zone_metadata_command_result=($(ec2-metadata -z))
export EC2_ZONE=${ec2_zone_metadata_command_result[1]}

# Install systems manager (for remote commands)
sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm

# Install python packages
sudo yum -y install python3 python-pip
python3 -m pip install --user -r requirements.txt

# Run update database script
python3 update_db.py

# Mount volume
python3 attach_volume.py
sleep 10
# Run sudo mkfs.ext4 /dev/sdf before first use
mkdir -m 000 /home/ec2-user/server
sudo mount /dev/sdf /home/ec2-user/server
sudo chmod -R +rwx server

# Install java
sudo amazon-linux-extras enable corretto8
sudo yum -y install java-1.8.0-amazon-corretto

# Run server in mounted volume
cd /home/ec2-user/server
if [ -f 'prepare.sh' ]; then
    chmod +x prepare.sh
    ./prepare.sh
fi
chmod +x start.sh
./start.sh
