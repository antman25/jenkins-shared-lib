import groovy.transform.Field

String getPathPrefix(boolean main_branch)
{
    if (main_branch == true)
    {
        return ""
    }
    else
    {
        return "/${job_testing_folder}/${branch_name}"
    }
}

boolean createPipelineRootFolder(String path_prefix)
{
    try
    {
        folder(pipeline_root_folder)
        {
            displayName("000 - Pipeline")
        }

        if (path_prefix != "")
        {
            folder("${path_prefix}")
        }


        folder("${path_prefix}/${pipeline_root_folder}")
        {
            displayName("000 - Pipeline Admin")
            description("Pipeline Admin jobs Area")

            properties {
                authorizationMatrix {
                    inheritanceStrategy { nonInheriting() }
                }
            }
        }
    }
    catch (Exception ex)
    {
        println("createPipelineRoot() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean createDeployJob(String path_prefix)
{
    try
    {
        def tools_url = getBinding().getVariables().getOrDefault('TOOLS_URL', 'NotSet')
        def desc = "Runs all the JobDSL for job deployment"
        def main_branch = branch_name == delivery_branch
        multibranchPipelineJob("${path_prefix}/${pipeline_root_folder}/deploy-all-jobs")
        {
            //if (branch_name != delivery_branch)
            //   disabled()

            displayName("000 - Deploy Development Jenkins Jobs")
            if (main_branch) {
                description(desc)
            }
            else {
                description(desc + "\n!!! Intentionally ignoring main branch from scanning while on a development branch !!!")
            }

            branchSources {
                branchSource {
                    source {
                        git {
                            remote (tools_url)
                            id ('pipeline-root-job-deploy-branch-source-2')
                            traits {
                                gitBranchDiscovery()

                                headRegexFilter
                                {
                                    regex('^(?!.*main).*$')
                                }
                            }
                        }
                    }
                    strategy {
                        allBranchesSame {
                            props {
                                suppressAutomaticTriggering {
                                    if (main_branch) {
                                        triggeredBranchesRegex ('^(.*main).*$')
                                    }
                                    else {
                                        triggeredBranchesRegex ('^$.')
                                    }
                                }
                            }
                        }
                    }
                }
            }
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(0)
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath("Jenkinsfile")
                }
            }
            triggers {
                periodicFolderTrigger {
                    interval("1h")
                }
            }
        }
    }
    catch (Exception ex)
    {
        println("createPipelineRoot() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean createJobTestFolder()
{
    try
    {
        folder("/${job_testing_folder}")
        {
            displayName("010 - Testing Area Root")
            description("Spot to test job dsl code prior to delivery")
        }

        /*pipelineJob("${path_prefix}/${pipeline_root_folder}/${job_testing_folder}/branch-cleanup") {
            displayName("000 - Branch Cleanup")
            description("Run to clean up all branches without an active remote origin")

            if (branch_name != delivery_branch) {
                disabled()
            }

            definition {
                cpsScm {
                    scm {
                        git {
                            remote { url(tools_url) }
                            branches(branch_name)
                            scriptPath('job-automation/jenkinsfiles/branch-cleanup/Jenkinsfile')
                            extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                        }
                    }
                }
            }
        }*/
    }
    catch (Exception ex)
    {
        println("createJobTestFolder() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean main()
{
    boolean is_delivery_branch = branch_name == delivery_branch
    String path_prefix = getPathPrefix(is_delivery_branch)

    boolean create_root_result = createPipelineRootFolder(path_prefix)
    if (create_root_result == true) {
        println("Create pipeline root folder: SUCCESS")
        boolean create_deploy_job_result = createDeployJob(path_prefix)
        if (create_deploy_job_result)
        {
            println("Create deploy job: SUCCESS")
        }
        else
        {
            println("Create deploy job: FAILURE")
            return false
        }

        boolean create_job_testing_result = createJobTestFolder()
        if (create_job_testing_result)
        {
            println("Create job testing folder: SUCCESS")
        }
        else
        {
            println("Create job testing folder: FAILURE")
            return false
        }
    }
    else
    {
        println("Create pipeline root folder: FAILURE")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}





