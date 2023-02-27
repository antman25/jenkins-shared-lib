String getPathPrefix(boolean isPriBranch, String rootPath, String branchName)
{
    def slugBranchName = utils.slugify(branchName)
    if (isPriBranch == false)
        return "/${rootPath}/${slugBranchName}"
    return ""
}

def call() {
    properties([disableConcurrentBuilds()])

    podTemplates.pythonTemplate {
        node(POD_LABEL) {
            def params = [:]

            String slugBranchName = utils.slugify(env.BRANCH_NAME)

            println("Branch Name: ${env.BRANCH_NAME} -- Branch Name(Slug): ${slugBranchName}")

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

                sh 'cat config/config.yaml'
            }

            // TODO: Remove when vault is integrated
            withCredentials([usernamePassword(credentialsId: 'bitbucket-plugin-cred', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                stage('Job DSL') {
                    try {
                        Map configYaml = readYaml (file: 'config/config.yaml')
                        boolean isPrimaryBranch = utils.isPrimaryBranch(env.BRANCH_NAME)
                        String pathTestingRoot = 'job-testing'

                        params = [ 'isPrimaryBranch' : isPrimaryBranch,
                                   'rootJobTesting' : pathTestingRoot,
                                   'pathPrefix' : getPathPrefix(isPrimaryBranch, getPathPrefix, env.BRANCH_NAME),
                                   'configYaml' : configYaml,
                                   'branchName' : env.BRANCH_NAME,
                                   'branchNameSlug' : utils.slugify(env.BRANCH_NAME),
                                   'pathWorkspace' : env.WORKSPACE,
                                   'urlTools' : env.TOOLS_URL,
                                   'passwordBootstrap' : env.PASSWORD ]
                        //'dsl/createTenantRoot.groovy'
                        jobDsl targets: [   'dsl/createTestingRoot.groovy',


                                            ].join('\n'),
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

            stage ('Test All podTemplates') {
                pod_template_jobs = [ :]
                pod_template_jobs['podtemplate_python'] = {
                    stage ('podTemplate: python')
                    {
                        //build job: "${path_prefix}/test-pipeline/pod-template-python3"
                    }
                }

                pod_template_jobs['podtemplate_docker'] = {
                    stage ('podTemplate: docker')
                    {
                        //build job: "${path_prefix}/test-pipeline/pod-template-docker"
                    }
                }

                pod_template_jobs['podtemplate_helm'] = {
                    stage ('podTemplate: helm')
                    {
                        //build job: "${path_prefix}/test-pipeline/pod-template-helm"
                    }
                }

                parallel (pod_template_jobs)
            }

            stage('Test All build steps')
            {
                build_step_jobs = [:]

                build_step_jobs['build_docker'] = {
                    stage ('buildDockerImage')
                    {
                        //build job: "${path_prefix}/test-pipeline/build-docker"
                    }
                }

                build_step_jobs['build_helm'] = {
                    stage ('buildHelmChart')
                    {
                        //build job: "${path_prefix}/test-pipeline/build-helm"
                    }
                }

                parallel(build_step_jobs)
            }

        }
    }
}