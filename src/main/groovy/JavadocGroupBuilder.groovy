public class JavadocGroupBuilder {

    private final String path
    private final String artifactType
    private final Set<String> artifactIDs
    private final String pluginLocation

    // AntBuilder used for unzipping content
    private def indexHtml = new StringBuilder()
    private def ant = new AntBuilder()
    private def indexJson = new groovy.json.JsonBuilder()
    private def jsonUrlMap = [:]


    public JavadocGroupBuilder(String path, String artifactType, String title,
                               Set<String> artifactIDs = null,
                               String pluginLocation = "https://repo.jenkins-ci.org/releases/"
                               ) {
        this.path = path
        this.artifactType = artifactType
        this.artifactIDs = artifactIDs
        this.pluginLocation = pluginLocation


        // Header
        indexHtml << "<html><head><title>${title}</title>"
        indexHtml << '<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>'
        indexHtml << '<link rel="stylesheet" type="text/css" href="style.css"/>'
        indexHtml << '<script src="script.js"></script>'
        indexHtml << '</head><body>'
    }

    public JavadocGroupBuilder withArtifact(Artifact artifact) {
        return withArtifact(artifact.name, artifact.groupID, artifact.artifactID, artifact.version, artifact.pageURL)
    }

    public JavadocGroupBuilder withArtifact(String name, String gid, String id, String version, String pageURL = null) {

        if (artifactIDs != null && !artifactIDs.contains(id)) {
            println "Skipping ${artifactType} ${id}"
            return
        }
        println "Publishing ${artifactType} ${id}"

        def repoUrl = pluginLocation + gid + "/" + id + "/"
        if (version == null) {
            println "Version is not defined, reading latest from Maven metadata"
            def metadataURL = repoUrl + "maven-metadata.xml"
            def metadata = new XmlSlurper().parseText(new URL (metadataURL).text)
            version = metadata.versioning.latest
            if (version != null && !version.trim().empty) {
                println "Located version: ${version}"
            } else {
                throw new IllegalStateException("Failed to determine version of ${gid}:${id}")
            }
        }

        // The plugin location is defined as:
        // "https://repo.jenkins-ci.org/releases/groupWithSlashes/artifactId/version/artifactId-version-javadoc.jar"
        def pluginLoc  = repoUrl + version + "/" + id + "-" + version + "-javadoc.jar";

        // Define the directory as to where the plugin should be extracted to
        def plugin_dir="build/site/${path}/"+ id + "/"

        // Create the directory where the plugin should exist.
        new File(plugin_dir).mkdirs();

        // Create the new jar file
        def file = new File(plugin_dir, "/${id}.jar");

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

            indexHtml << "<div id='${id}'><h2><a href='${id}/'>${name}</a><span class='version'>${version}</span></h2><p><tt>${id}</tt></p><p><a href='${id}/'>Javadoc</a></p><p>${pageHyperlink(pageURL)}</p></div>"
            jsonUrlMap[id] = [url: pageURL]
        } catch (FileNotFoundException e) {

            // This will only be encountered if there is no javadocs in our repo. We can safely move on.
            println "No javadoc found for ${artifactType}: ${id}. Tried ${pluginLoc}"
            indexHtml << "<div id='${id}' class='missing'><h2>${name}<span class='version'>${version}</span></h2><p><tt>${id}</tt></p><p>No Javadoc has been published for this ${artifactType}.</p><p>${pageHyperlink(pageURL)}</p></div>"
        } finally {
            fos.close();
        }

        return this;
    }

    public void build() {
        indexHtml << '</body></html>'
        new File("build/site/${path}/index.html").text = indexHtml

        // copy CSS anf JS into output
        new File("build/site/${path}/style.css").text = new File("resources/style.css").text

        // Generate JSON
        indexJson jsonUrlMap
        new File("build/site/${path}/index.json").text = indexJson.toString()
    }

    private String pageHyperlink(String pageURL) {
        if (pageURL == null) {
            // Try guessing GitHub repo?
            return "No homepage found";
        }

        return "<a href=\"${pageURL}\"/>${capitalizeFirstLetter(this.artifactType)} Information</a>"
    }

    private String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}