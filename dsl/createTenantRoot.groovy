import groovy.transform.Field

Map mergeMaps(Map lhs, Map rhs) {
    // Shallow copy so we dont modify the data of the arguments
    Map result = lhs.getClass().newInstance(lhs)
    rhs.each { k, v ->
        result[k] = (lhs[k] in Map ? mergeMaps(result[k], v) : v)
    }
    return result
}

def buildPermissionMatrix(Map jenkinsRoleMap, Map ldapRoleMap) {
    def result = []

    // Build a list in the format jenkins expects for a permission matrix plugin. Ex: GROUP:<Jenkins Permission>:<Ldap Group>
    ldapRoleMap.each { curRoleName, curLdapGroup ->
        def jenkinsPermissionList = jenkinsRoleMap.get(curRoleName)
        if (jenkinsPermissionList != null) {
            jenkinsPermissionList.each { curJenkinsPerm ->
                result.add("GROUP:${curJenkinsPerm}:${curLdapGroup}")
            }
        }
    }
    return result
}

def createTenantFolder(String tenantKey) {
    def folderPath = "${pathPrefix}/${tenantKey}"
}


boolean main()
{
    try {
        createTenantFolder('test')
        /*boolean create_tenant_jobs_result = createTenantJobs()

        if (create_tenant_jobs_result == true) {
            println("Create tenant jobs: SUCCESS")
        } else {
            println("Create tenant jobs: FAILURE")
            return false
        }*/
    } catch (Exception ex) {
        println("Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("createTenantRoot.groovy - Execution FAILURE")
}




