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

RODAN_HOME=$(pwd)/../../src
RODAN_SRC_HOME=$(RODAN_HOME)/src
JSS7_STACK_EXTENSIONS_HOME="${RODAN_SRC_HOME}"/connectivity/stack-extensions/jss7
JDIAMETER_STACK_EXTENSIONS_HOME="${RODAN_SRC_HOME}"/connectivity/stack-extensions/jdiameter
ASN_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/asn-extension
SCCP_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/sccp-extension-impl
TCAP_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/tcap-extension-impl
MAP_STACK_EXTENSION_PATH="${JSS7_STACK_EXTENSIONS_HOME}"/map-extension-impl
JDIAMETER_STACK_EXTENSION_PATH="${JDIAMETER_STACK_EXTENSIONS_HOME}"/jdiameter-extension-impl
LIBRARY_MODULE_PATH="${RODAN_SRC_HOME}"/library
CONNECTIVITY_MODULE_PATH="${RODAN_SRC_HOME}"/connectivity/connectivity
INTRUDER_MODULE_PATH="${RODAN_SRC_HOME}"/intruder
LAB_MODULE_PATH="${RODAN_SRC_HOME}"/lab
DOCKER_HOME="${RODAN_HOME}"/docker
MAEVEN_REPO="$HOME"/.m2

run_command "cd ${RODAN_HOME}"
if [[ ! -d $MAEVEN_REPO ]]
  then
    run_command "mkdir -p $MAEVEN_REPO"
fi


# Build jSS7 stack extentions
run_command "cd ${ASN_STACK_EXTENSION_PATH}"
run_command "mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"
run_command "cd ${SCCP_STACK_EXTENSION_PATH}"
run_command "mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"
run_command "cd ${TCAP_STACK_EXTENSION_PATH}"
run_command "mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"
run_command "cd ${MAP_STACK_EXTENSION_PATH}"
run_command "mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"

# Build jDiameter stack extentions
run_command "cd ${JDIAMETER_STACK_EXTENSION_PATH}"
run_command "mvn install -Dmaven.test.skip=true -Dcheckstyle.skip"


# Build Library Module
run_command "cd ${LIBRARY_MODULE_PATH}"
run_command "mvn install -Dmaven.test.skip=true"

# Build Connectivity Module
run_command "cd ${CONNECTIVITY_MODULE_PATH}"
run_command "mvn install -Dmaven.test.skip=true"


# Build Intruder Module
run_command "cd ${INTRUDER_MODULE_PATH}"
run_command "mvn install -Dmaven.test.skip=true"

# Build Lab Module
run_command "cd ${LAB_MODULE_PATH}"
run_command "mvn install -Dmaven.test.skip=true"
