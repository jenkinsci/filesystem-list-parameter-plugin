package alex.jenkins.plugins;

import alex.jenkins.plugins.FileSystemListParameterDefinition.FilesLister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@WithJenkins
class ChangeSequenceTest {

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
    void testSorting() {
        //boolean sortByLastModified = true;
        //boolean sortReverseOrder = false;

        TreeMap<String, Long> map = new TreeMap<>();
        String test1 = "test1";
        String test2 = "test2";
        File f1 = new File(test1);
        File f2 = new File(test2);

        map.put(f1.getName(), (long) 2);
        map.put(f2.getName(), (long) 1);


        List<String> sortedList = Utils.createTimeSortedList(map);

        assertEquals(test2, sortedList.get(0));
        assertEquals(test1, sortedList.get(1));
    }

    @Test
    void testReverseOrder() {
        boolean sortByLastModified = true;
        boolean sortReverseOrder = true;
        boolean includePathInValue = false;
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path, "", "FILE", "SINGLE_SELECT", "", "", sortByLastModified, sortReverseOrder, includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        FilesLister fl = new FilesLister(pd.getSelectedEnumType(), pd.getRegexIncludePattern(), pd.getRegexExcludePattern(), pd.getPath(), pd.isSortByLastModified(), pd.isSortReverseOrder());

        TreeMap<String, Long> map = new TreeMap<>();
        String test1 = "test1";
        String test2 = "test2";
        File f1 = new File(test1);
        File f2 = new File(test2);

        map.put(f1.getName(), (long) 2);
        map.put(f2.getName(), (long) 1);

        List<String> sortedList = fl.sortList(map);

        assertEquals(test1, sortedList.get(0));
        assertEquals(test2, sortedList.get(1));
    }

    @Test
    void testAlphabeticOrder() {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path, "", "FILE", "SINGLE_SELECT", "", "", sortByLastModified, sortReverseOrder, includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        FilesLister fl = new FilesLister(pd.getSelectedEnumType(), pd.getRegexIncludePattern(), pd.getRegexExcludePattern(), pd.getPath(), pd.isSortByLastModified(), pd.isSortReverseOrder());

        TreeMap<String, Long> map = new TreeMap<>();
        String test1 = "test1";
        String test2 = "test2";
        String test3 = "test3";
        File f1 = new File(test1);
        File f2 = new File(test2);
        File f3 = new File(test3);

        map.put(f1.getName(), (long) 1);
        map.put(f3.getName(), (long) 3);
        map.put(f2.getName(), (long) 2);

        List<String> sortedList = fl.sortList(map);

        assertEquals(test1, sortedList.get(0));
        assertEquals(test2, sortedList.get(1));
        assertEquals(test3, sortedList.get(2));
    }

    @Test
    void testGetExistingDefaultValue() {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        String includePattern = "";
        String excludePattern = "";
        String definitionDefault = "test2.txt";
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path, definitionDefault, "FILE", "SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder, includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);

        String resultDefault = (String) pd.getDefaultParameterValue().getValue();
        assertEquals(definitionDefault, resultDefault);
    }

    @Test
    void testGetNonExistingDefaultValue() {
        boolean sortByLastModified = false;
        boolean sortReverseOrder = false;
        boolean includePathInValue = false;
        String includePattern = "";
        String excludePattern = "";
        String definitionDefault = "test4.txt";
        FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path, definitionDefault, "FILE", "SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder, includePathInValue);
        FileSystemListParameterDefinition.addTestGC(globalConfigAllowedPaths);
        String resultDefault = (String) pd.getDefaultParameterValue().getValue();
        assertNotEquals(definitionDefault, resultDefault);
    }
}
