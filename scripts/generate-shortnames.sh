#!/bin/bash

source "$(dirname "$0")/common.sh"

LATEST=$(ls "$ARCHIVE_DIR" | sort --version-sort | tail -n 1)
LATESTDIR=${ARCHIVE_DIR}/${LATEST}

SHORTNAMEDIR=${SITE_DIR}/byShortName

mkdir_p "$SHORTNAMEDIR"

set +x

# Find all the files that look like class files, i.e. .html files without
# dashes (e.g. index-all.html)
for path in $(find "$LATESTDIR" -type f \( -iname "*.html" ! -iname "*[-]*" \)); do

	# Bash string substitutions are fun =_=
	trimmed=${path//.html/}
	# Compute the absolute URL path to the classes, including our /byShortName/ urlspace
	relative="/${path//$LATESTDIR\//}"

	# purge any paths we found with hyphens. This is necessary because the
	# `-iname` filter above only applies to the filename, and not the full
	# path. This mostly removes the /class-use/ paths
	if [[ $(expr index "$relative" "\-") != "0" ]]; then
		continue
	fi

	parentDir=$(basename "$(dirname "$trimmed")")
	classname=$(basename "$trimmed")

	if [[ ${parentDir} == "hudson" ]]; then
		classname="${parentDir}.${classname}"
	fi

	if [[ ${parentDir} == "jenkins" ]]; then
		classname="${parentDir}.${classname}"
	fi

	redirectdir=${SHORTNAMEDIR}/${classname}
	mkdir -p "$redirectdir"

	cat >"$redirectdir/index.html" <<EOF
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="refresh" content="0;URL=${relative}"/>
  </head>
  <body>
    Redirecting to page <a href="${relative}">${relative}</a>
  </body>
</html>
EOF
done

exit 0
