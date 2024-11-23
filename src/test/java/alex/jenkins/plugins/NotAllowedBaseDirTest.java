package alex.jenkins.plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

public class NotAllowedBaseDirTest {

    @Rule 
    public JenkinsRule j = new JenkinsRule();
    
    String allowedPath;
    String allowedFile;
    String allowedSimilarFile;
    String notAllowedPath;
    String notAllowedFile;
    String notAllowedSimilarFile;
    String userContentAllowedFile;
    File jenkinsTmpRoot;
    Jenkins jenkins;
    FileSystemListParameterGlobalConfiguration gc;

    @Before
    public void setup(){
        allowedPath=getAbsolutePath("/1");
        allowedFile=allowedPath+ File.separator+"test1.txt";
        allowedSimilarFile=allowedPath+"test1.txt";
        notAllowedPath=getAbsolutePath("/2");
        notAllowedFile=notAllowedPath+ File.separator+"notAllowed.txt";
        notAllowedSimilarFile=notAllowedPath+"notAllowed.txt";
        jenkinsTmpRoot=new File(getAbsolutePath("/"));
        userContentAllowedFile=jenkinsTmpRoot.getAbsolutePath()+File.separator+"userContent"+File.separator+"allowed.txt";
        jenkins = Jenkins.get();
        gc = new FileSystemListParameterGlobalConfiguration();
        GlobalConfiguration.all().get(FileSystemListParameterGlobalConfiguration.class);
        List<AdditionalBaseDirPath> list = gc.getAdditionalBaseDirs();
        AdditionalBaseDirPath additionalBaseDirs = new AdditionalBaseDirPath(allowedPath);
        list.add(additionalBaseDirs);
        gc.setAdditionalBaseDirs(list);
        gc.setEnabledUserContent(true);
    }

    private String getAbsolutePath(String path) {
        URL resource = getClass().getResource(path);
        Assert.assertNotNull("Test test directory missing", resource);
        File dir = new File(resource.getPath());
        return dir.getAbsolutePath();
    }
    
    @Test
    public void testPaths() {
        assertFalse(allowedPath.startsWith(notAllowedPath));
    }
    

    @Test
    public void testAdditionalBaseDir() {
        assertTrue(FileSystemListParameterDefinition.isAllowedPath(allowedPath, jenkinsTmpRoot, gc));
        assertFalse(FileSystemListParameterDefinition.isAllowedPath(allowedSimilarFile, jenkinsTmpRoot, gc));
        assertFalse(FileSystemListParameterDefinition.isAllowedPath(notAllowedFile, jenkinsTmpRoot, gc));
        assertFalse(FileSystemListParameterDefinition.isAllowedPath(notAllowedSimilarFile, jenkinsTmpRoot, gc));
    }

    @Test
    public void testUserContent() {
        assertTrue(FileSystemListParameterDefinition.isAllowedPath(userContentAllowedFile, jenkinsTmpRoot, gc));
        gc.setEnabledUserContent(false);
        assertFalse(FileSystemListParameterDefinition.isAllowedPath(userContentAllowedFile, jenkinsTmpRoot, gc));
    }
}
