String sanitizeBranchName(String branch_name) {
  return branch_name.replaceAll(/[^\w]/, '-').toLowerCase()
}

Map envVarExists(String key) {
  return env.getProperty(key) != null
}

String getShortCommit()
{
  return sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
}


boolean toBoolean(def value) {
  return (value instanceof java.lang.String) ? value.toBoolean() : value
}

String getPathPrefix(String branch_name, String delivery_branch)
{
  if (branch_name == delivery_branch)
  {
    return ""
  }
  else
  {
    def pipeline_root = env.getEnvironment().getOrDefault('PIPELINE_ROOT', 'pipeline-root')
    def job_testing_root = env.getEnvironment().getOrDefault('JOB_TESTING_ROOT', 'job-testing')
    return "${pipeline_root}/${job_testing_root}/${branch_name}"
  }
}

def default_stages(Closure body)
{
  return {
            stage ('Clone Code')
            {
                checkout scm
            }

            stage ('Additional Setup')
            {

            }
            body()

            stage ('Code Scan')
            {

            }

            stage ('Post Cleanup')
            {

            }
}

Map getConfig(key = null) {
    String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
    String branch_name_safe = utils.sanitizeBranchName(branch_name)
    String delivery_branch = env.getEnvironment().getOrDefault('DELIVERY_BRANCH', 'main')
    String chart_path = env.getEnvironment().getOrDefault('CHART_PATH', './helm')
    String dockerfile_path = env.getEnvironment().getOrDefault('DOCKERFILE_PATH', '.')
    String agent_pvc_name = env.getEnvironment().getOrDefault('AGENT_PVC_NAME', '.')
    String pipeline_root = env.getEnvironment().getOrDefault('PIPELINE_ROOT', 'pipeline-root')


  def isDeliveryBranch = branch_name == delivery_branch

  def config = [   branch_name : branch_name,
                                branch_name_safe : branch_name_safe,
                                branchDelivery: branchDelivery,
                                isDeliveryBranch: isDeliveryBranch,
                                chartPath : chart_path,
                                dockerfilePath: dockerfile_path,
                                agentPvcName: agent_pvc_name,
                                folderPipelineRoot: pipeline_root
                            ]

  /*def config = [

    apiTestsPath: env.API_TESTS_PATH ?: 'tests/api',
    chartPath: env.CHART_PATH ?: './helm',
    cleanupDeploy: envVarExists('CLEANUP_DEPLOY') ? env.CLEANUP_DEPLOY : true,
    codeverosChartPath: env.CODEVEROS_CHART_PATH ?: 'charts/codeveros',

    environment: env.ENVIRONMENT ?: 'ephemeral',
    externalIp: env.EXTERNAL_IP,
    functionalTestsPath: env.FUNCTIONAL_TESTS_PATH ?: 'tests/selenified',
    gitCredentials: env.GIT_CREDENTIALS ?: 'codeveros-gitlab-ssh',
    helmCredentials: env.HELM_CREDENTIALS ?: 'docker-registry-login',
    helmRepoUrl: env.HELM_REPO_URL,

    mavenConfigId: env.MAVEN_CONFIG_ID ?: 'globalmaven',
    namespace: env.NAMESPACE ?: "codeveros-${UUID.randomUUID().toString()}",
    nexusHelm: envVarExists('NEXUS_HELM') ? toBoolean(env.NEXUS_HELM) : true,
    pushBranchTag: envVarExists('PUSH_BRANCH_TAG') ? toBoolean(env.PUSH_BRANCH_TAG) : !isMasterBranch,
    pushChartOverrides: envVarExists('PUSH_CHART_OVERRIDES') ? toBoolean(env.PUSH_CHART_OVERRIDES) : isMasterBranch,
    pushLatestTag: envVarExists('PUSH_LATEST_TAG') ? toBoolean(env.PUSH_LATEST_TAG) : isMasterBranch,
    registry: env.DOCKER_REGISTRY,
    registryCredentialId: env.DOCKER_CREDENTIALS ?: 'docker-registry-login',
    regressionTestsPath: env.REGRESSION_TESTS_PATH ?: 'tests/selenified',
    releaseName: env.RELEASE_NAME ?: 'codeveros',
    repository: env.DOCKER_REPOSITORY,
    servicePath: env.SERVICE_PATH ?: './',
    tag: env.DOCKER_TAG,
    zapUrl: env.ZAP_URL ?: 'localhost:5000'
  ]*/

  return key ? config[key] : config

}
