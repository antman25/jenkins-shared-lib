boolean main()
{
    try
    {
        if (isPrimaryBranch == false)
        {
            folder("/${job_testing_folder}/${branch_name}")
            {
                displayName("${branch_name_raw}")
                description("Job root for branch: ${branch_name_raw}")
            }
        }

        folder("${pathPrefix}/${job_testing_folder}")
        {
            displayName("000 - Testing Area")
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
