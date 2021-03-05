#!/bin/bash

# Get instance id
ec2_instance_metadata_command_result=($(ec2-metadata -i))
export EC2_ID=${ec2_instance_metadata_command_result[1]}
ec2_zone_metadata_command_result=($(ec2-metadata -z))
export EC2_ZONE=${ec2_zone_metadata_command_result[1]}

# Install python packages
sudo yum -y install python3 python-pip
python3 -m pip install --user -r requirements.txt

# Run update database script
python3 update_db.py

# Install java
sudo amazon-linux-extras enable corretto8
sudo yum -y install java-1.8.0-amazon-corretto
