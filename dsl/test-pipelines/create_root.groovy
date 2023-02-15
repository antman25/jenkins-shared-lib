import groovy.transform.Field
@Field final String TEST_PIPELINE_PATH = 'test-pipeline'

String getPathPrefix(boolean is_delivery_branch)
{
    if (is_delivery_branch == true) {
        return ""
    }
    else {
        return "/${job_testing_folder}/${branch_name}"
    }
}

boolean main() {
    boolean is_delivery_branch = branch_name == delivery_branch
    String path_prefix = getPathPrefix(is_delivery_branch)

    try {
        folder("${path_prefix}/${TEST_PIPELINE_PATH}") {
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

boolean result = main()

if (result == false) {
    throw new Exception("Execution: FAILURE")
}


