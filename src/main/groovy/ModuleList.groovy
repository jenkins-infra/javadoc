public class ModuleList extends ArrayList<Artifact> {

    public void add(String id,
                   String name = null,
                   String groupId = "org.jenkins-ci.modules",
                   String pageURL = null) {

        def artifactName = name ?: "${id}-module"
        def url = pageURL ?: "https://github.com/jenkinsci/${id}-module"

        // TODO: determine versions from the last core and tweak version?
        def art = new Artifact(artifactName, groupId, id, null, url);
        add(art)
    }
}