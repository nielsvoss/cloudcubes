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
            versioned=False,
            removal_policy=cdk.RemovalPolicy.DESTROY
        )

        scripts_bucket_deployment = s3_deployment.BucketDeployment(self, "ScriptsBucketDeployment",
            destination_bucket=scripts_bucket,
            sources=[s3_deployment.Source.asset('./scripts')]
        )

        # Permissions to read scripts bucket
        scripts_bucket_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=[scripts_bucket.bucket_arn, f'{scripts_bucket.bucket_arn}/*'],
            actions=[
                's3:ListBucket',
                's3:GetObject'
            ]
        )

        attach_volumes_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=['*'],
            actions=[
                'ec2:AttachVolume'
            ]
        )

        # Role and InstanceProfile to be assigned to launched EC2 instances
        server_role = iam.Role(self, "Role",
            assumed_by=iam.ServicePrincipal('ec2.amazonaws.com')
        )
        server_role.add_to_policy(database_perms)
        server_role.add_to_policy(scripts_bucket_perms)
        server_role.add_to_policy(attach_volumes_perms)
        server_role.add_managed_policy(iam.ManagedPolicy.from_aws_managed_policy_name('AmazonSSMManagedInstanceCore'))
        server_instance_profile = iam.CfnInstanceProfile(self, "ServerInstanceProfile",
            roles=[server_role.role_name]
        )
        
        # Permissions to start servers
        start_server_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=['*'],
            actions=[
                'ec2:RequestSpotInstances'
            ]
        )
        pass_server_role_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=[server_role.role_arn],
            actions=[
                'iam:GetRole',
                'iam:PassRole'
            ]
        )

        # Lambda function for starting servers
        server_starter_function = lambda_.Function(self, "ServerStarterFunction",
            runtime=lambda_.Runtime.PYTHON_3_8,
            code=lambda_.Code.from_asset('./server-starter'),
            handler="app.lambda_handler",
            environment={
                "DATABASE_NAME": db_name.value_as_string,
                "SCRIPTS_BUCKET": scripts_bucket.bucket_name,
                "SERVER_INSTANCE_PROFILE": server_instance_profile.attr_arn
            }
        )
        server_starter_function.add_to_role_policy(database_perms)
        server_starter_function.add_to_role_policy(start_server_perms)
        server_starter_function.add_to_role_policy(pass_server_role_perms)

        list_ssm_actions_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=['*'],
            actions=[
                'ssm:ListDocuments',
                "ssm:DescribeDocument",
                "ssm:GetDocument",
                "ssm:DescribeInstance*"
            ]
        )
        
        run_command_perms = iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            resources=[
                '*'
            ],
            actions=[
                '*'
            ]
        )

        # Lambda function for stopping servers
        server_stopper_function = lambda_.Function(self, "ServerStopperFunction",
            runtime=lambda_.Runtime.PYTHON_3_8,
            code=lambda_.Code.from_asset('./server-stopper'),
            handler="app.lambda_handler",
            environment={
                "DATABASE_NAME": db_name.value_as_string
            }
        )
        server_stopper_function.add_to_role_policy(database_perms)
        server_stopper_function.add_to_role_policy(list_ssm_actions_perms)
        server_stopper_function.add_to_role_policy(run_command_perms)
