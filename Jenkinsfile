#!/usr/bin/env groovy

properties([
    buildDiscarder(logRotator(numToKeepStr: '2')),
    pipelineTriggers([cron('H 5 * * 1')]),
])

try {
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
                    "JAVA_HOME=${tool 'jdk8'}",
                    "PATH+GROOVY=${tool 'groovy'}/bin",
                    "PATH+JAVA=${tool 'jdk8'}/bin",
                ]) {
                sh './scripts/generate-javadoc.sh'
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
            archive 'build/*.tar.bz2'
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
}
catch (exc) {
    String recipient = 'infra@lists.jenkins-ci.org'

    mail subject: "${env.JOB_NAME} (${env.BUILD_NUMBER}) failed",
            body: "It appears that ${env.BUILD_URL} is failing, somebody should do something about that",
              to: recipient,
         replyTo: recipient,
            from: 'noreply@ci.jenkins.io'
}
