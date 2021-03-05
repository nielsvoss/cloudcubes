import boto3
import os

ec2_id = os.environ['EC2_ID']
ec2_zone = os.environ['EC2_ZONE']
id = int(os.environ['SERVER_ID'])
database_name = os.environ['DATABASE_NAME']

table = boto3.resource('dynamodb', region_name=ec2_zone[:-1]).Table(database_name)

table.update_item(
        Key={
            'Id': id
        },
        UpdateExpression='set Server_State=:u, EC2InstanceId=:v',
        ExpressionAttributeValues={
            ':u': 'SERVER_STARTED',
            ':v': ec2_id
        }
    )
