#!/bin/bash

source "$(dirname $0)/common.sh"

ensure_bin 'mvn'
ensure_bin 'git'

mkdir_p $OUTPUT_DIR
mkdir_p $SITE_DIR
mkdir_p $ARCHIVE_DIR

function generate_javadoc_core() {
    # First we need to get the built javadocs. 
    wget https://repo.jenkins-ci.org/releases/org/jenkins-ci/main/jenkins-core/${release}/jenkins-core-${release}-javadoc.jar

    # We need to move the contents to a new directory to then extract the content.
    mkdir jenkins-core-${release}
    mv jenkins-core-${release}-javadoc.jar jenkins-core-${release}
    cd jenkins-core-${release}

    # Extract the content of the javadoc jar
    jar -xvf jenkins-core-${release}-javadoc.jar

    # Verify that there was no error when extracting the javadocs
    if [ $? -ne 0 ]; then
        echo ">> failed to generate javadocs for ${1}"
    fi;

    # Move the docs to the archive directory
    cd .. # Leave the current directory
    mv jenkins-core-${release} ${ARCHIVE_DIR}/${1}
}

for release in 1.554 1.565 1.580 1.596 1.609 1.625 1.642 1.651 2.7 2.19; do
    echo ">> Found release ${release}"
    generate_javadoc_core "${release}"
done;

groovy scripts/generate-javadoc-plugins.groovy
