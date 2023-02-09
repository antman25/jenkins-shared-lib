import org.yaml.snakeyaml.Yaml

folder('test folder')
folder('test folder2')

def yaml = new org.yaml.snakeyaml.Yaml(new FileReader("${WORKSPACE}/seed_jobs/config.yaml"))
println("YAML: ${yaml}")