import aws_cdk.core as cdk
import aws_cdk.aws_s3 as s3
import aws_cdk.aws_lambda as lambda_


class CloudcubesStack(cdk.Stack):

    def __init__(self, scope: cdk.Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        db_name = cdk.CfnParameter(self, "Database_Name",
            type="String",
            description="The name of the dynamodb database where the information on server data is stored."
        )

        # The code that defines your stack goes here
        lambda_.Function(self, "SchedulerFunction",
            runtime=lambda_.Runtime.PYTHON_3_8,
            code=lambda_.Code.from_asset("scheduler"),
            handler="app.lambda_handler",
            environment={
                "DATABASE_NAME": db_name.value_as_string
            }
        )
