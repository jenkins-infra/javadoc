#!/bin/bash

source "$(dirname $0)/common.sh"

LATEST=$(ls $ARCHIVE_DIR | sort --version-sort | tail -n 1)
LATESTDIR=${ARCHIVE_DIR}/${LATEST}

pushd $LATESTDIR
    cp -R * ${SITE_DIR}
popd
