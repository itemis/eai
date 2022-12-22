package com.itemis.eai.preference;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class OpenAIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text apiKeyField;
	private String apiKey;

	public OpenAIPreferencePage() {
	}

	public OpenAIPreferencePage(String title) {
		super(title);
	}

	public OpenAIPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		apiKey = OpenAIPreferences.getAPIKey();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create a text field for entering the OpenAI API key
		Label apiKeyLabel = new Label(mainComposite, SWT.NONE);
		apiKeyLabel.setText("OpenAI API Key:");
		apiKeyField = new Text(mainComposite, SWT.BORDER | SWT.PASSWORD);
		apiKeyField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		apiKeyField.setText(apiKey);

		return mainComposite;
	}

	@Override
	public boolean performOk() {
		OpenAIPreferences.setAPIKey(apiKeyField.getText());
		return super.performOk();
	}

}