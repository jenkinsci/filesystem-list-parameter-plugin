package alex.jenkins.plugins;

import jenkins.model.GlobalConfiguration;

import java.util.List;

public final class TestUtils {

    public static FileSystemListParameterGlobalConfiguration createTestGC(String allowedPath) {
        FileSystemListParameterGlobalConfiguration globalConfigAllowedPaths = new FileSystemListParameterGlobalConfiguration();
        GlobalConfiguration.all().get(FileSystemListParameterGlobalConfiguration.class);
        List<AdditionalBaseDirPath> list = globalConfigAllowedPaths.getAdditionalBaseDirs();
        AdditionalBaseDirPath additionalBaseDirs = new AdditionalBaseDirPath(allowedPath);
        list.add(additionalBaseDirs);
        globalConfigAllowedPaths.setAdditionalBaseDirs(list);
        return globalConfigAllowedPaths;
    }
}
