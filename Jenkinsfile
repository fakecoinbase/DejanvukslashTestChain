pipeline {
    agent {
        docker {
            image 'maven:latest'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage('Build') {
            steps {
                dir("api") {
                    sh 'mvn -B -DskipTests clean package'
                }
            }
        }
        stage('Test') {

            steps {
                dir("api") {
                    sh 'mvn test'
                }
            }
            post {
                 always {
                    junit '**/target/surefire-reports/*.xml'
                 }
            }
        }
    }
}