import json
import boto3
import os
import base64
import re

def lambda_handler(event, context):
    id = event['Id']
    state = event['Server_State']
    database_name = os.environ['DATABASE_NAME']
    assert re.match(r'[a-zA-Z-,._+:@%/]*', database_name)

    # Make sure server is offline
    assert state in ['SERVER_SHUTDOWN_FUNCTION_CALLED', 'SERVER_STOPPING', 'SERVER_OFFLINE', ''], f"Server state {state} is not offline"

    table = boto3.resource('dynamodb').Table(database_name)

    # Get server data
    data = table.get_item(
        Key={
            'Id': id
        },
        ProjectionExpression='EBS, Instance_Type, Key_Name, EC2Instance, EC2SpotRequest',
        ConsistentRead=True
    )['Item']

    # Read user data from the shell script
    user_data: str = f"""
    #!/bin/bash
    export DATABASENAME={database_name}
    export SERVERID={int(id)}
    """
    with open('startup.sh', 'r') as file:
        user_data += file.read()
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
