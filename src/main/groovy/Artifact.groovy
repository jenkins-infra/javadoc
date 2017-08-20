public class Artifact {
    final String name
    final String groupID
    final String artifactID
    final String version
    String pageURL

    public Artifact(String name, String groupID, String artifactID, String version = null, String pageURL = null) {
        this.name = name
        this.groupID = groupID.replace(".", "/")
        this.artifactID = artifactID
        this.version = version
        this.pageURL = pageURL
    }

    public static Artifact fromGAV(String name, String gavString, String pageURL = null) {
        // we obtain the GAV value, and tokenize it at the ":"
        def gav = gavString.split(":");

        return new Artifact(name, gav[0].replace(".", "/"), gav[1], gav[2], pageURL);
    }

    public static Artifact pluginFromGAV(String name, String gavString) {
        // we obtain the GAV value, and tokenize it at the ":"
        def gav = gavString.split(":");

        return new Artifact(name, gav[0].replace(".", "/"), gav[1], gav[2], "https://plugins.jenkins.io/${gav[1]}");
    }

}