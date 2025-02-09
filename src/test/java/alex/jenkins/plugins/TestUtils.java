package alex.jenkins.plugins;

import java.util.List;

import jenkins.model.GlobalConfiguration;

public class TestUtils {

  public FileSystemListParameterGlobalConfiguration createTestGC(String allowedPath) {
    FileSystemListParameterGlobalConfiguration globalConfigAllowedPaths = new FileSystemListParameterGlobalConfiguration();
    GlobalConfiguration.all().get(FileSystemListParameterGlobalConfiguration.class);
    List<AdditionalBaseDirPath> list = globalConfigAllowedPaths.getAdditionalBaseDirs();
    AdditionalBaseDirPath additionalBaseDirs = new AdditionalBaseDirPath(allowedPath);
    list.add(additionalBaseDirs);
    globalConfigAllowedPaths.setAdditionalBaseDirs(list);
    return globalConfigAllowedPaths;
  }
}
