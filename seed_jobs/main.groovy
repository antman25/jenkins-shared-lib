import org.yaml.snakeyaml.Yaml

def config_data = new Yaml().load(config)

void printConfig()
{
    if (config_data.containsKey('groups') == true)
    {
        def groups = config_data['groups']
        println("Groups: ${groups}")

        groups.each { current_group ->
            println("CurGroups: ${current_group}")
            def name = current_group.get('name')
            def project_list = current_group.get('project_list')
            println("Group: ${name}")
            println("Project List: ${project_list}")
        }
    }
    else
    {
        println("Missing groups key")
    }


}


println("YAML: ${config_data}")

