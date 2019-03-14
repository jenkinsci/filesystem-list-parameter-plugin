package alex.jenkins.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegexFilterTest {

	String path;
	

	@Before
	public void setup(){
		URL resource = getClass().getResource("/1");
		Assert.assertNotNull("Test test directory missing", resource);
		path = resource.getPath();
	}
	
	@Test
	public void testRegexEmptyFilter() throws Exception {
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
		String includePattern = "";
		String excludePattern = "";
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", path, "FILE", includePattern, excludePattern, sortByLastModified, sortReverseOrder);

		List<String> list = pd.getFsObjectsList();
		
		Assert.assertEquals(3,list.size());
	
	}
	
	
	@Test
	public void testRegexFilterNotfound() throws Exception {
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
		String includePattern = "notFound";
		String excludePattern = "";
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", path, "FILE", includePattern, excludePattern, sortByLastModified, sortReverseOrder);
		
		List<String> list = pd.getFsObjectsList();
		
		Assert.assertEquals(1,list.size());
		Assert.assertTrue("Contains no objects found message", list.get(0).contains("No objects of type"));
		
	}
	
	
	@Test
	public void testRegexIncludeFilter() throws Exception {
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
		String includePattern = "[\\w]*3[.]txt";
		String excludePattern = "";
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", path, "FILE", includePattern, excludePattern, sortByLastModified, sortReverseOrder);
		
		List<String> list = pd.getFsObjectsList();
		
		Assert.assertEquals(1,list.size());
		Assert.assertEquals("test3.txt",list.get(0));
				
		
	}

	
	@Test
	public void testRegexExcludeFilter() throws Exception {
		boolean sortByLastModified = false;
		boolean sortReverseOrder = false;
		String includePattern = "";
		String excludePattern = "[\\w]*3[.]txt";
		FileSystemListParameterDefinition pd = new FileSystemListParameterDefinition("name", "description", path, "FILE", includePattern, excludePattern, sortByLastModified, sortReverseOrder);
		
		List<String> list = pd.getFsObjectsList();
		
		Assert.assertEquals(2,list.size());
		Assert.assertEquals("test1.txt",list.get(0));
		Assert.assertEquals("test2.txt",list.get(1));
		
		
	}
	
}
