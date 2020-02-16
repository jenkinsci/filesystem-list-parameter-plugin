package alex.jenkins.plugins;

import com.sonyericsson.rebuild.RebuildParameterPage;
import com.sonyericsson.rebuild.RebuildParameterProvider;
import hudson.Extension;
import hudson.model.ParameterValue;

@Extension(optional = true)
public class FileSystemListParameterRebuild extends RebuildParameterProvider {

    @Override
    public RebuildParameterPage getRebuildPage(ParameterValue parameterValue) {
        if (parameterValue instanceof FileSystemListParameterValue) {
            return new RebuildParameterPage(parameterValue.getClass(),"value.jelly");
        }
        return null;
    }
}
