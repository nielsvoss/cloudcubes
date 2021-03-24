import json
import boto3
import os
import base64
import re

def lambda_handler(event, context):
    id = event['Id']
    state = event['Server_State']
    database_name = os.environ['DATABASE_NAME']
    resources_bucket = os.environ['RESOURCES_BUCKET']
    data_bucket = os.environ['DATA_BUCKET']
    server_instance_profile = os.environ['SERVER_INSTANCE_PROFILE']
    assert not re.search(r'[^a-zA-Z0-9\-,._+:@%/]', database_name)
    assert not re.search(r'[^a-z0-9\-.]', resources_bucket)
    assert not re.search(r'[^a-z0-9\-.]', data_bucket)

    # Make sure server is offline
    assert state in ['SERVER_OFFLINE', ''], f"Server state {state} is not offline"

    table = boto3.resource('dynamodb').Table(database_name)

    # Get server data
    data = table.get_item(
        Key={
            'Id': id
        },
        ProjectionExpression='Instance_Type, Key_Name',
        ConsistentRead=True
    )['Item']

    # Set user data
    user_data: str = f""" 
    #!/bin/bash
    export DATABASE_NAME={database_name}
    export SERVER_ID={int(id)}
    export RESOURCES_BUCKET={resources_bucket}
    export DATA_BUCKET={data_bucket}

    cd /home/ec2-user
    su -c '
    mkdir server-scripts
    cd server-scripts
    aws s3 cp s3://${{RESOURCES_BUCKET}}/server-startup . --recursive
    chmod +x startup.sh
    ./startup.sh
    ' ec2-user
    """
    encoded_user_data = base64.b64encode(user_data.encode('ascii')).decode('ascii')

    # Request spot instances
    ec2_client = boto3.client('ec2')
    amazon_linux_ami = 'ami-01aab85a5e4a5a0fe'
    response = ec2_client.request_spot_instances(
        InstanceCount=1,
        Type='one-time',
        LaunchSpecification={
            'ImageId': amazon_linux_ami,
            'KeyName': data['Key_Name'],
            'InstanceType': data['Instance_Type'],
            'Placement': {
                'AvailabilityZone': 'us-east-2a'
            },
            'SecurityGroups': ['Minecraft'],
            'IamInstanceProfile': {
                'Arn': server_instance_profile
            },
            'UserData': encoded_user_data
        }
    )

    table.update_item(
        Key={
            'Id': id
        },
        UpdateExpression='set Server_State=:u, EC2SpotRequest=:v',
        ExpressionAttributeValues={
            ':u': 'SERVER_STARTING',
            ':v': response['SpotInstanceRequests'][0]['SpotInstanceRequestId']
        }
    )

    return {
        "statusCode": 200,
        "body": {
            "response": json.dumps(response, default=str),
            "user-data": user_data,
            "encoded-user-data": encoded_user_data
        }
    }
