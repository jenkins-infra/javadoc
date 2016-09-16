#!/usr/bin/env groovy

properties([buildDiscarder(logRotator(numToKeepStr: '2'))])

try {
    node {
        checkout scm

        stage 'Generate Javadocs'
        withEnv(["PATH+MVN=${tool 'mvn'}/bin", "JAVA_HOME=${tool 'jdk8'}", "PATH+GROOVY=${tool 'groovy'}/bin"]) {
            sh './scripts/generate-javadoc.sh'
        }

        stage 'Generate Shortnames'
        sh './scripts/generate-shortnames.sh'

        stage 'Prepare Latest'
        sh './scripts/default-to-latest.sh'

        stage 'Archive'
        sh 'cd build && tar -cjf javadoc-site.tar.bz2 site'
        archive 'build/*.tar.bz2'

        stage 'Clean up'
        echo 'We want to generate fresh javadocs on each run'
        dir('build/site') {
            deleteDir()
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
