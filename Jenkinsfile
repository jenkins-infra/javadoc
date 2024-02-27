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
                withCredentials([usernamePassword(
                    credentialsId: 'artifact-caching-proxy-credentials',
                    usernameVariable: 'ARTIFACT_CACHING_PROXY_USERNAME',
                    passwordVariable: 'ARTIFACT_CACHING_PROXY_PASSWORD'
                )]) {
                    def repositoryOrigin = "https://repo." + (env.ARTIFACT_CACHING_PROXY_PROVIDER ?: 'azure') + ".jenkins.io"
                    withEnv(["ARTIFACT_CACHING_PROXY_ORIGIN=${repositoryOrigin}"]) {
                        sh './scripts/generate-javadoc.sh'
                    }
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
            parallel(
                failFast: false,
                'With blobxfer on prodjavadoc': {
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
                },
                'With azcopy on javadocjenkinsio': {
                    infra.withFileShareServicePrincipal([
                        servicePrincipalCredentialsId: 'trustedci_javadocjenkinsio_fileshare_serviceprincipal_writer',
                        fileShare: 'javadoc-jenkins-io',
                        fileShareStorageAccount: 'javadocjenkinsio',
                        durationInMinute: 45
                    ]) {
                        sh '''
                        # Don't output sensitive information
                        set +x

                        # Synchronize the File Share content
                        azcopy sync \
                            --skip-version-check \
                            --recursive=true\
                            --delete-destination=true \
                            --compare-hash=MD5 \
                            --put-md5 \
                            --local-hash-storage-mode=HiddenFiles \
                            ./build/site/ "${FILESHARE_SIGNED_URL}"

                        # Retrieve azcopy logs to archive them
                        cat /home/jenkins/.azcopy/*.log > azcopy.log
                        '''
                        archiveArtifacts 'azcopy.log'
                    }
                }
            )
        }
    }

    stage('Clean up') {
        echo 'We want to generate fresh javadocs on each run'
        dir('build/site') {
            deleteDir()
        }
    }
}
