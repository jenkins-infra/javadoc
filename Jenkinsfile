#!/usr/bin/env groovy

node {
    checkout scm

    stage 'Generate Javadocs'
    withEnv(["PATH+MVN=${tool 'mvn'}/bin", "JAVA_HOME=${tool 'jdk8'}"]) {
        sh './scripts/generate-javadoc.sh'
    }

    stage 'Generate Shortnames'
    sh './scripts/generate-shortnames.sh'

    stage 'Prepare Latest'
    sh './scripts/default-to-latest.sh'

    stage 'Archive'
    sh 'cd build && tar -cjf javadoc-site.tar.bz2 site'
    archive 'build/*.tar.bz2'
}
