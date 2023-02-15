String getPathPrefix(boolean is_delivery_branch) {
    if (is_delivery_branch == true) {
        return ""
    }
    else {
        return "/${job_testing_folder}/${branch_name}"
    }
}

boolean main()
{
    boolean is_delivery_branch = branch_name == delivery_branch
    String path_prefix = getPathPrefix(is_delivery_branch)

    try
    {
        folder("${path_prefix}/${job_testing_folder}")
        {
            displayName("010 - Testing Area")
            description("This is where a mirrored version of all the jobs in a branch folder for testing purposes")

            properties {
                authorizationMatrix {
                    inheritanceStrategy { nonInheriting() }
                }
            }
        }

        if (branch_name != delivery_branch)
        {
            folder("${path_prefix}/${job_testing_folder}/${branch_name}")
            {
                displayName("${branch_name_raw}")
                description("Job root for branch: ${branch_name_raw}")
            }
        }
    }
    catch (Exception ex)
    {
        println("job-testing Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}