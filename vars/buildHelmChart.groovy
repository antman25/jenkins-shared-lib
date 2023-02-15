import org.yaml.snakeyaml.Yaml

void dumpYaml(Map conf, String file) {
  Yaml yaml = new Yaml();
  try {
    Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    yaml.dump(conf, writer);
  } catch (Exception ex) {
    echo("Error: ${ex.toString()}");
  }
}

Map loadYaml(String file) {
  Yaml yaml = new Yaml();
  def result = [:]
  try {
    def data = readFile file
    print(data)
    result = new Yaml().load(data)
  } catch (Exception ex) {
    echo("Error: ${ex.toString()}");
  }
  return result
}


def call(Map config, String chart_root_path) {
  def chartProps = [:]
  stage('Build Helm Chart') {

    if (!fileExists("${chart_root_path}/Chart.yaml")) {
      echo "Helm chart not found at path: ${chart_root_path}/Chart.yaml"
      sh 'exit  1'
    }

    container('helm') {
      dir(chart_root_path) {
        sh 'mkdir test'
        sh 'pwd && find . && ls -latr'
        //chartProps = readYaml file: ''
        def chart_data = readFile 'Chart.yaml'
        chartProps = new Yaml().load(chart_data)
        //chartProps = loadYaml('Chart.yaml')

        chartProps.version = "${chartProps.version}-${config.dockerImageTag}"
        chartProps.appVersion = config.dockerImageTag
        println("ChartProps: ${chartProps}")
        //writeYaml file: 'Chart.yaml', data: chartProps, overwrite: true
        //FileWriter writerChart = new FileWriter("test/Chart2.yaml");
        //yaml.dump(chartProps, writerChart);

        //dumpYaml(chartProps, 'Chart.yaml')
        Writer writer = new OutputStreamWriter(new FileOutputStream('Chart.yaml'), "UTF-8");
        yaml.dump(chartProps, writer);

        //def valuesProps = readYaml file: 'values.yaml'
        //def values_data = readFile 'values.yaml'
        //def valuesProps = new Yaml().load(values_data)

        //valuesProps.image.repository = "testrepoval"
        //writeYaml file: 'values.yaml', data: valuesProps, overwrite: true
        //dumpYaml(valuesProps, 'values.yaml')
        sh "helm lint ."
      }


    }
    return [name: chartProps.name, version: chartProps.version, repository: config.helmRepoUrl]
  }
}

/*
// todo: bake this helm-push installation into custom Docker image, add push plugin
      sh '''
        apk --no-cache add git curl
        helm plugin install https://github.com/chartmuseum/helm-push
        apk del git
        rm -f /var/cache/apk/*
      '''

      withFolderProperties {
        withCredentials([
                usernamePassword(usernameVariable: '$REPO_USER', passwordVariable: 'REPO_PASS', credentialsId: config.helmCredentials)
        ]) {
          if (config.nexusHelm) {
            sh """
             helm dependency build ${config.chartPath}
             CHART_PACKAGE="\$(helm package ${config.chartPath} | cut -d":" -f2 | tr -d '[:space:]')"
             curl -u $REPO_USER:$REPO_PASS ${config.helmRepoUrl} --upload-file \$CHART_PACKAGE -v
            """
          } else {
            sh "helm repo add chartRepo ${config.helmRepoUrl} --username $REPO_USER --password $REPO_PASS"
            sh "helm push -d ${config.chartPath} chartRepo"
          }
        }
      }
 */