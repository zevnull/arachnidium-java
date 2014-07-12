package org.arachnidium.util.configuration;

import java.util.HashMap;

/**
 * @author s.tihomirov Inheritors of this class should make access to
 *         configuration data easier
 */
public abstract class AbstractConfigurationAccessHelper{

	private final Configuration configuration;

	public AbstractConfigurationAccessHelper(Configuration configuration) {
		super();
		this.configuration = configuration;
	}

	protected HashMap<String, Object> getGroup(String groupName) {
		return configuration.getSettingGroup(groupName);
	}
	
	public abstract Object getSetting(String name);

	protected Object getSettingValue(String groupName, String settingName) {
		return configuration.getSettingValue(groupName, settingName);
	}
}
