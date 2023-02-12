boolean createPipelineRootFolder()
{
    try
    {
        folder("/${pipeline_root_folder}")
        {
            displayName("Pipeline Admin")
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

boolean createDeployJob()
{
    try
    {
        def tools_url = getBinding().getVariables().getOrDefault('TOOLS_URL', 'NotSet')

        multibranchPipelineJob("/${pipeline_root_folder}/job_deploy")
        {
            displayName("000 - Deploy Jenkins Jobs")
            description("Runs all the JobDSL for job deployment")

            branchSources {
                git {
                    remote(tools_url)
                    // branch source id must be unique
                    id ('pipeline-root-job-deploy-branch-source')

                }
            }
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(0)
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath("job_automation/Jenkinsfile")
                }
            }
            properties{
                suppressFolderAutomaticTriggering {
                    // Defines a regular expression of branch names which will be triggered automatically, for example (?!
                    branches("(?!main.*)")
                    // Determines events for which branches with matched names should not be triggered automatically.
                    strategy("INDEXING")
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
        folder("${pipeline_root_folder}/${job_testing_folder}")
        {
            displayName("010 - Job DSL Testing Area")
            description("Spot to test job dsl code prior to delivery")
        }
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
    if (branch_name != delivery_branch)
    {
        println("Not on delivery branch, skipping job creation")
        return true
    }


    boolean create_root_result = createPipelineRootFolder()
    if (create_root_result == true)
    {

        boolean create_deploy_job_result = createDeployJob()
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
if (result == true)
{
    println("create_pipeline_jobs.groovy execution: SUCCESS")
}
else
{
    throw new Exception("create_pipeline_jobs.groovy execution: FAILURE")
}




