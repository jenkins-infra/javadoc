import groovy.json.*;

// define sort order for plugins
def keyComparator = [compare: { e1, e2 -> e1.name.compareToIgnoreCase(e2.name) }] as Comparator

def components = new ArrayList<Artifact>();
components.addAll(Arrays.asList(
    new Artifact("Acceptance Test Harness", "org.jenkins-ci", "acceptance-test-harness", null, "https://github.com/jenkinsci/acceptance-test-harness"),
    new Artifact("Docker fixtures", "org.jenkins-ci.test", "docker-fixtures", null, "https://github.com/jenkinsci/docker-fixtures"),
    new Artifact("GitHub API", "org.kohsuke", "github-api", null, "http://github-api.kohsuke.org/"),
    new Artifact("Jenkins Test Harness", "org.jenkins-ci.main", "jenkins-test-harness", null, "https://github.com/jenkinsci/jenkins-test-harness"),
    new Artifact("Remoting", "org.jenkins-ci.main", "remoting", null, "https://github.com/jenkinsci/remoting"),
    new Artifact("Stapler", "org.kohsuke.stapler", "stapler", null, "https://github.com/stapler/stapler"),
    new Artifact("Version Number Lib", "org.jenkins-ci", "version-number", null, "https://github.com/jenkinsci/lib-version-number"),
    new Artifact("Crypto Util Lib", "org.jenkins-ci", "crypto-util", null, "https://github.com/jenkinsci/lib-crypto-util"),
    new Artifact("Annotation Indexer Lib", "org.jenkins-ci", "annotation-indexer", null, "https://github.com/jenkinsci/lib-annotation-indexer"),
    new Artifact("XStream", "org.jvnet.hudson", "xstream", null, "https://github.com/jenkinsci/xstream")
))


// For each plugin located in the update center
def indexBuilder = new JavadocGroupBuilder("component", "component", "Jenkins Components Javadoc");
components.toSorted(keyComparator).eachWithIndex { value, idx ->
    indexBuilder.withArtifact(value)
}

// Build all
indexBuilder.build()



