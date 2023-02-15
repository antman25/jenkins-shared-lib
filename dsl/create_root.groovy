boolean createTestingRootFolder(String branch_name, String delivery_branch)
{
    try
    {
        folder("/${job_testing_folder}")
        {
            displayName("010 - Job Testing Area")
            description("This is where a mirrored version of all the jobs in a branch folder for testing purposes")

            properties {
                authorizationMatrix {
                    inheritanceStrategy { nonInheriting() }
                }
            }
        }

        if (branch_name != delivery_branch)
        {
            folder("/${job_testing_folder}/${branch_name}")
            {
                displayName("${branch_name_raw}")
                description("Job root for branch: ${branch_name_raw}")
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

boolean main()
{
    //boolean is_delivery_branch = branch_name == delivery_branch
    //String path_prefix = getPathPrefix(is_delivery_branch)

    boolean create_job_test_root_result = createTestingRootFolder(branch_name, delivery_branch)
    if (create_job_test_root_result) {
        println("Create Job Testing Root: SUCCESS")
    }
    else {
        println("Create Job Testing Root: FAILURE")
        return false
    }

    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}
