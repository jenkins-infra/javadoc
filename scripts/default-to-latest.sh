#!/bin/bash

source "$(dirname "$0")/common.sh"

LATEST=$(ls "$ARCHIVE_DIR" | sort --version-sort | tail -n 1)
LATESTDIR=${ARCHIVE_DIR}/${LATEST}

pushd "$LATESTDIR"
cp -R * "${SITE_DIR}"
popd

available_javadoc_links=""
for archive in $(find "${ARCHIVE_DIR}" -maxdepth 1 -type d -name 'jenkins-*' | sort --version-sort -r); do
    version="${archive##*/jenkins-}"
	# First one is the latest Weekly release, while the other are LTS ones
	if [[ -z "${available_javadoc_links}" ]]; then
		available_javadoc_links="<a href="index-core.html">Latest Weekly (${version})</a>"
	else
	    available_javadoc_links="${available_javadoc_links}<br><a href=\"archive/jenkins-${version}/\">LTS ${version}</a>"
	fi
done

pushd "${SITE_DIR}"
mv index.html index-core.html
cat >index.html <<EOF
<html><head><title>Jenkins Javadoc</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
<link rel="stylesheet" type="text/css" href="style.css"/>
<div><h2><a href="index-core.html">Jenkins core</a></h2>${available_javadoc_links}</div>
<div><h2><a href="plugin/">Jenkins plugins</a></div>
<div><h2><a href="component/">Jenkins components</a></div>
</head><body>
EOF
popd
cp "$(dirname "$0")/../resources/style.css" "${SITE_DIR}"

exit 0
