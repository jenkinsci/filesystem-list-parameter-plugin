package alex.jenkins.plugins;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.model.StringParameterValue;

public class FileSystemListParameterValue extends StringParameterValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1673035307789515354L;

	@DataBoundConstructor
	public FileSystemListParameterValue(String name, String value) {
		super(name, value);
	}

}
