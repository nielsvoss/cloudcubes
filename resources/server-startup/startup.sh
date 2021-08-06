#!/bin/bash

# Get instance id
ec2_instance_metadata_command_result=($(ec2-metadata -i))
export EC2_ID=${ec2_instance_metadata_command_result[1]}
ec2_zone_metadata_command_result=($(ec2-metadata -z))
export EC2_ZONE=${ec2_zone_metadata_command_result[1]}
