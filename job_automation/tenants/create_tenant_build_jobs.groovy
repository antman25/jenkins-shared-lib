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

boolean createTentantBuildJobs(String path_prefix)
{
    if (config_yaml.containsKey('tenants') == true)
    {
        def tenants = config_yaml['tenants']

        try
        {
            tenants.each { cur_tenant ->
                def name = cur_tenant.get('name')
                def project_list = cur_tenant.get('project_list')
                if (name != null)
                {
                    def path = "${path_prefix}/${name}"

                }
                else
                {
                    println("createTentantBuildJobs(): No tenant name defined")
                }
            }
        }
        catch (Exception ex)
        {
            println("createTentantBuildJobs(): Exception ${ex.toString()}")
            return false
        }
    }
    else
    {
        println("createTentantBuildJobs(): Missing tenants key")
        return false
    }
    return true
}

boolean main()
{
    try
    {
        String path_prefix = getPathPrefix(branch_name, delivery_branch)
        boolean create_tenant_buildjob_result = createTentantBuildJobs(path_prefix)
        if (create_tenant_buildjob_result)
        {
            println("Create Tenant Build Job: SUCCESS")
        }
        else
        {
            println("Create Tenant Build Job: FAILURE")
            return false
        }

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


