#!/bin/bash

export OUTPUT_DIR="${PWD}/build"
export JENKINS_DIR="${OUTPUT_DIR}/jenkins.git"
export SITE_DIR="${OUTPUT_DIR}/site"
export ARCHIVE_DIR="${SITE_DIR}/archive"

function ensure_bin() {
    which $1 2>&1 > /dev/null
    if [ $? -ne 0 ]; then
        echo ">> ${1} must be installed and on PATH (${PATH})"
        exit 1;
    fi;
}

function mkdir_p() {
    if [ ! -d $1 ]; then
        echo ">> creating ${1}";
        mkdir -p $1
    fi;
}
