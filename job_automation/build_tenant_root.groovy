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

boolean createRestrictedFolder(String path, List<String> perm_groups)
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
            println("createRestrictedFolder(): Permission groups was null")
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
        println("createRestrictedFolder() Exception: ${ex.toString()}")
        return false
    }
    return true
}



boolean buildTentantRoot(String path_prefix, HashMap config_data)
{
    if (config_data.containsKey('tenants') == true)
    {
        def tenants = config_data['tenants']

        try
        {
            tenants.each { cur_tenant ->
                def name = cur_tenant.get('name')
                def project_list = cur_tenant.get('project_list')
                def perm_groups = cur_tenant.get('perm_groups')
                if (name != null)
                {
                    def path = "${path_prefix}/${name}"
                    def create_folder_result = createRestrictedFolder (path, perm_groups)
                    if (create_folder_result == false)
                        return false
                }
                else
                {
                    println("buildTentantRoot(): No tenant name defined")
                }
            }
        }
        catch (Exception ex)
        {
            println("buildTentantRoot(): Exception ${ex.toString()}")
            return false
        }
    }
    else
    {
        println("buildTentantRoot(): Missing tenants key")
        return false
    }
    return true
}

boolean createTestBranchFolder(String branch_name, String delivery_branch)
{
    try
    {
        String path_prefix = getPathPrefix(branch_name, delivery_branch)
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

void main()
{
    try
    {
        def config_yaml = new Yaml().load(config_data)
        boolean create_test_path = createTestBranchFolder(branch_name, delivery_branch)
        if (create_test_path)
        {
            println("Create branch folder SUCCESS")
            String path_prefix = getPathPrefix(branch_name, delivery_branch)
            boolean build_tenant_root = buildTentantRoot(path_prefix, config_yaml)

            if (build_tenant_root == true)
            {
                println("Tenant folder creation SUCCESS")
            }
            else
            {
                println("Tenant folder creation FAILURE")
            }
        }
        else
        {
            println("Create branch folder FAILURE")
        }
    }
    catch (Exception ex)
    {
        println("build_tenant_root.groovy main() Exception: ${ex.toString()}")
    }
}

main()


