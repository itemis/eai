package com.itemis.eai;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.itemis.eai.preference.OpenAIPreferences;
import com.theokanning.openai.OpenAiService;

public class OpenAI extends AbstractUIPlugin {

	private static OpenAI instance;

	public static OpenAiService getOpenAiService() {
		return new OpenAiService(OpenAIPreferences.getAPIKey(), 60);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		super.start(context);
	}

	public static OpenAI getDefault() {
		return instance;
	}

}
