#!/bin/bash

export OUTPUT_DIR="${PWD}/build"
export JENKINS_DIR="${OUTPUT_DIR}/jenkins.git"
export SITE_DIR="${OUTPUT_DIR}/site"
export ARCHIVE_DIR="${SITE_DIR}/archive"

function die() {
	echo "$(basename "$0"): $*" >&2
	exit 1
}

function ensure_bin() {
	which "$1" >/dev/null 2>&1 || die "${1} must be installed and on PATH (${PATH})"
}

function mkdir_p() {
	if [[ ! -d $1 ]]; then
		echo ">> creating ${1}"
		mkdir -p "$1"
	fi
}
