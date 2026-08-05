#!/bin/sh
set -e

awslocal s3 mb s3://documents || true
awslocal sqs create-queue --queue-name document-ready || true
awslocal sqs create-queue --queue-name embedding-created || true
