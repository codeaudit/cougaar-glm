#!/bin/sh
# Should be sourced by a shell (or shell script) to set defaults for
# LIBPATHS and ALP3RDPARTY environment variables.
# ALP_INSTALL_PATH must already be set

if [ -z "$ALP_INSTALL_PATH" ]; then
    echo "ALP_INSTALL_PATH is not set. Using /alp"
    ALP_INSTALL_PATH=/alp
    export ALP_INSTALL_PATH
fi

LIBPATHS=$ALP_INSTALL_PATH/lib/core.jar
if [ "$ALP_DEV_PATH" != "" ]; then
    os=`uname`
    SEP=";"
    if [ $os = "Linux" -o $os = "SunOS" ]; then SEP=":"; fi
    LIBPATHS="${ALP_DEV_PATH}${SEP}${LIBPATHS}"
fi
BOOTPATH=$ALP_INSTALL_PATH/lib/javaiopatch.jar
if [ "$ALP3RDPARTY" = "" ]; then
     ALP3RDPARTY=/opt/alp-jars
     export ALP3RDPARTY
fi
