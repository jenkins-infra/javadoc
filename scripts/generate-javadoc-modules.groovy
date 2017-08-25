import groovy.json.*;

// define sort order for plugins
def keyComparator = [compare: { e1, e2 -> e1.name.compareToIgnoreCase(e2.name) }] as Comparator

// TODO: automatic module discovery from WAR?
def modules = new ModuleList()
modules.add("instance-identity", "Instance Identity")
modules.add("sshd", "SSHD")
modules.add("ssh-cli-auth", "SSH CLI client authenticator")
modules.add("optional-plugin-helper", "Optional Plugin Helper")
modules.add("domain-discovery", "Instance Discovery via DNS SRV")
modules.add("slave-installer", "Agent Installer")
modules.add("windows-slave-installer", "Windows Agent Installer")
//TODO: Modules below are still names as Slave Installers
modules.add("launchd-slave-installer", "OS X Agent installer")
modules.add("systemd-slave-installer", "Systemd Agent installer")
modules.add("upstart-slave-installer", "Upstart Agent installer")

// For each plugin located in the update center
def indexBuilder = new JavadocGroupBuilder("module", "module", "Jenkins Modules Javadoc");
modules.toSorted(keyComparator).eachWithIndex { value, idx ->
    indexBuilder.withArtifact(value)
}

// Build all
indexBuilder.build()



