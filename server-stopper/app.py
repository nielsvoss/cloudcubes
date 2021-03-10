import boto3
import json
import os

def lambda_handler(event, context):

    id = event['Id']
    database_name = os.environ['DATABASE_NAME']

    table = boto3.resource('dynamodb').Table(database_name)

    # Get server data
    data = table.get_item(
        Key={
            'Id': id
        },
        ProjectionExpression='Server_State, EC2InstanceId',
        ConsistentRead=True
    )['Item']

    assert data['Server_State'] in ['SERVER_STARTED'], f"Server state {data['Server_State']} is not online"

    commands = [
        'sudo shutdown -h +5',
        'sudo sh /home/ec2-user/server/shutdown.sh'
    ]

    ssm_client = boto3.client('ssm')
    response = ssm_client.send_command(
        DocumentName="AWS-RunShellScript",
        Parameters={'commands': commands},
        InstanceIds=[
            data['EC2InstanceId']
        ]
    )

    # Update database
    table.update_item(
        Key={
            'Id': id
        },
        UpdateExpression='set Server_State=:u',
        ExpressionAttributeValues={
            ':u': 'SERVER_OFFLINE'
        }
    )

    return {
        "statusCode": 200,
        "body": {
            "response": json.dumps(response, default=str),
        }
    }
