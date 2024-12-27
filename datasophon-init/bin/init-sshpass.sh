#!/bin/bash

if [ $UID -ne 0 ]; then
  echo Non root user. Please run as root.
  exit 1
fi
if [ -L $0 ]; then
  BASE_DIR=$(dirname $(readlink $0))
else
  BASE_DIR=$(dirname $0)
fi
BASE_PATH=$(
  cd ${BASE_DIR}
  pwd
)
INIT_PATH=$(dirname "${BASE_PATH}")
echo "INIT_PATH: ${INIT_PATH}"
INIT_BIN_PATH=${INIT_PATH}/bin
echo "INIT_BIN_PATH: ${INIT_BIN_PATH}"
INIT_SBIN_PATH=${INIT_PATH}/sbin
echo "INIT_SBIN_PATH: ${INIT_SBIN_PATH}"
PACKAGES_PATH=${INIT_PATH}/packages
echo "PACKAGES_PATH: ${PACKAGES_PATH}"
echo "${PACKAGES_PATH}"
SSHPASS_FOLDER_NAME=sshpass

SSHPASS_TAR_NAME=sshpass.tar.gz
rpm -qa | grep sshpass
if [ "$?" == "0" ]; then
  echo "sshpass exists"
else
  yum -y install sshpass
  rpm -qa | grep sshpass
  if [ "$?" == "0" ]; then
    echo "init sshpass install successfully"
  fi
fi
