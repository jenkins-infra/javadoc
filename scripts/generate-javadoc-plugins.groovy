import groovy.json.*;

String location = "http://updates.jenkins.io/current/update-center.actual.json"
String pluginLocation = "https://repo.jenkins-ci.org/releases/"

// AntBuilder used for unzipping content
def ant = new AntBuilder()

// Now we can inject it into the JsonSlurper to produce an object to work with
def json = new JsonSlurper().parseText(new URL (location).text);

// For each plugin
json.plugins.each {it ->

    // we obtain the GAV value, and tokenize it at the ":"
    def gav = it.value.gav.split(":");

    // For the GroupID we need to replace the . with a / which is what we search for in the URL
    gid = gav[0].replace(".", "/")

    // Get the artifactID and version number as well.
    aid = gav[1]
    ver = gav[2]

    // The plugin location is defined as:
    // "https://repo.jenkins-ci.org/releases/artifact/id/version/artifactID-version-javadoc.jar"
    def pluginLoc  = pluginLocation + gid + "/" + aid + "/" + ver + "/" + aid + "-" + ver + "-javadoc.jar";

    // Define the directory as to where the plugin should be extracted to
    def plugin_dir="build/site/plugin/"+ aid + "/"

    // Create the directory where the plugin should exist.
    new File(plugin_dir).mkdirs();

    // Create the new jar file
    def file = new File(plugin_dir, "/" + aid + ".jar");

    // We need an output stream to write the content from the url to the file.
    def fos = file.newOutputStream()

    try {
        // Write the contents of the *-javadoc.jar to the file
        file << new URL(pluginLoc).openStream()

        // Unzip the contents to the plugin directory
        ant.unzip(
                src:file,
                dest:plugin_dir,
                overwrite:true)
    } catch (FileNotFoundException e) {

        // This will only be encountered if there is no javadocs in our repo. We can safely move on.
        println "No javadoc found for: " + aid
    } finally {
        fos.close();
    }
}
