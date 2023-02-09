import org.yaml.snakeyaml.Yaml

folder('test folder')
folder('test folder2')

println("Workspace: ${workspace}")

def config_str = readFile("${workspace}/seed_jobs/config.yaml")

println("Config Raw: ${config_str}")
//def yaml = new org.yaml.snakeyaml.Yaml(new FileReader("${workspace}/seed_jobs/config.yaml"))
//println("YAML: ${yaml}")

