package alex.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Utils {

	private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

	static boolean isAllowedPath(final String path, final File jenkinsRootDir, FileSystemListParameterGlobalConfiguration testGC) {
		FileSystemListParameterGlobalConfiguration globalConfig;
		// use testing gc
		if (testGC == null) {
			globalConfig = FileSystemListParameterGlobalConfiguration.get();
		} else {
			globalConfig = testGC;
		}
		List<AdditionalBaseDirPath> additionalBaseDirs = globalConfig.getAdditionalBaseDirs();
		Path pathToCheck;
		try {
			pathToCheck = new File(path).toPath().toRealPath();
			// userContent
			if (globalConfig.isEnabledUserContent() && jenkinsRootDir != null) {
				String userContentPath = jenkinsRootDir.getCanonicalPath() + File.separator + "userContent" + File.separator;
				if (pathToCheck.startsWith(userContentPath)) {
					return true;
				}
			}
			// AllowedPathList
			for (AdditionalBaseDirPath baseDir : additionalBaseDirs) {
				String baseDirCanonical = new File(baseDir.getAdditionalBaseDirPath()).getCanonicalPath() + File.separator;
				if (pathToCheck.startsWith(baseDirCanonical)) {
					return true;
				}
			}
		} catch (IOException e) {
			LOGGER.warning(String.format(Messages.FileSystemListParameterDefinition_PathCheckError(), path));
		}
		return false;
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

}
