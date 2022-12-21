package com.itemis.eai.question;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class QuestionDialog extends Dialog {

	private Text instructionText;

	private String input;
	private String instruction;

	public QuestionDialog(Shell parentShell, String input) {
		super(parentShell);
		this.input = input;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Ask a question");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Question:");

		instructionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		layoutData.widthHint = 600;
		layoutData.heightHint = 400;
		instructionText.setLayoutData(layoutData);
		if (!input.isEmpty())
			instructionText.setText(input);

		instructionText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13 && (e.stateMask & SWT.CTRL) == SWT.CTRL) {
					okPressed();
					e.doit = false;
				}
			}
		});

		return composite;
	}

	@Override
	protected void okPressed() {
		instruction = instructionText.getText();
		super.okPressed();
	}

	public String getInstruction() {
		return instruction;
	}

}