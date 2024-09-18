package alex.jenkins.plugins;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;


@Extension
public class FileSystemListParameterGlobalConfiguration extends GlobalConfiguration {

    public static FileSystemListParameterGlobalConfiguration get() {
        return GlobalConfiguration.all().get(FileSystemListParameterGlobalConfiguration.class);
    }
    
    public FileSystemListParameterGlobalConfiguration() {
        load();
    }

    private boolean enabledUserContent;
    private List<AdditionalBaseDirPath> additionalBaseDirs = new ArrayList<>();

    public boolean isEnabledUserContent() {
        return enabledUserContent;
    }
    
    @DataBoundSetter
    public void setEnabledUserContent(boolean enabledUserContent) {
        this.enabledUserContent = enabledUserContent;
        save();
    }

    public List<AdditionalBaseDirPath> getAdditionalBaseDirs() {
        return additionalBaseDirs;
    }

    public void setAdditionalBaseDirs(List<AdditionalBaseDirPath> additionalBaseDirs) {
        this.additionalBaseDirs = additionalBaseDirs;
        save();
    }
}