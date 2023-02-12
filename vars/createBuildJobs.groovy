#!/usr/bin/env groovy

def call() {
    properties([disableConcurrentBuilds()])

    podTemplate {
        node(POD_LABEL) {

            stage('Clone code')
            {
                checkout scm
            }

            stage('Job DSL') {
                def branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
                def sanitized_branch_name = utils.sanitizeBranchName(branch_name)

                def config_data = readFile 'config/config.yaml'
                def params = [  'pipeline_root_folder' : 'pipeline_root',
                                             'job_testing_folder' : 'job_testing',
                                             'branch_name' : sanitized_branch_name,
                                             'delivery_branch' : 'main',
                                             'config_data' : config_data,
                                             'workspace_path' : "${WORKSPACE}",
                                             'tools_url' : "${TOOLS_URL}"]

                jobDsl targets: ['job_automation/build_pipeline_root.groovy',
                                 'job_automation/build_tenant_root.groovy'].join('\n'),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'JENKINS_ROOT',
                        additionalParameters: params
            }
        }
    }
}