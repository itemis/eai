package com.itemis.eai.prefernce;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.itemis.eai.OpenAI;

public class OpenAIPreferences extends AbstractPreferenceInitializer {

	private static final String PREFERENCE_API_KEY = "API_KEY";

	@Override
	public void initializeDefaultPreferences() {
		getPreferenceStore().setDefault(PREFERENCE_API_KEY, "");
	}

	public static String getAPIKey() {
		return getPreferenceStore().getString(PREFERENCE_API_KEY);
	}

	public static void setAPIKey(String key) {
		getPreferenceStore().setValue(OpenAIPreferences.PREFERENCE_API_KEY, key);
	}

	private static IPreferenceStore getPreferenceStore() {
		return OpenAI.getDefault().getPreferenceStore();
	}

}
