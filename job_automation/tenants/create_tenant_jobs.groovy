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
            println("createTestBranchFolder(): Skipping folder creation for root")
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
        boolean  create_test_branch_folder = createTestBranchFolder(path_prefix)
        if (create_test_branch_folder == true) {
            println("Create test branch folder: SUCCESS")

            if (config_yaml.containsKey('tenants') == true) {
                def tenants = config_yaml['tenants']

                tenants.each { cur_tenant ->
                    def tenant_name = cur_tenant.get('tenant_name')
                    def perm_groups = cur_tenant.get('perm_groups')
                    if (tenant_name != null) {
                        def tenant_root_path = "${path_prefix}/${tenant_name}"
                        boolean create_root_folder_result = createTenantFolder (tenant_root_path, perm_groups)
                        if (create_root_folder_result == true) {
                            boolean create_buildjobs_root_result = createTentantBuildRoot(tenant_root_path)
                        }
                        else {
                            println("createTenantJobs(): No tenant name defined")
                            return false
                        }
                    }
                    else {
                        println("createTenantJobs(): No tenant name defined")
                        return false
                    }
                }

            }
            else {
                println("createTenantJobs(): Missing tenants key in config")
                return false
            }
        }
        else {
            println("createTenantJobs(): Failed to create test branch folder")
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
        println("Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution FAILURE")
}




