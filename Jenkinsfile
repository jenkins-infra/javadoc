#!/usr/bin/env groovy

properties([
    buildDiscarder(logRotator(numToKeepStr: '2')),
    pipelineTriggers([cron('H 5 * * 1')]),
])

node('linux') {
    checkout scm

    dir("scripts/build") {
        deleteDir()
    }

    dir("build") {
        deleteDir()
    }

    def repositoryOrigin = "https://repo." + (env.ARTIFACT_CACHING_PROXY_PROVIDER ?: 'azure') + ".jenkins.io"

    stage('Generate Javadocs') {
        retry(3) {
            withEnv([
                    "PATH+MVN=${tool 'mvn'}/bin",
                    "JAVA_HOME=${tool 'jdk11'}",
                    "PATH+GROOVY=${tool 'groovy'}/bin",
                    "PATH+JAVA=${tool 'jdk11'}/bin",
                    "ARTIFACT_CACHING_PROXY_ORIGIN=${repositoryOrigin}"
            ]) {
                withCredentials([usernamePassword(
                    credentialsId: 'artifact-caching-proxy-credentials',
                    usernameVariable: 'ARTIFACT_CACHING_PROXY_USERNAME',
                    passwordVariable: 'ARTIFACT_CACHING_PROXY_PASSWORD'
                )]) {
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
            /* -> https://github.com/Azure/blobxfer
            Require credential 'JAVADOC_STORAGEACCOUNTKEY' set to the storage account key */
            withCredentials([string(credentialsId: 'JAVADOC_STORAGEACCOUNTKEY', variable: 'JAVADOC_STORAGEACCOUNTKEY')]) {
                sh './scripts/blobxfer upload \
                --local-path /data/site \
                --storage-account-key $JAVADOC_STORAGEACCOUNTKEY \
                --storage-account prodjavadoc \
                --remote-path javadoc \
                --recursive \
                --mode file \
                --skip-on-md5-match \
                --file-md5 \
                --connect-timeout 30 \
                --delete'
            }
        }
    }


    stage('Clean up') {
        echo 'We want to generate fresh javadocs on each run'
        dir('build/site') {
            deleteDir()
        }
    }
}
