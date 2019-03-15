/**
 * 
 */
package alex.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author aendter
 *
 */
public class FileSystemListParameterDefinition extends ParameterDefinition {

	private static final long serialVersionUID = 9032072543915872650L;

	private static final Logger LOGGER = Logger.getLogger(FileSystemListParameterDefinition.class.getName());
	
    public static final String MASTER = "master";

	public static enum FsObjectTypes implements java.io.Serializable {
		ALL, DIRECTORY, FILE, SYMLINK
	}

	@Extension
	public static class DescriptorImpl extends ParameterDescriptor {
		@Override
		public String getDisplayName() {
			return Messages.FileSystemListParameterDefinition_DisplayName();
		}

		public FormValidation doCheckName(@QueryParameter final String name) throws IOException {
			if (StringUtils.isBlank(name)) {
				return FormValidation.error(Messages.FileSystemListParameterDefinition_NameCanNotBeEmpty());
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckPath(@QueryParameter final String path) throws IOException {
			if (StringUtils.isBlank(path)) {
				return FormValidation.error(Messages.FileSystemListParameterDefinition_PathCanNotBeEmpty());
			}

			File dir = new File(path);
			if (!dir.exists()) {
				return FormValidation.error(Messages.FileSystemListParameterDefinition_PathDoesntExist(), path);
			}

			String[] items = dir.list();
			if (items == null || items.length == 0) {
				return FormValidation.warning(Messages.FileSystemListParameterDefinition_NoObjectsFound(), path);
			}
			return FormValidation.ok();

		}

		public FormValidation doCheckRegexIncludePattern(@QueryParameter final String regexIncludePattern) {
			return checkRegex(regexIncludePattern);

		}

		public FormValidation doCheckRegexExcludePattern(@QueryParameter final String regexExcludePattern) {
			return checkRegex(regexExcludePattern);

		}

		private FormValidation checkRegex(String regex) {
			try {
				Pattern.compile(regex);
				return FormValidation.ok();
			} catch (PatternSyntaxException pse) {
				return FormValidation.error(Messages.FileSystemListParameterDefinition_RegExPatternNotValid(), regex,
						pse.getDescription());
			}
		}

	}


	private String nodeName;
	private String path;
	private String selectedType;
	private boolean sortByLastModified;
	private boolean sortReverseOrder;
	private FsObjectTypes selectedEnumType;
	private String regexIncludePattern;
	private String regexExcludePattern;
	private String value;

	/**
	 * @param name
	 * @param description
	 */
	@DataBoundConstructor
	public FileSystemListParameterDefinition(String name, String description, String nodeName, String path, String selectedType,
			String regexIncludePattern, String regexExcludePattern, boolean sortByLastModified,
			boolean sortReverseOrder) {
		super(name, description);

		this.nodeName = nodeName;
		this.path = Util.fixNull(path);
		this.selectedType = selectedType;
		this.selectedEnumType = FsObjectTypes.valueOf(selectedType);
		this.sortByLastModified = sortByLastModified;
		this.sortReverseOrder = sortReverseOrder;
		this.regexIncludePattern = regexIncludePattern;
		this.regexExcludePattern = regexExcludePattern;

	}

	@Override
	public ParameterValue createValue(StaplerRequest request) {
		String value[] = request.getParameterValues(getName());
		if (value == null) {
			return getDefaultParameterValue();
		}
		return null;
	}

	@Override
	public ParameterValue createValue(StaplerRequest request, JSONObject jO) {
		Object value = jO.get("value");
		String strValue = "";
		if (value instanceof String) {
			strValue = (String) value;
		} else if (value instanceof JSONArray) {
			JSONArray jsonValues = (JSONArray) value;
			strValue = StringUtils.join(jsonValues.iterator(), ',');
		}

		return new FileSystemListParameterValue(getName(), strValue);
	}

	@Override
	public ParameterValue getDefaultParameterValue() {
		String defaultValue = "";

		try {
			defaultValue = getEffectiveDefaultValue();
		} catch (Exception e) {
			LOGGER.warning(
					String.format(Messages.FileSystemListParameterDefinition_SymlinkDetectionError(), defaultValue));
		}
		if (!StringUtils.isBlank(defaultValue)) {
			return new FileSystemListParameterValue(getName(), defaultValue);
		}
		return super.getDefaultParameterValue();
	}

	private String getEffectiveDefaultValue() throws Exception {

		List<String> defaultList = getFsObjectsList();
		String defaultValue = defaultList.get(0);
		return defaultValue;
	}

	public List<String> getFsObjectsList() throws Exception {

		Computer computer = null;
		VirtualChannel channel = null;
		Jenkins instance = Jenkins.getInstance();
		if (getNodeName() != null && !getNodeName().trim().isEmpty() && instance != null) {
			computer = instance.getComputer(getNodeName());
			if (computer != null) {
				channel = computer.getChannel();
			}
		}
		
		
		FilePath rootPath = new FilePath(channel, path);
		class FilesLister implements FileCallable<List<String>> {

			private static final long serialVersionUID = 1;

			@Override
			public List<String> invoke(File rootDir, VirtualChannel channel) {
				final TreeMap<String, Long> map = new TreeMap<>();
				try {
					File[] listFiles = rootDir.listFiles();

					if (listFiles != null) {
						switch (getSelectedEnumType()) {
						case SYMLINK:
							createSymlinkMap(listFiles, map);
							break;
						case DIRECTORY:
							createDirectoryMap(listFiles, map);
							break;
						case FILE:
							createFileMap(listFiles, map);
							break;
						default:
							createAllObjectsMap(listFiles, map);
							break;
						}
					}
				} catch (IOException e) {
					LOGGER.warning(String.format(Messages.FileSystemListParameterDefinition_SymlinkDetectionError(),
							"Failed to obtain"));
				}
				return sortList(map);
			}

			@Override
			public void checkRoles(RoleChecker rc) throws SecurityException {
				throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																				// choose Tools | Templates.
			}

		}
		return rootPath.act(new FilesLister());
	}

	List<String> sortList(Map<String, Long> map) {
		List<String> list;

		if (map.isEmpty()) {
			list = new ArrayList<String>();
			String msg = String.format(Messages.FileSystemListParameterDefinition_NoObjectsFoundAtPath(),
					getSelectedEnumType(), getRegexIncludePattern(), getRegexExcludePattern(), getPath()).toString();
			LOGGER.warning(msg);
			list.add(msg);
		} else {
			// Sorting:
			if (isSortByLastModified()) {
				list = createTimeSortedList(map);
			} else {
				list = new ArrayList<String>();
				list.addAll(map.keySet());
			}
			if (isSortReverseOrder()) {
				Collections.reverse(list);
			}
		}

		return list;
	}

	static List<String> createTimeSortedList(Map<String, Long> map) {
		List<String> list = new ArrayList<String>();

		Collection<Long> valuesC = map.values();
		List<Long> sortList = new ArrayList<Long>(valuesC);
		Collections.sort(sortList);

		// iterate over sorted values
		for (Long value : sortList) {

			if (map.containsValue(value)) {

				// key with lowest value will be added first
				for (Map.Entry<String, Long> entry : map.entrySet()) {
					if (value.equals(entry.getValue())) {
						list.add(entry.getKey());
					}
				}
			}
		}

		return list;
	}

	static private boolean isSymlink(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}

	private boolean isPatternMatching(String name) {

		if (getRegexIncludePattern().equals("") && getRegexExcludePattern().equals("")) {
			return true;
		}

		if (!getRegexIncludePattern().equals("") && getRegexExcludePattern().equals("")) {
			return name.matches(getRegexIncludePattern());
		}
		if (getRegexIncludePattern().equals("") && !getRegexExcludePattern().equals("")) {
			return !name.matches(getRegexExcludePattern());
		}

		return name.matches(getRegexIncludePattern()) && !name.matches(getRegexExcludePattern());
	}

	private void createSymlinkMap(File[] listFiles, Map<String, Long> target) throws IOException {

		for (File file : listFiles) {
			if (!file.isHidden() && isSymlink(file) && isPatternMatching(file.getName())) {
				target.put(file.getName(), file.lastModified());
				LOGGER.finest("add " + file);
			}
		}
	}

	private void createDirectoryMap(File[] listFiles, Map<String, Long> target) throws IOException {

		for (File file : listFiles) {
			if (!file.isHidden() && file.isDirectory() && !isSymlink(file) && isPatternMatching(file.getName())) {
				target.put(file.getName(), file.lastModified());
				LOGGER.finest("add " + file);
			}
		}
	}

	private void createFileMap(File[] listFiles, Map<String, Long> target) throws IOException {

		for (File file : listFiles) {
			if (!file.isHidden() && file.isFile() && !isSymlink(file) && isPatternMatching(file.getName())) {
				target.put(file.getName(), file.lastModified());
				LOGGER.finest("add " + file);
			}
		}
	}

	private void createAllObjectsMap(File[] listFiles, Map<String, Long> target) {

		for (File file : listFiles) {
			if (!file.isHidden() && isPatternMatching(file.getName())) {
				target.put(file.getName(), file.lastModified());
				LOGGER.finest("add " + file);
			}
		}
	}

	/*
	 * Creates list to display in config.jelly
	 */
	public List<String> getJellyFsObjectTypes() {
		ArrayList<String> list = new ArrayList<String>();
		String selected = getSelectedType();

		LOGGER.finest("# selectedType=" + selected);

		if (selected.equals("")) {
			for (FsObjectTypes type : FsObjectTypes.values()) {
				String string = type.toString();
				LOGGER.finest("# add " + string);
				list.add(string);
			}

		} else {
			LOGGER.finest("# add " + selected);
			list.add(selected);
			for (FsObjectTypes type : FsObjectTypes.values()) {
				String string = type.toString();

				if (!selected.equals(string)) {
					LOGGER.finest("# add " + string);
					list.add(string);
				}
			}
		}

		return list;
	}
	
	
	/*
	 * Creates list to display in config.jelly
	 */
	public static List<String> getNodeNames() {
		ArrayList<String> list = new ArrayList<String>();
		final List<Node> nodes = Jenkins.getInstance().getNodes();

			// add master
			list.add(MASTER);
			for (Node node : nodes) {
				String nodeName = node.getNodeName();
				if (StringUtils.isNotBlank(nodeName)) {
					LOGGER.finest("# add " + nodeName);
					list.add(nodeName);
				}
			}

		return list;
	}
	

	
	
	





    

	public String getPath() {
		return path;
	}

	public String getSelectedType() {
		return selectedType;
	}

	public boolean isSortByLastModified() {
		return sortByLastModified;
	}

	public boolean isSortReverseOrder() {
		return sortReverseOrder;
	}

	public FsObjectTypes getSelectedEnumType() {
		return selectedEnumType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRegexIncludePattern() {
		return regexIncludePattern;
	}

	public String getRegexExcludePattern() {
		return regexExcludePattern;
	}

	public String getNodeName() {
		return nodeName;
	}

}
