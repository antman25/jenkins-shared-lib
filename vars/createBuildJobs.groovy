import org.yaml.snakeyaml.Yaml

def call() {
    properties([disableConcurrentBuilds()])

    podTemplates.pythonTemplate {
        node(POD_LABEL) {

            stage('Clone code') {
                checkout scm
            }

            stage('Build Config')
            {
                println("Executing command in python3 container")
                container('python')
                {
                    sh '''
                        python3 -m pip install PyYAML && \
                        python3 config/build_config.py                         
                       '''
                }
            }


            stage('Job DSL') {
                try  {
                    String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
                    String sanitized_branch_name = utils.sanitizeBranchName(branch_name)
                    println("Sanitized branch name: ${sanitized_branch_name}")
                    def config_data = readFile 'config/config.yaml'
                    def config_yaml = new Yaml().load(config_data)

                    def params = [ 'config_yaml' : config_yaml,
                                                'branch_name' : sanitized_branch_name,
                                                'delivery_branch' : "${DELIVERY_BRANCH}",
                                                'pipeline_root_folder' : "${PIPELINE_ROOT}",
                                                'job_testing_folder' : "${JOB_TESTING_ROOT}",
                                                'workspace_path' : "${WORKSPACE}",
                                                'tools_url' : "${TOOLS_URL}"]

                    jobDsl targets: ['job-automation/dsl/pipeline/create_pipeline_jobs.groovy',
                                     'job-automation/dsl/tenants/create_tenant_jobs.groovy' ].join('\n'),
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            lookupStrategy: 'JENKINS_ROOT',
                            additionalParameters: params
                }
                catch (Exception ex) {
                    println("Exception: ${ex.toString()}")
                    sh('exit 1')
                }
            }
        }
    }
}