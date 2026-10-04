#!/usr/bin/env groovy
properties([
    buildDiscarder(logRotator(numToKeepStr: '2')),
    pipelineTriggers([cron('H 5 * * 3')]),
])

def branches = [:]

branches['Javadoc'] = {
    dir("scripts/build") {
        deleteDir()
    }

    dir("build") {
        deleteDir()
    }

    stage('Generate Javadocs') {
        withEnv([
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
        sh 'cd build && tar -cjf javadoc-site.tar.bz2 site'
        archiveArtifacts artifacts: 'build/*.tar.bz2',
                            allowEmptyArchive: false,
                            fingerprint: false,
                            onlyIfSuccessful: true
    }

    if (infra.isTrusted()){
        stage('Publish on Azure') {
            infra.deployWebsite('build/site')
        }
        stage ('Publish build report') {
            publishBuildStatusReport()
        }
    }

    stage('Clean up') {
        echo 'We want to generate fresh javadocs on each run'
        dir('build/site') {
            deleteDir()
        }
    }
}

branches['Dockerfile'] = {
    sh 'docker build -t javadoc-test .'
}

node('linux') {
    checkout scm

    // No need to test Dockerfile build on any other controller than ci.jenkins.io
    if (!infra.isCiController()) {
        branches.remove('Dockerfile')
    }
    parallel(branches)
}
