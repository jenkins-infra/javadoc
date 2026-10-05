#!/usr/bin/env groovy
properties([
    buildDiscarder(logRotator(numToKeepStr: '2')),
    pipelineTriggers([cron('H 5 * * 3')]),
])

node('linux') {
    checkout scm

    stage('Generate Javadocs') {
        withEnv([
                "JAVA_HOME=${tool 'jdk17'}",
                "PATH+GROOVY=${tool 'groovy'}/bin",
                "PATH+JAVA=${tool 'jdk17'}/bin",
        ]) {
            if (infra.isTrustedCiController()) {
                sh 'make build'
            } else {
                infra.withArtifactCachingProxy(true) {
                    sh 'make build'
                }
            }
        }
    }

    stage('Archive') {
        sh 'cd build && tar -cjf javadoc-site.tar.bz2 site'
        archiveArtifacts artifacts: 'build/*.tar.bz2',
                            allowEmptyArchive: false,
                            fingerprint: false,
                            onlyIfSuccessful: true
    }

    if (infra.isTrustedCiController()){
        stage('Publish on Azure') {
            infra.deployWebsite('build/site')
        }
        stage ('Publish build report') {
            publishBuildStatusReport()
        }
    }

    stage('Cleanup') {
        // Workspace cleanup in case we need to run this job from permanent trusted.ci.jenkins.io agent
        echo 'We want to generate fresh javadocs on each run'
        sh 'make clean'
    }
}
