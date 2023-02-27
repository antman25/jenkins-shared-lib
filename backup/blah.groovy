if (config_yaml.containsKey('tenants') == true) {
    def tenants = config_yaml['tenants']

    tenants.each { cur_tenant ->
        def tenant_name = cur_tenant.get('tenant_name')
        def perm_groups = cur_tenant.get('perm_groups')
        if (tenant_name != null) {
            def tenant_root_path = "${pathPrefix}/${tenant_name}"
            boolean create_root_folder_result = createTenantFolder (pathPrefix, tenant_name, perm_groups)
            if (create_root_folder_result == true) {
                boolean create_buildjobs_root_result = createTentantBuildRoot(tenant_root_path)

                if (create_buildjobs_root_result) {
                    def project_list = cur_tenant.get('project_list')
                    if (project_list != null) {
                        boolean create_buildjobs_project = true
                        project_list.each { cur_project ->
                            create_buildjobs_project |= createTentantProjectFolder(tenant_root_path, bitbucket_url, cur_project, tenant_name)
                        }

                        if (create_buildjobs_project == true) {
                            println("createTenantRoot(): Create project build folder: SUCCESS")
                        }
                        else {
                            println("createTenantRoot(): Create project build folder: FAILURE")
                            return false
                        }

                    } else  {
                        println("createTenantRoot(): Project List Empty")
                        return false
                    }
                }
            }
            else {
                println("createTenantRoot(): failed to create build job root")
                return false
            }
        }
        else {
            println("createTenantRoot(): No tenant name defined")
            return false
        }
    }

}
else {
    println("createTenantRoot(): Missing tenants key in config")
    return false
}
