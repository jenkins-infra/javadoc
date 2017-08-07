import groovy.json.*;

String location = "http://updates.jenkins.io/current/update-center.actual.json"
String pluginLocation = "https://repo.jenkins-ci.org/releases/"

String baseUrl = "http://javadoc.jenkins.io/plugin/"

// AntBuilder used for unzipping content
def ant = new AntBuilder()

// Now we can inject it into the JsonSlurper to produce an object to work with
def json = new JsonSlurper().parseText(new URL (location).text);

// HTML index file header
def indexHtml = '' << '<html><head><title>Jenkins Plugins Javadoc</title>'
indexHtml << '<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>'
indexHtml << '<link rel="stylesheet" type="text/css" href="style.css"/>'
indexHtml << '<script src="script.js"></script>'
indexHtml << '</head><body>'

def indexJson = new groovy.json.JsonBuilder()
def jsonUrlMap = [:]

// define sort order for plugins
def keyComparator = [compare: { e1, e2 -> e1.key.compareToIgnoreCase(e2.key) }] as Comparator

// For each plugin
json.plugins.toSorted(keyComparator).collect { k, v -> v }.eachWithIndex { value, idx ->

    // we obtain the GAV value, and tokenize it at the ":"
    def gav = value.gav.split(":");

    // For the GroupID we need to replace the . with a / which is what we search for in the URL
    gid = gav[0].replace(".", "/")

    // Get the artifactID and version number as well.
    aid = gav[1]
    ver = gav[2]

    // The plugin location is defined as:
    // "https://repo.jenkins-ci.org/releases/groupWithSlashes/artifactId/version/artifactId-version-javadoc.jar"
    def pluginLoc  = pluginLocation + gid + "/" + aid + "/" + ver + "/" + aid + "-" + ver + "-javadoc.jar";

    // Define the directory as to where the plugin should be extracted to
    def plugin_dir="build/site/plugin/"+ aid + "/"

    // Create the directory where the plugin should exist.
    new File(plugin_dir).mkdirs();

    // Create the new jar file
    def file = new File(plugin_dir, "/" + aid + ".jar");

    // We need an output stream to write the content from the url to the file.
    def fos = file.newOutputStream()

    def name = value.title
    def id = value.name
    def version = value.version

    try {
        // Write the contents of the *-javadoc.jar to the file
        file << new URL(pluginLoc).openStream()

        // Unzip the contents to the plugin directory
        ant.unzip(
                src:file,
                dest:plugin_dir,
                overwrite:true)

        indexHtml << "<div id='${id}'><h2><a href='${id}'>${name}</a><span class='version'>${version}</span></h2><p><tt>${id}</tt></p><p><a href='${id}'>Javadoc</a></p><p><a href='https://plugins.jenkins.io/${id}'>Plugin Information</a></p></div>"
        jsonUrlMap[id] = [url: baseUrl + id]
    } catch (FileNotFoundException e) {

        // This will only be encountered if there is no javadocs in our repo. We can safely move on.
        println "No javadoc found for: " + aid
        indexHtml << "<div id='${id}' class='missing'><h2>${name}<span class='version'>${version}</span></h2><p><tt>${id}</tt></p><p>No Javadoc has been published for this plugin.</p><p><a href='https://plugins.jenkins.io/${id}'>Plugin Information</a></p></div>"
    } finally {
        fos.close();
    }
}

// white index.html file with nicer looking list of links to plugins
indexHtml << '</body></html>'
new File("build/site/plugin/index.html").text = indexHtml

indexJson jsonUrlMap
new File("build/site/plugin/index.json").text = indexJson.toString()

// copy CSS anf JS into output
new File("build/site/plugin/style.css").text = new File("resources/style.css").text
