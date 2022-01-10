#!/bin/bash

# Etisalat Egypt, Open Source
# Copyright 2020, Etisalat Egypt and individual contributors
# by the @authors tag.
#
# This program is free software: you can redistribute it and/or modify
# under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation; either version 3 of
# the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>
#
#
# @author Ayman ElSherif

function run_command() {
  eval $1
  exit_code=$?
  if [ $exit_code -ne 0 ]; then
    echo -e "\n[-] Build failed!\n"
    exit 1
  fi
}

RODAN_HOME=$(cd "$pwd../.."; pwd)
RODAN_SRC_HOME=$RODAN_HOME/src
RODAN_BUILD_HOME=$RODAN_HOME/build
RODAN_CFG_HOME=$RODAN_BUILD_HOME/config
JSS7_STACK_EXTENSIONS_HOME="${RODAN_SRC_HOME}"/connectivity/stack-extensions/jss7
ASN_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/asn-extension
SCCP_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/sccp-extension-impl
TCAP_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/tcap-extension-impl
MAP_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/map-extension-impl
JDIAMETER_STACK_EXTENSIONS_HOME="${RODAN_SRC_HOME}"/connectivity/stack-extensions/jdiameter
JDIAMETER_STACK_EXTENSION_PATH="${JDIAMETER_STACK_EXTENSIONS_HOME}"/jdiameter-extension-impl
LIBRARY_MODULE_PATH="${RODAN_SRC_HOME}"/library
CONNECTIVITY_MODULE_PATH="${RODAN_SRC_HOME}"/connectivity/connectivity
INTRUDER_MODULE_PATH="${RODAN_SRC_HOME}"/intruder
LAB_MODULE_PATH="${RODAN_SRC_HOME}"/lab
DOCKER_HOME="${RODAN_BUILD_HOME}"/docker
MAEVEN_REPO="$HOME"/.m2

run_command "cd ${RODAN_HOME}"
if [[ ! -d $MAEVEN_REPO ]]
  then
    run_command "mkdir -p $MAEVEN_REPO"
fi

cd "${RODAN_HOME}"
# Build jSS7 stack extentions
run_command "docker run -it --rm --name stack-builder --volume $ASN_STACK_EXTENSION_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"
run_command "docker run -it --rm --name stack-builder --volume $SCCP_STACK_EXTENSION_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"
run_command "docker run -it --rm --name stack-builder --volume $TCAP_STACK_EXTENSION_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"
run_command "docker run -it --rm --name stack-builder --volume $MAP_STACK_EXTENSION_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"

# Build jDiameter stack extentions
run_command "docker run -it --rm --name stack-builder --volume $JDIAMETER_STACK_EXTENSION_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"

# Build Library Module
run_command "docker run -it --rm --name rodan-builder --volume $LIBRARY_MODULE_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true"

# Build Connectivity Module
run_command "docker run -it --rm --name rodan-builder --volume $CONNECTIVITY_MODULE_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true"


# Build Intruder Module
run_command "docker run -it --rm --name rodan-builder --volume $INTRUDER_MODULE_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true"

# Build Lab Module
run_command "docker run -it --rm --name rodan-builder --volume $LAB_MODULE_PATH:/usr/src/app --volume $MAEVEN_REPO:/root/.m2 -w /usr/src/app maven:3.8.4-openjdk-17 mvn install -Dmaven.test.skip=true"


run_command "cd ${DOCKER_HOME}"

# Copy config files
run_command "cp ${RODAN_CFG_HOME}/intruder.yml ./"
run_command "cp ${RODAN_CFG_HOME}/stp.yml ./"
run_command "cp ${RODAN_CFG_HOME}/hlr.yml ./"
run_command "cp ${RODAN_CFG_HOME}/msc.yml ./"

# Copy Rodan binary files
run_command "cp ${INTRUDER_MODULE_PATH}/main/cli/target/intruder.jar ./"
run_command "cp ${LAB_MODULE_PATH}/ss7/stp/main/cli/target/stp.jar ./"
run_command "cp ${LAB_MODULE_PATH}/ss7/hlr/main/cli/target/hlr.jar ./"
run_command "cp ${LAB_MODULE_PATH}/ss7/msc/main/cli/target/msc.jar ./"

#Build images
run_command "docker build --tag rodanframework/intruder:1.2.2 -f Dockerfile ."
run_command "docker build --tag rodanframework/stp:1.2.2 -f Dockerfile-STP ."
run_command "docker build --tag rodanframework/hlr:1.2.2 -f Dockerfile-HLR ."
run_command "docker build --tag rodanframework/msc:1.2.2 -f Dockerfile-MSC ."

# Remove temp config files
run_command "rm ${DOCKER_HOME}/intruder.yml"
run_command "rm ${DOCKER_HOME}/stp.yml"
run_command "rm ${DOCKER_HOME}/hlr.yml"
run_command "rm ${DOCKER_HOME}/msc.yml"

# Remove Rodan binary files
run_command "rm ${DOCKER_HOME}/intruder.jar"
run_command "rm ${DOCKER_HOME}/stp.jar"
run_command "rm ${DOCKER_HOME}/hlr.jar"
run_command "rm ${DOCKER_HOME}/msc.jar"
