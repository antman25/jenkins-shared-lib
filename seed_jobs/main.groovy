import org.yaml.snakeyaml.Yaml

folder('test folder')
folder('test folder2')

println("Workspace: ${workspace}")

//def config_path = "${workspace}/seed_jobs/config.yaml".toString()

println("Config Raw: ${config}")

Yaml parser = new Yaml()
List config_data = parser.load(config)


println("YAML: ${config_data}")

