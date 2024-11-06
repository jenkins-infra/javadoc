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
        sh 'cd build && tar -cjf javadoc-site.tar.bz2 site'
        archiveArtifacts artifacts: 'build/*.tar.bz2',
                            allowEmptyArchive: false,
                            fingerprint: false,
                            onlyIfSuccessful: true
    }

    if (infra.isTrusted()){
        stage('Publish on Azure') {
            try {
                infra.withFileShareServicePrincipal([
                    servicePrincipalCredentialsId: 'trustedci_javadocjenkinsio_fileshare_serviceprincipal_writer',
                    fileShare: 'javadoc-jenkins-io',
                    fileShareStorageAccount: 'javadocjenkinsio',
                    durationInMinute: 20
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
                    '''
                }
            } catch (err) {
                currentBuild.result = 'FAILURE'
                // Only collect azcopy log when the deployment fails, because it is an heavy one
                sh '''
                # Retrieve azcopy logs to archive them
                cat /home/jenkins/.azcopy/*.log > azcopy.log
                '''
                archiveArtifacts 'azcopy.log'
                
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
