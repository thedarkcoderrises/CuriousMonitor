def containerId=""
pipeline {
    agent none
    stages {
        stage('Build CuriousMonitor') {
            agent {
                    docker {
                        image 'maven:3-alpine'
                        args '-v /root/.m2:/root/.m2'
                    }
                  }
            steps {
                    sh 'mvn -X clean install -DskipTests'
                  }
            }

        stage('Staging CuriousMonitor Image') {
            agent any
            steps{
                    script{
                        containerId = sh (
                        script :'docker ps -aqf "name=curious"',
                        returnStdout: true
                        ).trim()
                        if("${containerId}"!= ""){
                          sh 'docker stop curious'
                          sh 'docker rm curious'
                          sh 'docker rmi $(docker images --filter=reference=curious --format "{{.ID}}")'
                        }
                    }
                    sh 'docker build -t curious:1.0 .'
                }
              }
        stage('Containerising CuriousMonitor') {
          agent any
           steps {
                   sh 'docker run -d -v /home/ec2-user/logs:/logs -e HOST_NAME=socat -e http_port=8082 --name curious --link=socat curious:1.0'
                 }
         }
    }
 }