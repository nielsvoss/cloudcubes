def lambda_handler(app, context):
    return {
        "statusCode": 200,
        "body": {
            "message": "Hello World!"
        }
    }
