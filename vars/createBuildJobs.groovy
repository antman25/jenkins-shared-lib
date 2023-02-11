#!/usr/bin/env groovy

def call() {
    properties([disableConcurrentBuilds()])

    node(POD_LABEL) {

        stage('Clone code')
        {
            checkout scm
        }

        stage ('Job DSL') {
            def config = readFile 'seed_jobs/config.yaml'
            def params = ['workspace': "${WORKSPACE}",
                                        'git_branch' : "${GIT_BRANCH}",
                                       'config' : config ]
            jobDsl targets: ['seed_jobs/main.groovy'].join('\n'),
                    removedJobAction: 'DELETE',
                    removedViewAction: 'DELETE',
                    lookupStrategy: 'JENKINS_ROOT',
                    additionalParameters: params
        }
    }
}