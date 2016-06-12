node {
  stage 'checkout'
  checkout scm

  stage 'test'
  sh './gradlew clean test'

  stage 'build'
  sh './gradlew build'

  stage 'archive'
  step([$class: 'ArtifactArchiver', artifacts: 'build/libs/*.jar', fingerprint: true])

  stage 'reports'
  step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/*.xml'])
}
