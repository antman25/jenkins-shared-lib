import org.yaml.snakeyaml.Yaml

def call() {
    properties([disableConcurrentBuilds()])

    podTemplate {
        node(POD_LABEL) {

            stage('Clone code') {
                checkout scm
            }

            stage('Job DSL') {
                try  {
                    String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
                    String sanitized_branch_name = utils.sanitizeBranchName(branch_name)

                    def config_data = readFile 'config/config.yaml'
                    def config_yaml = new Yaml().load(config_data)

                    def params = [ 'pipeline_root_folder' : 'pipeline_root',
                                                'job_testing_folder' : 'job_testing',
                                                'branch_name' : sanitized_branch_name,
                                                'delivery_branch' : 'main',
                                                'config_data' : config_yaml,
                                                'workspace_path' : "${WORKSPACE}",
                                                'tools_url' : "${TOOLS_URL}"]

                    jobDsl targets: ['job_automation/pipeline/create_pipeline_jobs.groovy',
                                     'job_automation/tenants/create_tenant_root.groovy',
                                     'job_automation/tenants/create_tenant_build_jobs.groovy'].join('\n'),
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            lookupStrategy: 'JENKINS_ROOT',
                            additionalParameters: params
                }
                catch (Exception ex) {
                    println("createBuildJobs.groovy Exception: ${ex.toString()}")
                    sh('exit 1')
                }
            }
        }
    }
}