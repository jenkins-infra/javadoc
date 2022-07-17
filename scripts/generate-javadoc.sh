#!/bin/bash

set -e

source "$(dirname $0)/common.sh"

ensure_bin 'wget'
ensure_bin 'jar'
ensure_bin 'groovy'
ensure_bin 'curl'

mkdir_p $OUTPUT_DIR
mkdir_p $SITE_DIR
mkdir_p $ARCHIVE_DIR

function generate_javadoc_core() {
    declare release=$1
    # First we need to get the built javadocs. 
    wget --no-verbose https://repo.jenkins-ci.org/releases/org/jenkins-ci/main/jenkins-core/${release}/jenkins-core-${release}-javadoc.jar

    # We need to move the contents to a new directory to then extract the content.
    mkdir jenkins-core-${release}
    mv jenkins-core-${release}-javadoc.jar jenkins-core-${release}
    cd jenkins-core-${release}

    # Extract the content of the javadoc jar
    jar -xf jenkins-core-${release}-javadoc.jar

    # Verify that there was no error when extracting the javadocs
    if [ $? -ne 0 ]; then
        echo ">> failed to generate javadocs for ${release}"
    fi;

    #
    # Until JDK-8215291 is backported to Java 11, work around the problem by
    # munging the file ourselves.
    #
    if [ -f search.js ]; then
        sed -i -e 's/if (ui.item.p == item.l)/if (item.m \&\& ui.item.p == item.l)/g' search.js
    fi

    # Since Java 9, the javadoc(1) command's package-list file has been
    # superseded by a new element-list file. However, the Java 8 version of
    # javadoc(1) still consumes the old package-list file. In order to support
    # both Java 8 and Java 11 builds (including supporting the ability to link
    # against https://javadoc.jenkins.io), we work around the problem by
    # ensuring that both package-list and element-list exist. When we no longer
    # need to support Java 8 builds, this workaround can be deleted.
    #
    if [ -e package-list ] && [ ! -e element-list ]; then
        cp package-list element-list
    elif [ -e element-list ] && [ ! -e package-list ]; then
        cp element-list package-list
    fi

    # Move the docs to the archive directory
    cd .. # Leave the current directory
    mv jenkins-core-${release} ${ARCHIVE_DIR}/jenkins-${release}
}


wget --no-verbose -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 || { echo "Failed to download jq" >&2 ; exit 1; }
chmod +x jq || { echo "Failed to make jq executable" >&2 ; exit 1; }

set -o pipefail

if [ -z "${LTS_RELEASES}" ] ; then
    echo "LTS_RELEASES is not defined. Pulling all releases from Jenkins Artifactory"
    LTS_RELEASES=$( curl 'https://repo.jenkins-ci.org/api/search/versions?g=org.jenkins-ci.main&a=jenkins-core&repos=releases&v=?.*.1' | ./jq --raw-output '.results[].version' | head -n 10 ) || { echo "Failed to retrieve list of releases" >&2 ; exit 1 ; }
fi

set +o pipefail

for release in $LTS_RELEASES ; do
    echo ">> Found release ${release/%.1/}"
    generate_javadoc_core "${release/%.1/}"
done;

LATEST=$(wget -q -O - "https://updates.jenkins.io/current/latestCore.txt")

echo ">> Found release ${LATEST}"
generate_javadoc_core ${LATEST}

groovy -cp src/main/groovy scripts/generate-javadoc-plugins.groovy "${PLUGINS}"
# TODO: "libs" would be ideal, but this entry is already full of garbage files
groovy -cp src/main/groovy scripts/generate-javadoc-components.groovy
