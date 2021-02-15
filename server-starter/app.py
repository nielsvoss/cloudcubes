import json
import boto3
import os

def lambda_handler(event, context):
    id = event['Id']
    state = event['Server_State']

    # Make sure server is offline
    assert state in ['SERVER_SHUTDOWN_FUNCTION_CALLED', 'SERVER_STOPPING', 'SERVER_OFFLINE', ''], f"Server state {state} is not offline"

    table = boto3.resource('dynamodb').Table(os.environ['DATABASE_NAME'])

    # Get server data
    data = table.get_item(
        Key={
            'Id': id
        },
        ProjectionExpression='EBS, EC2Instance, EC2SpotRequest',
        ConsistentRead=True
    )['Item']

    table.update_item(
        Key={
            'Id': id
        },
        UpdateExpression='set Server_State=:u',
        ExpressionAttributeValues={
            ':u': 'SERVER_STARTING'
        }
    )

    return {
        "statusCode": 200,
        "body": "hello world"
    }
