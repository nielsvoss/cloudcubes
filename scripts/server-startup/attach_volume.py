import boto3
import os

ec2_id = os.environ['EC2_ID']
ec2_zone = os.environ['EC2_ZONE']
id = int(os.environ['SERVER_ID'])
database_name = os.environ['DATABASE_NAME']

table = boto3.resource('dynamodb', region_name=ec2_zone[:-1]).Table(database_name)
ec2_client = boto3.client('ec2', region_name=ec2_zone[:-1])

volume_name = table.get_item(
    Key={
        'Id': id
    },
    ProjectionExpression='EBS',
    ConsistentRead=True
)['Item']['EBS']

ec2_client.attach_volume(
    Device='/dev/sdf',
    InstanceId=ec2_id,
    VolumeId=volume_name
)
