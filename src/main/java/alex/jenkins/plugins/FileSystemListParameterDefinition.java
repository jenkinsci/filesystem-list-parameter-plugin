/**
 *
 */
package alex.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
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

	public static enum FsSelectTypes  implements java.io.Serializable {
		SINGLE_SELECT, MULTI_SELECT
	}

	@Extension @Symbol("fileSystemList")
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

		public FormValidation doCheckPath(@QueryParameter final String path, @QueryParameter final String selectedNodeName) throws IOException, InterruptedException {
			if (StringUtils.isBlank(path)) {
				return FormValidation.error(Messages.FileSystemListParameterDefinition_PathCanNotBeEmpty());
			}
			
			// Check Path is symlink
			if (hudson.Util.isSymlink(new File(path))) {
				return FormValidation.error(Messages.FileSystemListParameterDefinition_SymlinkPathNotAllowed(), path);
			}
			
			Jenkins instance = Jenkins.getInstanceOrNull();

			// Check Path allowed
			if (instance != null && !isAllowedPath(path, instance.getRootDir(), null)){
				return FormValidation.error(Messages.FileSystemListParameterDefinition_PathNotAllowed(), path);
			}


			// Check nodes
			Computer computer = null;
			VirtualChannel channel = null;

			if (selectedNodeName==null || selectedNodeName.equals(MASTER)) {
				File dir = new File(path);
				if (!dir.exists()) {
					return FormValidation.error(Messages.FileSystemListParameterDefinition_PathDoesntExist(), path);
				}

				String[] items = dir.list();
				if (items == null || items.length == 0) {
					return FormValidation.warning(Messages.FileSystemListParameterDefinition_NoObjectsFound(), path);
				}
				return FormValidation.ok();

			} else {


				if (!selectedNodeName.trim().isEmpty() && instance != null) {
					computer = instance.getComputer(selectedNodeName);
					if (computer != null) {
						channel = computer.getChannel();
					}
				}
				
				
				FilePath filepath = new FilePath(channel, path);
				if (!filepath.exists()) {
					return FormValidation.error(Messages.FileSystemListParameterDefinition_PathDoesntExist(), path);
				}

				List<FilePath> list = filepath.list();
				if (list.isEmpty()) {
					return FormValidation.warning(Messages.FileSystemListParameterDefinition_NoObjectsFound(), path);
				}
				return FormValidation.ok();

			}

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

	//check allowedPath
	static boolean isAllowedPath(final String path, final File jenkinsRootDir, FileSystemListParameterGlobalConfiguration testGC) {
			FileSystemListParameterGlobalConfiguration globalConfig;
			// inject testing gc
			if(testGC==null) {
					globalConfig = FileSystemListParameterGlobalConfiguration.get();
			} else {
					globalConfig = testGC;
			}
			List<AdditionalBaseDirPath> additionalBaseDirs = globalConfig.getAdditionalBaseDirs();
			Path pathToCheck;
			try {
					pathToCheck = new File(path).toPath().toRealPath();
					// userContent
					if (globalConfig.isEnabledUserContent()) {
							String userContentPath = jenkinsRootDir.getCanonicalPath() + File.separator + "userContent" + File.separator;
							if (pathToCheck.startsWith(userContentPath)) {return true;}
					}
					// AllowedPathList
					for (AdditionalBaseDirPath baseDir : additionalBaseDirs) {
							String baseDirCanonical = new File(baseDir.getAdditionalBaseDirPath()).getCanonicalPath() + File.separator;
							if (pathToCheck.startsWith(baseDirCanonical)) {return true;}
					}
			} catch (IOException e) {
					LOGGER.warning(String.format(Messages.FileSystemListParameterDefinition_PathCheckError(), path));
			}
			return false;
	}


	private String selectedNodeName;
	private String path;
	private String selectedType;
	private String formSelectType;
	private boolean sortByLastModified;
	private boolean sortReverseOrder;
	private FsObjectTypes selectedEnumType;
	private String regexIncludePattern;
	private String regexExcludePattern;
	private String value;
	private String defaultValue;
  private boolean includePathInValue;

	/**
	 * @param name
	 * @param description
	 */
	@DataBoundConstructor
	public FileSystemListParameterDefinition(String name, String description, String selectedNodeName, String path, String defaultValue, String selectedType,
			String formSelectType, String regexIncludePattern, String regexExcludePattern, boolean sortByLastModified,
			boolean sortReverseOrder, boolean includePathInValue) {

    super(name);

		this.selectedNodeName = selectedNodeName;
	  this.path = Util.fixNull(path);
		this.defaultValue = defaultValue;
		this.selectedType = selectedType;
		this.formSelectType = formSelectType;
		this.selectedEnumType = FsObjectTypes.valueOf(selectedType);
		this.sortByLastModified = sortByLastModified;
		this.sortReverseOrder = sortReverseOrder;
		this.regexIncludePattern = regexIncludePattern;
		this.regexExcludePattern = regexExcludePattern;
    this.includePathInValue = includePathInValue;
	}


	// https://issues.jenkins.io/browse/JENKINS-74886
	@Override
	public ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException {
		StringParameterValue parameterValue = new StringParameterValue(this.getName(), this.isIncludePathInValue() ? new File(this.path, String.valueOf(value)).getPath() : String.valueOf(value));
		return checkParameterValue(parameterValue);
	}
	
	private ParameterValue checkParameterValue(StringParameterValue parameterValue) {
		try {
			List<String> valuesPossible = getFsObjectsList();
			if (valuesPossible.contains(parameterValue.getValue())) {
				return parameterValue;
			} else {
				//throw new UnsupportedOperationException("Value not valid!");
				//return new StringParameterValue(this.getName(), "injected_value_not_valid__please_check_objects_list");
				LOGGER.warning(String.format(Messages.FileSystemListParameterDefinition_InjectedObjectNotFoundAtPath(), parameterValue.getValue(), path));
				return null;
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException("Problem checking value: " + e.getMessage());
		}
	}
		
		
	@Override
	public ParameterValue createValue(StaplerRequest2 request) {
		String parameterValues[] = request.getParameterValues(getName());
		if (parameterValues == null || parameterValues.length == 0) {
			return getDefaultParameterValue();
		}
		String value = parameterValues[0];
		StringParameterValue stringParameterValue = new StringParameterValue(this.getName(), this.isIncludePathInValue() ? new File(this.path, String.valueOf(value)).getPath() : String.valueOf(value));

		return checkParameterValue(stringParameterValue);
	}

	@Override
	public ParameterValue createValue(StaplerRequest2 request, JSONObject jO) {
		Object value = jO.get("value");
		String strValue = "";
		if (value instanceof String) {
            strValue = this.isIncludePathInValue() ? new File(this.path, String.valueOf(value)).getPath() : String.valueOf(value);
		} else if (value instanceof JSONArray) {
			JSONArray jsonValues = (JSONArray) value;
            strValue = StringUtils.join(
                    this.isIncludePathInValue() ? jsonValues.stream()
                                                        .filter(e -> !StringUtils.isBlank(String.valueOf(e)))
                                                        .map(e -> new File(this.path, String.valueOf(e)).getPath()).iterator()
                                            : jsonValues.iterator(), 
                    ','
            );
		}
		return new FileSystemListParameterValue(getName(), strValue);
	}

	@Override
	public ParameterValue getDefaultParameterValue() {
		String localDefaultValue = "";

		try {
			localDefaultValue = getEffectiveDefaultValue();
		} catch (Exception e) {
			LOGGER.warning(
					String.format(Messages.FileSystemListParameterDefinition_SymlinkDetectionError(), localDefaultValue));
		}
		if (!StringUtils.isBlank(localDefaultValue)) {
            return new FileSystemListParameterValue(
                getName(), 
                this.isIncludePathInValue() ? new File(this.path, localDefaultValue).getPath() : localDefaultValue
            );
		}
		return super.getDefaultParameterValue();
	}

	private String getEffectiveDefaultValue() throws Exception {
		List<String> defaultList = getFsObjectsList();
		if (defaultList.contains(getDefaultValue())) {
			return getDefaultValue();
		} else {
			return defaultList.get(0);
		}
	}

	public List<String> getFsObjectsList() throws Exception {

		Computer computer = null;
		VirtualChannel channel = null;
		Jenkins instance = Jenkins.getInstanceOrNull();
		
		if (getSelectedNodeName() != null && !getSelectedNodeName().trim().isEmpty() && instance != null) {
			computer = instance.getComputer(getSelectedNodeName());
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
		Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance != null && !isAllowedPath(this.path, instance.getRootDir(), null)){
		    list = new ArrayList<String>();
		    String msg = String.format(Messages.FileSystemListParameterDefinition_PathNotAllowed(),
		             getPath()).toString();
		    LOGGER.warning(msg);
		    list.add(msg);
	    } else if (map.isEmpty()) {
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
			if (!file.isHidden() && hudson.Util.isSymlink(file) && isPatternMatching(file.getName())) {
				target.put(file.getName(), file.lastModified());
				LOGGER.finest("add " + file);
			}
		}
	}

	private void createDirectoryMap(File[] listFiles, Map<String, Long> target) throws IOException {

		for (File file : listFiles) {
			if (!file.isHidden() && file.isDirectory() && !hudson.Util.isSymlink(file) && isPatternMatching(file.getName())) {
				target.put(file.getName(), file.lastModified());
				LOGGER.finest("add " + file);
			}
		}
	}

	private void createFileMap(File[] listFiles, Map<String, Long> target) throws IOException {

		for (File file : listFiles) {
			if (!file.isHidden() && file.isFile() && !hudson.Util.isSymlink(file) && isPatternMatching(file.getName())) {
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
	 * Creates list of Select Types to display in config.jelly
	 */
	public List<String> getJellyFsFormSelectTypes() {
		ArrayList<String> list = new ArrayList<String>();
		String selected = getFormSelectType();

		LOGGER.finest("# formSelectType=" + selected);

		if (selected.equals("")) {
			for (FsSelectTypes type : FsSelectTypes.values()) {
				LOGGER.finest("# add " + type.toString());
				list.add(type.toString());
			}
		} else {
			LOGGER.finest("# add " + selected);
			list.add(selected);
			for (FsSelectTypes type : FsSelectTypes.values()) {
				if (!selected.equals(type.toString())) {
					LOGGER.finest("# add " + type.toString());
					list.add(type.toString());
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
		final List<Node> nodes = Jenkins.get().getNodes();

			// add master
			list.add(MASTER);
			for (Node node : nodes) {
				String tmpNodeName = node.getNodeName();
				if (StringUtils.isNotBlank(tmpNodeName)) {
					LOGGER.finest("# add " + tmpNodeName);
					list.add(tmpNodeName);
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

	public String getFormSelectType() {
		return formSelectType;
	}

	public String getValue() {
		return value;
	}

	public String getRegexIncludePattern() {
		return regexIncludePattern;
	}

	public String getRegexExcludePattern() {
		return regexExcludePattern;
	}

	public String getSelectedNodeName() {
		return selectedNodeName;
	}

	public String setSelectedNodeName() {
		return selectedNodeName;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDescription(String description) {
	    super.setDescription(description);
	}

	public boolean isIncludePathInValue() {
		return includePathInValue;
	}

}
