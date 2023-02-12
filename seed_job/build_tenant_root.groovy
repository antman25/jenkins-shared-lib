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
                    def total_permissions = []
                    perm_groups.each { cur_perm_group ->
                        println("Adding permission group: ${cur_perm_group} to path ${path}")
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

boolean buildTentantRoot(String branch_name, HashMap config_data)
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
                    createRestrictedFolder (name, perm_groups)
                }
                else
                {
                    println("buildTentantRoot(): No name defined")
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

def config_yaml = new Yaml().load(config_data)
def result = buildTentantRoot(branch_name, config_yaml)
if (result == true)
{
    println("Tenant folder creation SUCCESS")
}
else
{
    println("Tenant folder creation FAILURE")
}

