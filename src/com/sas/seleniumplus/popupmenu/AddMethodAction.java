package com.sas.seleniumplus.popupmenu;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sas.seleniumplus.Activator;


public class AddMethodAction implements IObjectActionDelegate {
	private static final String insertTestMethodActionId = "customplugin.editormethod.insertTestingMethodAction";

	@SuppressWarnings("unused")
	private Shell shell = null;


	public AddMethodAction() {
		super();
	}

	@Override
	public void run(IAction action) {

		IWorkbenchWindow iww = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();


		try {
			//get editor
			IEditorPart editorPart = iww.getActivePage().getActiveEditor();

			if (editorPart instanceof AbstractTextEditor) {
				int offset = 0;

				ITextEditor editor = (ITextEditor)editorPart;
				IDocumentProvider dp = editor.getDocumentProvider();
				IEditorSite iEditorSite = editorPart.getEditorSite();

				if (iEditorSite != null) {

					ISelectionProvider selectionProvider = iEditorSite.getSelectionProvider();
					IDocument doc = dp.getDocument(editor.getEditorInput());


					if (selectionProvider != null) {
						ISelection iSelection = selectionProvider.getSelection();
						offset = ((ITextSelection) iSelection).getOffset();

						if(action.getId().equals(insertTestMethodActionId)){
							doc.replace(offset, 0, FileTemplates.getRegressionTestingMethodSignature());
						}else{
							doc.replace(offset, 0, FileTemplates.getMethodSignature());
						}
					}

				}


				/*
				IEditorSite iEditorSite = editorPart.getEditorSite();
				if (iEditorSite != null) {
					//get selection provider
					ISelectionProvider selectionProvider = iEditorSite
							.getSelectionProvider();
					if (selectionProvider != null) {
						ISelection iSelection = selectionProvider
								.getSelection();
						//offset
						offset = ((ITextSelection) iSelection).getOffset();
						if (!iSelection.isEmpty()) {
							selectedText = ((ITextSelection) iSelection).

							//length
							length = ((ITextSelection) iSelection).getLength();
							System.out.println("length: " + length);
							 MessageDialog.openInformation(
							         shell,
							         "Do Something Menu",
							         "Length: " + length + "    Offset: " + offset);
						}
					}
				}
				*/

			}
		} catch (Exception e) {		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();

	}



}
