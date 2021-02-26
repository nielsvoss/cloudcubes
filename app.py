#!/usr/bin/env python3

from aws_cdk import core

from cloudcubes.cloudcubes_stack import CloudcubesStack


app = core.App()
CloudcubesStack(app, "cloudcubes")

app.synth()
