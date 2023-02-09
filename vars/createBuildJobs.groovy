#!/usr/bin/env groovy

def call() {
    properties([disableConcurrentBuilds()])

    node() {

        stage('Clone code')
        {
            checkout scm
        }

        stage ('Job DSL') {
            def params = [:]
            jobDsl targets: ['seed_jobs/main.groovy'].join('\n'),
                    removedJobAction: 'DELETE',
                    removedViewAction: 'DELETE',
                    lookupStrategy: 'JENKINS_ROOT',
                    additionalParameters: params
        }
    }
}