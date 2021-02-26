import aws_cdk.core as cdk
import aws_cdk.aws_s3 as s3
import aws_cdk.aws_lambda as lambda_
import aws_cdk.aws_iam as iam
import aws_cdk.aws_s3_deployment as s3_deployment

class CloudcubesStack(cdk.Stack):

    def __init__(self, scope: cdk.Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # Name of the DynamoDB database
        db_name = cdk.CfnParameter(self, "Database_Name",
            type="String",
            description="The name of the dynamodb database where the information on server data is stored."
        )

        # Grants access to the dynamodb database
        database_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=[f'arn:aws:dynamodb:*:*:table/{db_name.value_as_string}'],
            actions=[
                'dynamodb:BatchGetItem',
                'dynamodb:BatchWriteItem',
                'dynamodb:ConditionCheckItem',
                'dynamodb:PutItem',
                'dynamodb:DescribeTable',
                'dynamodb:DeleteItem',
                'dynamodb:GetItem',
                'dynamodb:Scan',
                'dynamodb:Query',
                'dynamodb:UpdateItem'
            ]
        )

        # The lambda function in charge of scheduling servers
        scheduler_function = lambda_.Function(self, "SchedulerFunction",
            runtime=lambda_.Runtime.PYTHON_3_8,
            code=lambda_.Code.from_asset("./scheduler"),
            handler="app.lambda_handler",
            environment={
                "DATABASE_NAME": db_name.value_as_string
            }
        )
        scheduler_function.add_to_role_policy(database_perms)

        # S3 bucket to store scripts (used by EC2 instances)
        scripts_bucket = s3.Bucket(self, "ScriptsBucket",
            bucket_name='cloudcubes-scripts-bucket',
            versioned=False,
            removal_policy=cdk.RemovalPolicy.DESTROY
        )

        scripts_bucket_deployment = s3_deployment.BucketDeployment(self, "ScriptsBucketDeployment",
            destination_bucket=scripts_bucket,
            sources=[s3_deployment.Source.asset('./scripts')]
        )
