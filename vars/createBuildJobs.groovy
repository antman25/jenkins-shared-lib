#!/usr/bin/env groovy

def call() {
    properties([disableConcurrentBuilds()])

    node {
        stage ('Run Job DSL')
        {
            def params = [:]
            jobDsl targets: ['main.groovy'].join('\n'),
                    removedJobAction: 'DELETE',
                    removedViewAction: 'DELETE',
                    lookupStrategy: 'JENKINS_ROOT',
                    additionalParameters: params
        }
    }
}