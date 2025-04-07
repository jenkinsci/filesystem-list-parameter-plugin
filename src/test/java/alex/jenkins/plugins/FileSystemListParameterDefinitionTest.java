package alex.jenkins.plugins;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.For;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@For(FileSystemListParameterDefinition.class)
@WithJenkins
class FileSystemListParameterDefinitionTest {

    @Test
    @Issue("JENKINS-49649")
    void smokeRoundtrip(JenkinsRule j) throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        FileSystemListParameterDefinition d = new FileSystemListParameterDefinition("name", "description", "master", "path", "", "FILE", "SINGLE_SELECT", "", "", true, false, false);
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(d);
        p.addProperty(params);

        j.configRoundtrip(p);
    }
}
