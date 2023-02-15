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

boolean createTestingRootFolder()
{
    try
    {
        folder("${path_prefix}/${job_testing_folder}")
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

boolean createJobTestingRootFolder()
{
    try
    {
        folder("/${job_testing_folder}")
        {
            displayName("000 - Pipeline Job Testing")
            description("Spot to test job dsl and Jenkinsfiles code prior to delivery")
        }

    }
    catch (Exception ex)
    {
        println("createJobTestingRootFolder() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean main()
{
    boolean is_delivery_branch = branch_name == delivery_branch
    String path_prefix = getPathPrefix(is_delivery_branch)



    boolean create_job_test_root_result = createJobTestingRootFolder()
    if (create_job_test_root_result)
    {
        println("Create Job Testing Root: SUCCESS")
    }
    else
    {
        println("Create Job Testing Root: FAILURE")
        return false
    }

    boolean create_root_result = createPipelineRootFolder(path_prefix)
    if (create_root_result == true) {
        println("Create pipeline root folder: SUCCESS")
    }
    else
    {
        println("Create pipeline root folder: FAILURE")
        return false
    }

    /*boolean create_deploy_job_result = createDeployJob(path_prefix)
    if (create_deploy_job_result)
    {
        println("Create deploy job: SUCCESS")
    }
    else
    {
        println("Create deploy job: FAILURE")
        return false
    }

    */
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}





