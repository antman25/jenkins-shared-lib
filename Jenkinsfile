podTemplate {
    node(POD_LABEL)
    {
        stage('clone')
        {
            checkout scm
        }

        stage('Env Dump')
        {
            sh ('env | sort -n')
        }

        stage ('Env Dump2')
        {
            def env_test = env.getEnvironment()

            env_test.each { cur_var ->
                println("CurVar: ${cur_var}")
                //println("Key: ${key}  Value: ${env_test[key]}")
            }
        }

        stage ('test stage')
        {
            echo "blah"
        }
    }
}

