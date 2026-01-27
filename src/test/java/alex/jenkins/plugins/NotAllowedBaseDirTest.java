package alex.jenkins.plugins;

import jenkins.model.GlobalConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class NotAllowedBaseDirTest {

    private JenkinsRule j;

    private String allowedPath;
    //private String allowedFile;
    private String allowedSimilarFile;
    private String notAllowedPath;
    private String notAllowedFile;
    private String notAllowedSimilarFile;
    private String userContentAllowedFile;
    private File jenkinsTmpRoot;
    private FileSystemListParameterGlobalConfiguration gc;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
        allowedPath = getAbsolutePath("/1");
        //allowedFile = allowedPath + File.separator + "test1.txt";
        allowedSimilarFile = allowedPath + "test1.txt";
        notAllowedPath = getAbsolutePath("/2");
        notAllowedFile = notAllowedPath + File.separator + "notAllowed.txt";
        notAllowedSimilarFile = notAllowedPath + "notAllowed.txt";
        jenkinsTmpRoot = new File(getAbsolutePath("/"));
        userContentAllowedFile = jenkinsTmpRoot.getAbsolutePath() + File.separator + "userContent" + File.separator + "allowed.txt";
        gc = new FileSystemListParameterGlobalConfiguration();
        GlobalConfiguration.all().get(FileSystemListParameterGlobalConfiguration.class);
        List<AdditionalBaseDirPath> list = gc.getAdditionalBaseDirs();
        AdditionalBaseDirPath additionalBaseDirs = new AdditionalBaseDirPath(allowedPath);
        list.add(additionalBaseDirs);
        gc.setAdditionalBaseDirs(list);
        gc.setEnabledUserContent(true);
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", "path", "", "FILE", "SINGLE_SELECT", "", "", false, false, false);
        pd.getDefaultValue();
        FileSystemListParameterDefinition.addTestGC(gc);
    }

    private static String getAbsolutePath(String path) {
        URL resource = NotAllowedBaseDirTest.class.getResource(path);
        assertNotNull(resource, "Test test directory missing");
        File dir = new File(resource.getPath());
        return dir.getAbsolutePath();
    }

    @Test
    void testPaths() {
        assertFalse(allowedPath.startsWith(notAllowedPath));
    }

    @Test
    void testAdditionalBaseDir() {
        assertTrue(Utils.isAllowedPath(allowedPath, jenkinsTmpRoot, gc));
        assertFalse(Utils.isAllowedPath(allowedSimilarFile, jenkinsTmpRoot, gc));
        assertFalse(Utils.isAllowedPath(notAllowedFile, jenkinsTmpRoot, gc));
        assertFalse(Utils.isAllowedPath(notAllowedSimilarFile, jenkinsTmpRoot, gc));
    }

    @Test
    void testUserContent() {
        assertTrue(Utils.isAllowedPath(userContentAllowedFile, jenkinsTmpRoot, gc));
        gc.setEnabledUserContent(false);
        assertFalse(Utils.isAllowedPath(userContentAllowedFile, jenkinsTmpRoot, gc));
    }
}
