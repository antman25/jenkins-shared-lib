boolean createTestingRootFolder()
{
    try
    {
        folder("/${job_testing_folder}")
        {
            displayName("999 - Job Testing Area")
            description("This is where a mirrored version of all the jobs in a branch folder for testing purposes")

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

boolean main()
{
    boolean is_delivery_branch = branch_name == delivery_branch

    boolean create_job_test_root_result = createTestingRootFolder()
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
