#!/usr/bin/env bash

# Copyright (c) 2020 Ayman ElSherif <ayman.elsherif@outlook.com> - All Rights Reserved

RODAN_HOME=$(cd "$pwd../.."; pwd)
RODAN_BUILD_HOME=$RODAN_HOME/build
DOCKER_HOME="${RODAN_BUILD_HOME}"/docker
cd "${DOCKER_HOME}"
docker-compose up
