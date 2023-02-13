def call(Map config) {
  container('docker') {
    def commitTag = config.isMainBranch ? config.commitShortHash : "${config.branchNameSafe}-${config.commitShortHash}"

    try {
      stage('Build Docker Image') {

      }
    } catch(err) {
      echo 'Error building and pushing Docker image'
      throw err
    } finally {
      echo 'Delete created Docker images from local registry'


    }
    return [name: "${config.registry}/${config.repository}", tag: commitTag]
  }
}
