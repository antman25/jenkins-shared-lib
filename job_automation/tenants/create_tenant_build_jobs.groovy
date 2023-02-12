import org.yaml.snakeyaml.Yaml

String getPathPrefix(String branch_name, String delivery_branch)
{
    if (branch_name == delivery_branch)
    {
        return ""
    }
    else
    {
        return "${pipeline_root_folder}/${job_testing_folder}/${branch_name}"
    }
}


boolean main()
{
    try
    {


    }
    catch (Exception ex)
    {
        println("create_tenant_build_jobs.groovy main() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean result = main()
if (result == true)
{
    println("create_tenant_build_jobs.groovy execution SUCCESS")
}
else
{
    throw new Exception("create_tenant_build_jobs.groovy execution FAILURE")
}


