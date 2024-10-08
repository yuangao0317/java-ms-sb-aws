#!/bin/bash

# Check if .env file exists
if [ ! -f .env ]; then
  echo ".env file not found!"
  exit 1
fi

# Load the environment variables from .env file
export $(grep -v '^#' .env | xargs)

# Run Docker with AWS CLI
docker run --rm -it --env-file .env \
  --hostname="$HOSTNAME" \
  --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
  --network=bridge \
  --workdir=/aws \
  --restart=no \
  --runtime=runc amazon/aws-cli:"$AWS_CLI_IMAGE_TAG" "$@"
