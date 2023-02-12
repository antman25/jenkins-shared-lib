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

List<String> permissionDeveloper(String group)
{
    return ["hudson.model.Item.Read:${group}",
            "hudson.model.Item.Cancel:${group}",
            "hudson.model.Item.Build:${group}",
            "hudson.model.Item.Workspace:${group}"];
}

boolean createTestBranchFolder(String path_prefix)
{
    try
    {
        if (path_prefix != "")
        {
            folder(path_prefix)
            {
                displayName("${branch_name}")
                description("Root folder for Job testing. Branch: ${branch_name}")
            }
        }
        else
        {
            println("Root path - Skipping folder creation")
        }

    }
    catch (Exception ex)
    {
        println("createTestBranchFolder() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean createTenantFolder(String path, List<String> perm_groups)
{
    try
    {
        def total_permissions = []
        if (perm_groups != null)
        {
            perm_groups.each { cur_perm_group ->
                total_permissions.addAll(permissionDeveloper(cur_perm_group))
            }
            println("Path: ${path} -- Permission Groups: ${perm_groups.toString()}")
        }
        else
        {
            println("createTenantFolder(): Permission groups was null")
            return false
        }

        folder(path)
        {
            properties {
                authorizationMatrix {
                    inheritanceStrategy { nonInheriting() }
                    permissions ( total_permissions )
                }
            }
        }

    }
    catch (Exception ex)
    {
        println("createTenantFolder() Exception: ${ex.toString()}")
        return false
    }
    return true
}



boolean createTenantJobs() {
    try {
        String path_prefix = getPathPrefix(branch_name, delivery_branch)
        if (config_yaml.containsKey('tenants') == true) {
            def tenants = config_yaml['tenants']

            tenants.each { cur_tenant ->
                def tenant_name = cur_tenant.get('tenant_name')
                def perm_groups = cur_tenant.get('perm_groups')
                if (name != null) {
                    def tenant_root_path = "${path_prefix}/${tenant_name}"
                    boolean create_root_folder_result = createTenantFolder (tenant_root_path, perm_groups)
                    if (create_root_folder_result == true) {

                    }

                }
                else {
                    println("createTenantJobs(): No tenant name defined")
                }
            }
        }
        else {
            println("createTenantJobs(): Missing tenants key")
            return false
        }
    }
    catch (Exception ex) {
        println("createTenantJobs(): Exception ${ex.toString()}")
        return false
    }

    return true
}

boolean createTentantBuildRoot(String path)
{
    try {
        folder("${path}/builds")
        {
            displayName("Builds")
            description("Builds Root Folder")
        }
    }
    catch (Exception ex) {
        println("createTentantBuildRoot() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean main()
{
    try
    {
        boolean create_tenant_jobs_result = createTenantJobs()

        if (create_tenant_jobs_result == true)
        {
            println("Create tenant jobs: SUCCESS")
        }
        else
        {
            println("Create tenant jobs: FAILURE")
            return false
        }
    }
    catch (Exception ex)
    {
        println("create_tenant_jobs.groovy Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean result = main()
if (result == true)
{
    println("create_tenant_jobs.groovy execution SUCCESS")
}
else
{
    throw new Exception("create_tenant_jobs.groovy execution FAILURE")
}




