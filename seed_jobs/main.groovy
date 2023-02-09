import org.yaml.snakeyaml.Yaml

folder('test folder')
folder('test folder2')

println("Workspace: ${workspace}")

def config_path = "${workspace}/seed_jobs/config.yaml".toString()
def config_raw = readFile(config_path)

println("Config Raw: ${config_raw}")
//def yaml = new org.yaml.snakeyaml.Yaml(new FileReader("${workspace}/seed_jobs/config.yaml"))
//println("YAML: ${yaml}")

