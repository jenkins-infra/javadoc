import java.io.BufferedReader
import java.io.IOException
import java.io.UncheckedIOException
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64

public class JavadocGroupBuilder {

    private final String path
    private final String artifactType
    private final Set<String> artifactIDs
    private final String pluginLocation
    private final String username
    private final String password

    // AntBuilder used for unzipping content
    private def indexHtml = new StringBuilder()
    private def ant = new AntBuilder()
    private def indexJson = new groovy.json.JsonBuilder()
    private def jsonUrlMap = [:]


    public JavadocGroupBuilder(String path, String artifactType, String title,
                               Set<String> artifactIDs = null,
                               String pluginLocation = "https://repo.jenkins-ci.org/releases/",
                               String username = null,
                               String password = null,
                               ) {
        this.path = path
        this.artifactType = artifactType
        this.artifactIDs = artifactIDs
        this.pluginLocation = pluginLocation
        this.username = username
        this.password = password


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
            def metadataURL = repoUrl + "maven-metadata.xml"
            def metadata
            println "Version is not defined, reading latest from ${metadataURL}"
            // If username & password are defined, it means we need to add a basic auth to the request
            if (this.username && this.password) {
                try {
                    URLConnection conn = new URL(metadataURL).openConnection()
                    conn.setRequestProperty("Accept-Charset", "UTF-8")
                    conn.setRequestProperty("Accept-Encoding", "identity")
                    conn.setRequestProperty("User-Agent", "javadoc-generator/0.1")
                    conn.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((this.username + ':' + this.password)).getBytes("UTF-8")), "UTF-8"))
                    BufferedReader inBR = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    StringBuilder urlContent = new StringBuilder()
                    String inputLine
                    while ((inputLine = inBR.readLine()) != null) {
                        urlContent.append(inputLine)
                    }
                    inBR.close()
                    metadata = new XmlSlurper().parseText(urlContent.toString())
                } catch(UnsupportedEncodingException uee) {
                    uee.printStackTrace();
                }
            } else {
                metadata = new XmlSlurper().parseText(new URL (metadataURL).text)
            }
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
            // If username & password are defined, it means we need to add a basic auth to the request
            if (this.username && this.password) {
                try {
                    URLConnection conn = new URL(pluginLoc).openConnection()
                    conn.setRequestProperty("Accept-Charset", "UTF-8")
                    conn.setRequestProperty("Accept-Encoding", "identity")
                    conn.setRequestProperty("User-Agent", "javadoc-generator/0.1")
                    conn.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((this.username + ':' + this.password)).getBytes("UTF-8")), "UTF-8"))
                    file << conn.getInputStream()
                } catch(UnsupportedEncodingException uee) {
                    uee.printStackTrace();
                }
            } else {
                file << new URL(pluginLoc).openStream()
            }

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
        } catch (org.apache.tools.ant.BuildException e) {

            // This has happened when javadocs were corrupt in the repo. We can safely move on.
            println "Corrupt javadoc found for ${artifactType}: ${id}. Tried ${pluginLoc} got exception ${e}"
            indexHtml << "<div id='${id}' class='missing'><h2>${name}<span class='version'>${version}</span></h2><p><tt>${id}</tt></p><p>Corrupt Javadoc published for this ${artifactType}.</p><p>${pageHyperlink(pageURL)}</p></div>"
        } finally {
            fos.close();
        }

        /*
         * Until JDK-8215291 is backported to Java 11, work around the problem by munging the file
         * ourselves.
         */
        def search = new File(plugin_dir, 'search.js')
        if (search.exists()) {
            def sedParams = ['sed', '-i', '-e', 's/if (ui.item.p == item.l)/if (item.m \\&\\& ui.item.p == item.l)/g', search.absolutePath] as String[]
            def sedProc = Runtime.getRuntime().exec(sedParams)
            def sedReturn = sedProc.waitFor()
            if (sedReturn != 0) {
                throw new IllegalStateException('sed failed with ' + sedReturn)
            }
        }

        /*
         * Since Java 9, the javadoc(1) command's package-list file has been superseded by a new
         * element-list file. However, the Java 8 version of javadoc(1) still consumes the old
         * package-list file. In order to support both Java 8 and Java 11 builds (including
         * supporting the ability to link against https://javadoc.jenkins.io), we work around the
         * problem by ensuring that both package-list and element-list exist. When we no longer need
         * to support Java 8 builds, this workaround can be deleted.
         */
        def packageList = new File(plugin_dir, 'package-list').toPath()
        def elementList = new File(plugin_dir, 'element-list').toPath()
        try {
            if (Files.exists(packageList) && !Files.exists(elementList)) {
                Files.copy(packageList, elementList, StandardCopyOption.COPY_ATTRIBUTES)
            } else if (Files.exists(elementList) && !Files.exists(packageList)) {
                Files.copy(elementList, packageList, StandardCopyOption.COPY_ATTRIBUTES)
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e)
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
