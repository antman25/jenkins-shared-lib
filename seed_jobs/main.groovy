import org.yaml.snakeyaml.Yaml

folder('test folder')

def yaml = new org.yaml.snakeyaml.Yaml(new FileReader('config.yaml'))
println("YAML: ${yaml}")