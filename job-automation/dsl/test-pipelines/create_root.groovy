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

boolean createTestPipelineRoot(String path_prefix)
{
    try {
        def pipeline_test_root ="${path_prefix}/${TEST_PIPELINE_PATH}"
        folder(pipeline_test_root)
        {
            displayName ('030 - SharedLib Test Pipelines')
            description("Job location of shared-lib tests")
        }
    }
    catch (Exception ex) {
        println("createTestPipelineRoot() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean main()
{
    String path_prefix = getPathPrefix(branch_name, delivery_branch)
    boolean is_delivery_branch = branch_name == delivery_branch

    boolean create_test_pipeline_root = createTestPipelineRoot(path_prefix)
    if (create_test_pipeline_root) {
        println("Create Test Pipeline Root: SUCCESS")
    }
    else
    {
        println("Create Test Pipeline Root: FAILURE")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}


