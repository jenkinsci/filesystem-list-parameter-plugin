package alex.jenkins.plugins;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FileSystemListParameterDefinitionTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void smokeRoundtrip() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        FileSystemListParameterDefinition d = new FileSystemListParameterDefinition("name", "description", "master", "path", "", "FILE","SINGLE_SELECT", "", "", true, false, false);
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(d);
        p.addProperty(params);

        j.configRoundtrip(p);
    }

}
