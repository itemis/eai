package com.itemis.eai.question;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditor;

import com.itemis.eai.OpenAI;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;

public class QuestionHandler extends AbstractHandler {

	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String input = "";
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			ISelection selection = textEditor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				input = textSelection.getText();
				if (input.isEmpty()) {
					input = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
				}
			}
		}
		QuestionDialog dialog = new QuestionDialog(Display.getCurrent().getActiveShell(), input);
		if (dialog.open() == Dialog.OK) {
			String instruction = dialog.getInstruction();
			queryAndAnswer(instruction);
		}
		return null;
	}

	private void queryAndAnswer(String instruction) {
		try {
			runProgressService(instruction);
		} catch (InvocationTargetException | InterruptedException e) {
			StatusManager.getManager().handle(Status.error(e.getMessage(), e));
		}
	}

	private void runProgressService(String instruction)
			throws InvocationTargetException, InterruptedException {
		PlatformUI.getWorkbench().getProgressService().run(true, false,
				new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Query Open AI", 100);
						monitor.worked(5);
						String result = callOpenAiService(instruction, monitor);
						displayResult(instruction, result);
						monitor.worked(5);
						monitor.done();
					}
				});
	}

	private String callOpenAiService(String instruction, IProgressMonitor monitor) {
		CompletionRequest request = CompletionRequest.builder()
				.prompt(instruction)
				.model("text-davinci-003")
				.temperature(0.7)
				.maxTokens(2000)
				.echo(true)
				.build();
		List<CompletionChoice> choices = OpenAI.getOpenAiService().createCompletion(request).getChoices();
		monitor.worked(90);
		if (choices.size() <= 0)
			throw new RuntimeException("No result!");
		choices.forEach(System.out::println);
		CompletionChoice choice = choices.get(0);
		return choice.getText();
	}

	private void displayResult(String instruction, String result)
			throws InvocationTargetException, InterruptedException {
		PlatformUI.getWorkbench().getProgressService().runInUI(window, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				ResultDialog dialog = new ResultDialog(Display.getCurrent().getActiveShell(),
						result.substring(instruction.length()).trim());
				dialog.open();
			}
		}, null);
	}

}
