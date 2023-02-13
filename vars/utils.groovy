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

def

Map getConfig(key = null) {
  // these are the configurable options that can be overridden by passing in config to the
  // run actions. Some also have corresponding environment variables.

  def mainBranch = env.MASTER_BRANCH ?: 'main'
  def isMainBranch = masterBranch == env.BRANCH_NAME

  def config = [
    chartPath: env.CHART_PATH ?: './helm',
    dockerfilePath: env.DOCKERFILE_PATH ?: '.',
    isMainBranch: isMainBranch,
    mainBranch: mainBranch,
    agentPvcName: env.AGENT_PVC_NAME ?: 'jenkins-agent-retain',
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
