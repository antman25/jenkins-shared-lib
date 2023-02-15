import groovy.transform.Field
@Field final String TEST_PIPELINE_PATH = 'test-pipeline'

String getPathPrefix(boolean is_delivery_branch)
{
    if (is_delivery_branch == true)
    {
        return ""
    }
    else
    {
        return "/${job_testing_folder}/${branch_name}"
    }
}

def templatePipelineJob(String job_path, string display_name, string jenkinsfile_path)
{
    pipelineJob(job_path) {
        displayName(display_name)
        description("Test job for podTemplate")
        logRotator {
            numToKeep(20)
        }
        environmentVariables {
            env('BRANCH_NAME', branch_name)
        }

        definition {
            cpsScm {
                scm {
                    git {
                        remote { url(tools_url) }
                        branches(branch_name)
                        scriptPath(jenkinsfile_path)
                        extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                    }
                }
            }
        }
    }
}

boolean main()
{
    String path_prefix = getPathPrefix(branch_name, delivery_branch)

    try {
        templatePipelineJob("${path_prefix}/${TEST_PIPELINE_PATH}/pod-template-python3", "pythonTemplate", "pipeline-tests/template_tests/python3/Jenkinsfile")
        templatePipelineJob("${path_prefix}/${TEST_PIPELINE_PATH}/pod-template-docker", "dockerTemplate", "pipeline-tests/template_tests/docker/Jenkinsfile")
        templatePipelineJob("${path_prefix}/${TEST_PIPELINE_PATH}/pod-template-helm", "helmTemplate", "pipeline-tests/template_tests/helm/Jenkinsfile")
    }
    catch (Exception ex) {
        println("createPodTemplateTests() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}


