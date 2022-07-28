pipeline{
    agent {
        label 'maven'
    }
    environment{
        OCP_PROJECT = '77c02f-test'
        IMAGE_PROJECT = '77c02f-tools'
        IMAGE_TAG = 'latest'
        APP_SUBDOMAIN_SUFFIX = '77c02f-test'
        APP_DOMAIN = 'apps.silver.devops.gov.bc.ca'
        TAG = 'test'
        REPO_NAME = 'educ-rule-engine-api'
        ORG = 'bcgov'
        BRANCH = 'main'
        SOURCE_REPO_URL = 'https://github.com/${ORG}/${REPO_NAME}'
        SOURCE_REPO_URL_RAW = 'https://raw.githubusercontent.com/${ORG}/${REPO_NAME}'
        MIN_CPU = "20m"
        MAX_CPU = "250m"
        MIN_MEM = "100mi"
        MAX_MEM = "400mi"
        MIN_REPLICAS = "1"
        MAX_REPLICAS = "3"
    }
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }
    parameters {
        choice( name: 'IMAGE_TAG', choices: ['latest', 'main', 'dev', 'test' ] )
    }
    stages{
        stage('Deploy to TEST') {
            steps{
                script {
                    openshift.withCluster() {
                        openshift.withProject(OCP_PROJECT) {
                            openshift.apply(
                                    openshift.process("-f", "${SOURCE_REPO_URL_RAW}/${BRANCH}/tools/openshift/api.dc.yaml",
                                            "REPO_NAME=${REPO_NAME}", "HOST_ROUTE=${REPO_NAME}-${APP_SUBDOMAIN_SUFFIX}.${APP_DOMAIN}",
                                            "TAG_NAME=${params.IMAGE_TAG}", "IS_NAMESPACE=${IMAGE_PROJECT}", "MIN_CPU=${MIN_CPU}", "MAX_CPU=${MAX_CPU}", "MIN_MEM=${MIN_MEM}", "MAX_MEM=${MAX_MEM}", "MIN_REPLICAS=${MIN_REPLICAS}", "MAX_REPLICAS=${MAX_REPLICAS}")
                            )
                            openshift.selector("dc", "${REPO_NAME}-dc").rollout().latest()
                            timeout (time: 10, unit: 'MINUTES') {
                                openshift.selector("dc", "${REPO_NAME}-dc").rollout().status()
                            }
                        }
                    }
                }
            }
            post{
                success {
                    echo "${REPO_NAME} successfully deployed to TEST"
                    script {
                        openshift.withCluster() {
                            openshift.withProject(IMAGE_PROJECT) {
                                echo "Tagging image"
                                openshift.tag("${IMAGE_PROJECT}/${REPO_NAME}:latest", "${REPO_NAME}:${TAG}")
                            }
                        }
                    }
                }
                failure {
                    echo "${REPO_NAME} deployment to TEST Failed!"
                }
            }
        }
    }
}
