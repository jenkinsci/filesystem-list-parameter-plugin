package alex.jenkins.plugins;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ChangeSequenceTest {

	String path;

    @Before
	public void setup(){
		URL resource = getClass().getResource("/1");
		Assert.assertNotNull("Test test directory missing", resource);
		path = resource.getPath();
	}
	
	@Test
	public void testSorting() {
		//boolean sortByLastModified = true;
		//boolean sortReverseOrder = false;

		TreeMap<String, Long> map = new TreeMap<>();
		String test1 = "test1";
		String test2 = "test2";
		File f1 = new File(test1);
		File f2 = new File(test2);

		map.put(f1.getName(), (long) 2);
		map.put(f2.getName(), (long) 1);
		
		List<String> sortedList = FileSystemListParameterDefinition.createTimeSortedList(map);
		
		Assert.assertEquals(test2,sortedList.get(0));
		Assert.assertEquals(test1,sortedList.get(1));
		
		
	}

	@Test
	public void testReverseOrder() {
		boolean sortByLastModified = true;
		boolean sortReverseOrder = true;
        boolean includePathInValue = false;
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", "path", "", "FILE","SINGLE_SELECT", "", "", sortByLastModified, sortReverseOrder, includePathInValue);

		TreeMap<String, Long> map = new TreeMap<>();
		String test1 = "test1";
		String test2 = "test2";
		File f1 = new File(test1);
		File f2 = new File(test2);
		
		map.put(f1.getName(), (long) 2);
		map.put(f2.getName(), (long) 1);
		
		List<String> sortedList = pd.sortList(map);
		
		Assert.assertEquals(test1,sortedList.get(0));
		Assert.assertEquals(test2,sortedList.get(1));
		
		
	}

	@Test
	public void testAlphabeticOrder() {
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
        boolean includePathInValue = false;
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", "path", "", "FILE","SINGLE_SELECT", "", "", sortByLastModified, sortReverseOrder, includePathInValue);

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
		
		List<String> sortedList = pd.sortList(map);
		
		Assert.assertEquals(test1,sortedList.get(0));
		Assert.assertEquals(test2,sortedList.get(1));
		Assert.assertEquals(test3,sortedList.get(2));
		
		
	}

	@Test
	public void testGetExistingDefaultValue() throws Exception {
		
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
	    boolean includePathInValue = false;
		String includePattern = "";
		String excludePattern = "";
		String definition_default = "test2.txt";
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path, definition_default, "FILE","SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder, includePathInValue);
		
		String result_default = (String) pd.getDefaultParameterValue().getValue();
		Assert.assertEquals(definition_default, result_default);
					
	}
	
	@Test
	public void testGetNonExistingDefaultValue() {
		
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
		boolean includePathInValue = false;
		String includePattern = "";
		String excludePattern = "";
		String definition_default = "test4.txt";
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", "master", path, definition_default, "FILE","SINGLE_SELECT", includePattern, excludePattern, sortByLastModified, sortReverseOrder, includePathInValue);
		
		String result_default = (String) pd.getDefaultParameterValue().getValue();
		Assert.assertNotEquals(definition_default, result_default);
		
	}
	
	
}
