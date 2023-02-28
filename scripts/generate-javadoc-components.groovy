import groovy.json.*;

// define sort order for plugins
def keyComparator = [compare: { e1, e2 -> e1.name.compareToIgnoreCase(e2.name) }] as Comparator

def components = new ArrayList<Artifact>();
components.addAll(Arrays.asList(
    new Artifact("Acceptance Test Harness", "org.jenkins-ci", "acceptance-test-harness", null, "https://github.com/jenkinsci/acceptance-test-harness"),
    new Artifact("Bridge Method Annotation", "com.infradna.tool", "bridge-method-annotation", null, "https://github.com/jenkinsci/bridge-method-injector"),
    new Artifact("Bridge Method Injector", "com.infradna.tool", "bridge-method-injector", null, "https://github.com/jenkinsci/bridge-method-injector"),
    new Artifact("Docker fixtures", "org.jenkins-ci.test", "docker-fixtures", null, "https://github.com/jenkinsci/docker-fixtures"),
    new Artifact("File Leak Detector", "org.kohsuke", "file-leak-detector", null, "https://github.com/jenkinsci/lib-file-leak-detector"),
    new Artifact("GitHub API", "org.kohsuke", "github-api", null, "http://github-api.kohsuke.org/"),
    new Artifact("Jenkins Test Harness", "org.jenkins-ci.main", "jenkins-test-harness", null, "https://github.com/jenkinsci/jenkins-test-harness"),
    new Artifact("Test Annotations", "org.jenkins-ci", "test-annotations", null, "https://github.com/jenkinsci/lib-test-annotations"),
    new Artifact("Memory Monitor Lib", "org.jenkins-ci", "memory-monitor", null, "https://github.com/jenkinsci/extras-memory-monitor"),
    new Artifact("Remoting", "org.jenkins-ci.main", "remoting", null, "https://github.com/jenkinsci/remoting"),
    new Artifact("Stapler", "org.kohsuke.stapler", "stapler", null, "https://github.com/jenkinsci/stapler"),
    new Artifact("Task Reactor Lib", "org.jenkins-ci", "task-reactor", null, "https://github.com/jenkinsci/lib-task-reactor"),
    new Artifact("Version Number Lib", "org.jenkins-ci", "version-number", null, "https://github.com/jenkinsci/lib-version-number"),
    new Artifact("Crypto Util Lib", "org.jenkins-ci", "crypto-util", null, "https://github.com/jenkinsci/lib-crypto-util"),
    new Artifact("Annotation Indexer Lib", "org.jenkins-ci", "annotation-indexer", null, "https://github.com/jenkinsci/lib-annotation-indexer"),
    new Artifact("Access Modifier Annotation", "org.kohsuke", "access-modifier-annotation", null, "https://github.com/jenkinsci/lib-access-modifier"),
    new Artifact("Access Modifier Checker", "org.kohsuke", "access-modifier-checker", null, "https://github.com/jenkinsci/lib-access-modifier"),
    new Artifact("Access Modifier Suppressions", "org.kohsuke", "access-modifier-suppressions", null, "https://github.com/jenkinsci/lib-access-modifier"),
    new Artifact("Commons Jelly", "org.jenkins-ci", "commons-jelly", null, "https://github.com/jenkinsci/jelly"),
    new Artifact("Mock JavaMail", "org.jvnet.mock-javamail", "mock-javamail", null, "https://github.com/jenkinsci/lib-mock-javamail"),
    new Artifact("Support Log Formatter", "io.jenkins.lib", "support-log-formatter", null, "https://github.com/jenkinsci/lib-support-log-formatter"),
    new Artifact("Symbol Annotation", "org.jenkins-ci", "symbol-annotation", null, "https://github.com/jenkinsci/lib-symbol-annotation"),
    new Artifact("WinP", "org.jvnet.winp", "winp", null, "https://github.com/jenkinsci/winp"),
    new Artifact("Winstone", "org.jenkins-ci", "winstone", null, "https://github.com/jenkinsci/winstone")
))


// For each plugin located in the update center
def indexBuilder = new JavadocGroupBuilder("component", "component", "Jenkins Components Javadoc");
components.toSorted(keyComparator).eachWithIndex { value, idx ->
    indexBuilder.withArtifact(value)
}

// Build all
indexBuilder.build()



