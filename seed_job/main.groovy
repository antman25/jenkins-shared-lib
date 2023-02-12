import org.yaml.snakeyaml.Yaml

List<String> permissionDeveloper(String group)
{
    return ["hudson.model.Item.Read:${group}",
            "hudson.model.Item.Cancel:${group}",
            "hudson.model.Item.Build:${group}",
            "hudson.model.Item.Workspace:${group}"];
}

void createRestrictedFolder(String path, List<String> perm_groups)
{
    folder(path)
    {
        properties {
            authorizationMatrix {
                inheritanceStrategy { nonInheriting() }

                if (perm_groups != null)
                {
                    //println("perm_groups: ${perm_groups}")
                    def total_permissions = []
                    perm_groups.each { cur_perm_group ->
                        println("cur_perm_group: ${cur_perm_group}")
                        total_permissions.addAll(permissionDeveloper(cur_perm_group))
                    }
                    permissions ( total_permissions )
                }
                else
                {
                    permissions ()
                }
            }
        }
    }
}


void buildJobs(String branch_name, HashMap config_data)
{
    if (config_data.containsKey('groups') == true)
    {
        def groups = config_data['groups']
        groups.each { cur_group ->
            def name = cur_group.get('name')
            def project_list = cur_group.get('project_list')
            def perm_groups = cur_group.get('perm_groups')
            if (name != null)
            {
                println("GroupName: ${name}")
                createRestrictedFolder (name, perm_groups)



            }
            else
            {
                println("No group name")
            }
        }
    }
    else
    {
        println("Missing groups key")
    }


}

println("Config Dump:\n${config}")

//def config_yaml = new Yaml().load(config)
//buildJobs(config_yaml)
buildJobs(config)

