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
                {
                    properties {
                        authorizationMatrix {
                            inheritanceStrategy { nonInheriting() }
                            permissions ( ['hudson.model.Item.Read:groupA',
                                           'hudson.model.Item.Cancel:groupA',
                                           'hudson.model.Item.Build:groupA',
                                           'hudson.model.Item.Workspace:groupA'] )
                        }

                    }


                }


                if (project_list != null)
                {
                    project_list.each { cur_project ->
                        println("Project: ${cur_project}")
                        folder("${name}/${cur_project}")

                        folder("${name}/${cur_project}/Builds")
                        folder("${name}/${cur_project}/Sandbox")

                        pipelineJob("${name}/${cur_project}/Sandbox/TestJob") {
                            displayName('Seed job')

                            def repo = 'https://github.com/antman25/jenkins-shared-lib.git'

                            description("Seed Job")

                            definition {
                                cpsScm {
                                    scm {
                                        git {
                                            remote { url(repo) }
                                            branches("main")
                                            scriptPath('Jenkinsfile')
                                            extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                                        }

                                    }
                                }
                            }
                        }
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


