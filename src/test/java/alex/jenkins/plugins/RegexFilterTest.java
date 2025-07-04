package alex.jenkins.plugins;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class RegexFilterTest {

    private JenkinsRule j;

    private String path;
    private FileSystemListParameterGlobalConfiguration globalConfigAllowedPaths;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
        URL resource = getClass().getResource("/1");
        assertNotNull(resource, "Test test directory missing");
        path = resource.getPath();
        globalConfigAllowedPaths = TestUtils.createTestGC(path);
    }

    @Test
    void testRegexEmptyFilter() throws Exception {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        String includePattern = "";
        String excludePattern = "";
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path,
                "", "FILE", "SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder,
                includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        List<String> list = pd.getFsObjectsList();

        assertEquals(3, list.size());
    }

    @Test
    void testRegexFilterNotfound() throws Exception {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        String includePattern = "notFound";
        String excludePattern = "";
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path,
                "", "FILE", "SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder,
                includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        List<String> list = pd.getFsObjectsList();

        assertEquals(1, list.size());
        assertTrue(list.get(0).contains("No objects of type"), "Contains no objects found message");
    }

    @Test
    void testRegexIncludeFilter() throws Exception {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        String includePattern = "[\\w]*3[.]txt";
        String excludePattern = "";
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path,
                "", "FILE", "SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder,
                includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        List<String> list = pd.getFsObjectsList();

        assertEquals(1, list.size());
        assertEquals("test3.txt", list.get(0));
    }

    @Test
    void testRegexExcludeFilter() throws Exception {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        String includePattern = "";
        String excludePattern = "[\\w]*3[.]txt";
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path,
                "", "FILE", "SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder,
                includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        List<String> list = pd.getFsObjectsList();

        assertEquals(2, list.size());
        assertEquals("test1.txt", list.get(0));
        assertEquals("test2.txt", list.get(1));
    }
}
