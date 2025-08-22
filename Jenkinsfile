#!/usr/bin/env groovy

properties([
    buildDiscarder(logRotator(numToKeepStr: '2')),
    pipelineTriggers([cron('H 5 * * 3')]),
])

node('linux') {
    checkout scm

    dir("scripts/build") {
        deleteDir()
    }

    dir("build") {
        deleteDir()
    }

    stage('Generate Javadocs') {
        withEnv([
                "PATH+MVN=${tool 'mvn'}/bin",
                "JAVA_HOME=${tool 'jdk17'}",
                "PATH+GROOVY=${tool 'groovy'}/bin",
                "PATH+JAVA=${tool 'jdk17'}/bin",
        ]) {
            if (infra.isTrusted()) {
                sh './scripts/generate-javadoc.sh'
            } else {
                infra.withArtifactCachingProxy(true) {
                    sh './scripts/generate-javadoc.sh'
                }
            }
        }
    }

    stage('Generate Shortnames') {
        sh './scripts/generate-shortnames.sh'
    }

    stage('Prepare Latest') {
        sh './scripts/default-to-latest.sh'
    }

    stage('Archive') {
        if (infra.isTrusted()){
            stash includes: 'build/site/**', name: 'site'
        } else {
            sh 'cd build && tar -cjf javadoc-site.tar.bz2 site'
            archiveArtifacts artifacts: 'build/*.tar.bz2',
                allowEmptyArchive: false,
                fingerprint: false,
                onlyIfSuccessful: true
        }
    }
}

if (infra.isTrusted()) {
    node('updatecenter') {
        stage('Publish') {
            unstash 'site'
            sh '''
            time rsync --recursive --links -D \
                --checksum --verbose \
                ./build/site/ /data-storage-jenkins-io/javadoc.jenkins.io/
            '''
        }
    }
}
