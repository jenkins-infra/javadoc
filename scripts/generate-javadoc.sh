#!/bin/bash

OUTPUT_DIR="${PWD}/build"
JENKINS_DIR="${OUTPUT_DIR}/jenkins.git"
SITE_DIR="${OUTPUT_DIR}/site"
ARCHIVE_DIR="${SITE_DIR}/archive"


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

ensure_bin 'mvn'
ensure_bin 'git'

mkdir_p $OUTPUT_DIR
mkdir_p $SITE_DIR
mkdir_p $ARCHIVE_DIR


if [ ! -d $JENKINS_DIR ]; then
    echo ">> ${JENKINS_DIR} does not exist, cloning repository"
    git clone https://github.com/jenkinsci/jenkins.git ${JENKINS_DIR}
else
    echo ">> ${JENKINS_DIR} already exists, updating refs"
    (cd $JENKINS_DIR && git fetch --all)
fi;


pushd $JENKINS_DIR

    RELEASES=$(git tag -l jenkins-2\* --sort=-version:refname)

    for release in $RELEASES; do
        echo ">> Found release ${release}"
        git checkout $release
        git clean -xf
        mvn javadoc:aggregate

        if [ $? -ne 0 ]; then
            echo ">> failed to generate javadocs for ${release}"
            exit 1;
        fi;

        mv target/site/apidocs ${ARCHIVE_DIR}/${release}
    done;

popd;
