#!/bin/bash

source "$(dirname $0)/common.sh"

ensure_bin 'wget'
ensure_bin 'jar'
ensure_bin 'groovy'

mkdir_p $OUTPUT_DIR
mkdir_p $SITE_DIR
mkdir_p $ARCHIVE_DIR

function generate_javadoc_core() {
    declare release=$1
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
        echo ">> failed to generate javadocs for ${release}"
    fi;

    # Move the docs to the archive directory
    cd .. # Leave the current directory
    mv jenkins-core-${release} ${ARCHIVE_DIR}/jenkins-${release}
}


wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 || { echo "Failed to download jq" >&2 ; exit 1; }
chmod +x jq || { echo "Failed to make jq executable" >&2 ; exit 1; }

set -o pipefail

RELEASES=$( curl 'https://repo.jenkins-ci.org/api/search/versions?g=org.jenkins-ci.main&a=jenkins-core&repos=releases&v=?.*.1' | ./jq --raw-output '.results[].version' | head -n 10 ) || { echo "Failed to retrieve list of releases" >&2 ; exit 1 ; }


set +o pipefail

for release in $RELEASES ; do
    echo ">> Found release ${release/%.1/}"
    generate_javadoc_core "${release/%.1/}"
done;

LATEST=$(wget -q -O - "https://updates.jenkins.io/current/latestCore.txt")

echo ">> Found release ${LATEST}"
generate_javadoc_core ${LATEST}

groovy scripts/generate-javadoc-plugins.groovy
