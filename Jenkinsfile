#!/usr/bin/env groovy
// Test of https://github.com/jenkins-infra/pipeline-library/pull/972
@Library('pipeline-library@pull/972/head') _

node('linux') {
    checkout scm

    dir("scripts/build") {
        deleteDir()
    }

    dir("build") {
        deleteDir()
    }

    if (infra.isTrusted()){
        stage('Get lenght of FILESHARE_SIGNED_URL from "withFileShareServicePrincipal" function') {
            try {
                infra.withFileShareServicePrincipal([
                    servicePrincipalCredentialsId: 'trustedci_javadocjenkinsio_fileshare_serviceprincipal_writer',
                    fileShare: 'javadoc-jenkins-io',
                    fileShareStorageAccount: 'javadocjenkinsio',
                    durationInMinute: 30
                ]) {
                    sh '''
                    length=${#FILESHARE_SIGNED_URL}

                    echo "FILESHARE_SIGNED_URL lenght: ${length}"
                    '''
                }
            }
        }
    }
}
