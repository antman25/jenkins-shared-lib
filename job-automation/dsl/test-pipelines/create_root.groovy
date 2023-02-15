import groovy.transform.Field
@Field final String VALIDIATION_PATH = 'validation'

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

boolean createValidiationRoot(String path_prefix)
{
    try {
        def validation_root ="${path_prefix}/${VALIDIATION_PATH}"
        folder(validation_root)
        {
            displayName ('030 - Smoketests')
            description("Job location of shared-lib smoke tests")
        }
    }
    catch (Exception ex) {
        println("createSmoktestRoot() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean main()
{
    String path_prefix = getPathPrefix(branch_name, delivery_branch)
    boolean create_smoketest_root = createSmoktestRoot(path_prefix)
    if (create_smoketest_root) {
        println("Create Smoketest Root: SUCCESS")
    }
    else
    {
        println("Create Smoketest Root: FAILURE")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}


