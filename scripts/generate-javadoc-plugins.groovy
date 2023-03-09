import groovy.json.*;

// define sort order for plugins
def keyComparator = [compare: { e1, e2 -> e1.key.compareToIgnoreCase(e2.key) }] as Comparator

Set<String> pluginsAIDs = null;
if (args.length > 0 && !args[0].trim().empty) {
    if (pluginsAIDs == null) {
        pluginsAIDs = new HashSet<>(args.length)
    }

    def argPluginIDs = args[0].trim().split("[\\s,]+")
    for (def pluginID : argPluginIDs) {
        pluginsAIDs.add(pluginID)
    }
    println "Plugins to be published: ${argPluginIDs.join(",")}"
}

// Use the artifact caching proxy if defined
String pluginLocation = System.getenv("ARTIFACT_CACHING_PROXY_ORIGIN") != null) ? System.getenv("ARTIFACT_CACHING_PROXY_ORIGIN") + "/releases/" : "https://repo.jenkins-ci.org/releases/"
String username = System.getenv("ARTIFACT_CACHING_PROXY_USERNAME") ?: null
String password = System.getenv("ARTIFACT_CACHING_PROXY_PASSWORD") ?: null

// Start building the plugin group
def indexBuilder = new JavadocGroupBuilder("plugin", "plugin", "Jenkins Plugins Javadoc", pluginsAIDs, pluginLocation, username, password)

// For each plugin located in the update center

String location = "https://updates.jenkins.io/current/update-center.actual.json"
def json = new JsonSlurper().parseText(new URL (location).text);
json.plugins.toSorted(keyComparator).collect { k, v -> v }.eachWithIndex { value, idx ->
    def artifact = Artifact.pluginFromGAV(value.title, value.gav)
    indexBuilder.withArtifact(artifact)
}

// Build all
indexBuilder.build()



