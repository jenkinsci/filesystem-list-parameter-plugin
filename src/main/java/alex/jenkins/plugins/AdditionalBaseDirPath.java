package alex.jenkins.plugins;


import org.kohsuke.stapler.DataBoundConstructor;


import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class AdditionalBaseDirPath extends AbstractDescribableImpl<AdditionalBaseDirPath>{

    private String additionalBaseDirPath;

    @DataBoundConstructor
    public AdditionalBaseDirPath(String additionalBaseDirPath) {
        super();
        this.additionalBaseDirPath = additionalBaseDirPath;
    }

    public String getAdditionalBaseDirPath() {
        return additionalBaseDirPath;
    }
    
    @Extension
    public static class AdditionalBaseDirsDescriptor extends Descriptor<AdditionalBaseDirPath> {
        
        //checks
        
        @Override
        public String getDisplayName() { return "Additional Base Dir Path"; }
    }

}
