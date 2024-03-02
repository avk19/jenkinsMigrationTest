pipeline {
    parameters {
        choice(name: 'JDK_VERSION', choices: ['8', '11'], description: 'Select JDK version')
        choice(name: 'ANT_VERSION', choices: ['1.9.14', '1.10.11'], description: 'Select Ant version')
        choice(name: 'MAVEN_VERSION', choices: ['3.6.3', '3.8.1'], description: 'Select Maven version')
        string(name: 'GIT_BRANCH', defaultValue: 'master', description: 'Git branch name')
        string(name: 'GIT_TAG', defaultValue: 'v1.0.0', description: 'Git tag name')
        string(name: 'DATABASE_NAME', defaultValue: 'your_database_name', description: 'Database name')
        choice(name: 'NEXUS_BUILD_TYPE', choices: ['snapshot', 'release'], description: 'Nexus build type')
        credentials(name: 'GIT_CREDENTIALS', defaultValue: 'your_git_credentials_id', description: 'Git credentials')
        credentials(name: 'NEXUS_CREDENTIALS', defaultValue: 'your_nexus_credentials_id', description: 'Nexus credentials')
        string(name: 'EMAIL_RECIPIENTS', defaultValue: 'your_email@example.com', description: 'Email recipients (comma-separated)')
    }

    agent any

    environment {
        JDK_HOME = tool "JDK-${params.JDK_VERSION}"
        ANT_HOME = tool "Ant-${params.ANT_VERSION}"
        MAVEN_HOME = tool "Maven-${params.MAVEN_VERSION}"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${params.GIT_BRANCH}"]],
                        userRemoteConfigs: [[url: credentials(params.GIT_CREDENTIALS).url]]
                    ])
                }
            }
        }

        stage('Build with Ant') {
            steps {
                script {
                    def antCMD = "${ANT_HOME}/bin/ant"
                    sh "${antCMD} -f build.xml -Ddatabase.name=${params.DATABASE_NAME}"
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    def mavenCMD = "${MAVEN_HOME}/bin/mvn"
                    sh "${mavenCMD} deploy -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true"
                }
            }
        }

        stage('Create Git Tag') {
            steps {
                script {
                    sh "git tag -a ${params.GIT_TAG} -m 'Release ${params.GIT_TAG}'"
                    sh 'git push origin ${params.GIT_TAG}'
                }
            }
        }
    }

    post {
        success {
            emailext subject: "Build Success: ${currentBuild.fullDisplayName}",
                     body: "The build was successful. Git Tag: ${params.GIT_TAG}",
                     to: params.EMAIL_RECIPIENTS
        }

        failure {
            emailext subject: "Build Failure: ${currentBuild.fullDisplayName}",
                     body: "The build failed. Please check the Jenkins logs for details.",
                     to: params.EMAIL_RECIPIENTS
        }
    }
}
