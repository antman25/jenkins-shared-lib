import org.yaml.snakeyaml.Yaml


void printConfig(config_data)
{
    if (config_data.containsKey('groups') == true)
    {
        def groups = config_data['groups']
        //println("Groups: ${groups}")

        groups.each { cur_group ->
            //println("CurGroups: ${current_group}")
            def name = cur_group.get('name')
            def project_list = cur_group.get('project_list')
            if (name != null)
            {
                println("GroupName: ${name}")

                if (project_list != null)
                {
                    project_list.each { cur_project ->
                        println("Project: ${cur_project}")
                    }
                }
                else
                {
                    println("No Projects Lists")
                }
            }
            else
            {
                println("No group name")
            }



            //println("Project List: ${project_list}")
        }
    }
    else
    {
        println("Missing groups key")
    }


}

def config_yaml = new Yaml().load(config)
printConfig(config_yaml)
println("YAML: ${config_yaml}")

