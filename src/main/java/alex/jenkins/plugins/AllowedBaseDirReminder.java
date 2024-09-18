package alex.jenkins.plugins;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;

/**
 * Displays the AllowedBaseDirsReminder that the global configuration must be done.
 */
@Extension
public class AllowedBaseDirReminder extends AdministrativeMonitor {


    @Override
    public String getDisplayName() {
        return "Please configure allowed base dirs for File System List Parameter - Allowed base directories";
    }


    @Override
    public boolean isActivated() {
        return true;
    }
}
