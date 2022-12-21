package com.itemis.eai.rework;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditor;

import com.itemis.eai.OpenAI;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.edit.EditChoice;
import com.theokanning.openai.edit.EditRequest;

public class ReworkHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String input = "";
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(editor instanceof ITextEditor))
			throw new ExecutionException("Only works for text editors.");
		ITextEditor textEditor = (ITextEditor) editor;
		ISelection selection = textEditor.getSelectionProvider().getSelection();
		if (!(selection instanceof ITextSelection))
			throw new ExecutionException("Only works with text selections.");
		ITextSelection textSelection = (ITextSelection) selection;
		int offset = textSelection.getOffset();
		int length = textSelection.getLength();
		input = textSelection.getText();
		if (input.isEmpty()) {
			input = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
			offset = 0;
			length = input.length();
		}
		ReworkDialog dialog = new ReworkDialog(Display.getCurrent().getActiveShell(), input);
		if (dialog.open() == Dialog.OK) {
			String instruction = dialog.getInstruction();
			queryAndReplace(textEditor, offset, length, input, instruction);
		}
		return null;
	}

	private void queryAndReplace(ITextEditor textEditor, int offset, int length, String input, String instruction) {
		try {
			runProgressService(textEditor, offset, length, input, instruction);
		} catch (InvocationTargetException | InterruptedException e) {
			StatusManager.getManager().handle(Status.error(e.getMessage(), e));
		}
	}

	private void runProgressService(ITextEditor textEditor, int offset, int length, String input, String instruction)
			throws InvocationTargetException, InterruptedException {
		PlatformUI.getWorkbench().getProgressService().run(true, false,
				new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Query Open AI", 100);
						monitor.worked(5);
						String result = callOpenAiService(input, instruction, monitor);
						runEditorReplace(textEditor, offset, length, result);
						monitor.worked(5);
						monitor.done();
					}
				});
	}

	private String callOpenAiService(String input, String instruction, IProgressMonitor monitor) {
		OpenAiService service = OpenAI.getOpenAiService();
		EditRequest request = EditRequest.builder()
				.input(input)
				.instruction(instruction)
				.model("code-davinci-edit-001")
				.temperature(0.7)
				.build();
		List<EditChoice> choices = service.createEdit(request).getChoices();
		monitor.worked(90);
		if (choices.size() <= 0)
			throw new RuntimeException("No result!");
		choices.forEach(System.out::println);
		EditChoice choice = choices.get(0);
		return choice.getText();
	}

	private void runEditorReplace(ITextEditor textEditor, int offset, int length, String result)
			throws InvocationTargetException, InterruptedException {
		PlatformUI.getWorkbench().getProgressService().runInUI(
				textEditor.getEditorSite().getWorkbenchWindow(),
				new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						try {
							textEditor.getDocumentProvider()
									.getDocument(textEditor.getEditorInput())
									.replace(offset, length, result);
						} catch (BadLocationException e) {
							throw new RuntimeException(e);
						}
					}
				}, null);
	}

}
