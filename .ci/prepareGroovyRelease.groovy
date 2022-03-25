/**
 * Prepares a release build:
 * -check release version number(s)
 * -checkout from repository
 * -set release version in maven modules
 * -create release commit +tag
 * -set next development version in maven modules
 * -create development commit + tag
 * -checkout release commit
 *
 * @param config the  map of configuration parameters:
 * -additionalModules : additional modules to be updated(if they are not children of the root module)
 * -rootModule : the parent module , must only be specified if the topmost folder is not the parent
 * -property : the property in in the pom . xml to update with the new version.The property is only applied in the root module.
 */
def call(Map config = [:]) {
    currentBuild.displayName = "Release ${params.RELEASE_VERSION} (${params.BRANCH})"

    assert params.RELEASE_VERSION ==~ /(\d+\.)+\d+(-rc\d\d|-m\d\d|-a\d{8}-\d\d|-[A-Z]+)?/
    assert params.DEVELOPMENT_VERSION ==~ /(\d+\.)+\d+/

    checkout([
            $class           : 'GitSCM',
            branches         : [[name: "${params.BRANCH}"]],
            extensions       : [[$class: 'WipeWorkspace'], [$class: 'LocalBranch']],
            userRemoteConfigs: scm.userRemoteConfigs
    ])

    // Remove and fetch all tags - necessary because there could a stale tag from a previous failed release
    sh "git tag -l | xargs git tag -d"
    sh "git fetch -t"

    String snapshotModifier = "SNAPSHOT"

    setVersion(params.RELEASE_VERSION)

    sh "git add . && git commit -m '[release] prepare release ${params.RELEASE_VERSION}' && git tag -a -m ${params.RELEASE_VERSION} ${params.RELEASE_VERSION}"

    setVersion("${params.DEVELOPMENT_VERSION}-${snapshotModifier}")

    sh "git add . && git commit -m '[release] prepare for next development iteration'"

    sh "git checkout HEAD~1"
}

def setVersion(String version) {
    def propertiesFile = readFile file: 'gradle.properties', encoding: 'UTF-8'
    def properties = new Properties()
    properties.load(new StringReader(propertiesFile))
    properties['version']=version
    properties.store(new StringWriter(propertiesFile))
}
