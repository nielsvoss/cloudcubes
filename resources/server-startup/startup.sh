#!/bin/bash
cd /home/ec2-user || exit

# Get instance id
ec2_instance_metadata_command_result=($(ec2-metadata -i))
export EC2_ID=${ec2_instance_metadata_command_result[1]}
ec2_zone_metadata_command_result=($(ec2-metadata -z))
export EC2_ZONE=${ec2_zone_metadata_command_result[1]}

# Amazon Linux comes with AWS CLI version 1 by default, this will install version 2
# See https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-linux.html
# After installing version 2 can be accessed with /usr/local/bin/aws
mkdir awscliv2
cd awscliv2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
cd ..
rm -rf awscliv2

# Download contents of the server-startup folder
/usr/local/bin/aws s3 cp --recursive s3://"$CLOUDCUBESRESOURCEBUCKETNAME"/server-startup startup

# Update database with ONLINE state
# See https://awscli.amazonaws.com/v2/documentation/api/latest/reference/dynamodb/update-item.html#examples
printf '{"Id":{"N":"%s"}}\n' "$SERVER_ID" > startup/set-state-online-key.json
/usr/local/bin/aws dynamodb update-item \
    --table-name "$CLOUDCUBESSERVERDATABASENAME" \
    --key file://startup/set-state-online-key.json \
    --update-expression "SET #S = :s" \
    --expression-attribute-names file://startup/set-state-online-expression-attribute-names.json \
    --expression-attribute-values file://startup/set-state-online-expression-attribute-values.json \
    --return-values NONE \
    --return-consumed-capcity NONE \
    --return-item-collection-metrics NONE
