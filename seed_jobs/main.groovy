import org.yaml.snakeyaml.Yaml


void buildJobs(config_data)
{
    if (config_data.containsKey('groups') == true)
    {
        def groups = config_data['groups']
        groups.each { cur_group ->
            def name = cur_group.get('name')
            def project_list = cur_group.get('project_list')
            if (name != null)
            {
                println("GroupName: ${name}")
                folder("${name}")
                if (project_list != null)
                {
                    project_list.each { cur_project ->
                        println("Project: ${cur_project}")
                        folder("${name}/${cur_project}")
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
        }
    }
    else
    {
        println("Missing groups key")
    }


}

def config_yaml = new Yaml().load(config)
buildJobs(config_yaml)


