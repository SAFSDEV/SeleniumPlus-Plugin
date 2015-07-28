package com.sas.seleniumplus.consoles;

import java.io.File;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.ide.IDE;
import org.safs.StringUtils;
import org.safs.StringUtils.ErrorLineParser;
import org.safs.StringUtils.ErrorLinkTrace;

import com.sas.seleniumplus.Activator;

public class TestErrorConsoleLineTracker implements IConsoleLineTracker {

	@SuppressWarnings("unused")
	private static final String PLUGIN_ID = "com.sas.seleniumplus.consoles.TestErrorConsoleLineTracker";
	private static final String PLUGIN_LINE_MARKER_ID = "com.sas.seleniumplus.consoles.SourceLineMarker";

	private IConsole console;

	public void init(IConsole console) {
		this.console = console;
	}

	/**
	 * According to a line of console string, generate the HyperLink to be added in the console.<br>
	 * If the "console string" matches some pattern, a HyperLink will be generated.<br>
	 * @param offset int, the start position of the text on the console.
	 * @param text String, the console string to be parsed.
	 * @return SEErrorHyperLink, it the parameter text doesn't match any regex pattern, then null is returned.
	 * @see ErrorLineParser#parse(int, String)
	 */
	private static SEErrorHyperLink generateHpyerLink(int offset, String text){
		if(text==null || text.isEmpty()) return null;
		SEErrorHyperLink link = null;

		try{
			link = new SEErrorHyperLink(StringUtils.ErrorLineParser.parse(offset, text));
		}catch(Exception ex) {
			Activator.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(ex));
		}

		return link;
	}

	public void lineAppended(IRegion line) {

		try {
			int offset = line.getOffset();
			String text = console.getDocument().get(offset, line.getLength());
			SEErrorHyperLink link = generateHpyerLink(offset, text);

			if(link!=null) {
				console.addLink(link, link.getLinkOffset(), link.getLinkLength());
			}

		} catch(Exception ex) {
			Activator.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(ex));
		}
	}

	public void dispose() {
	}

	/**
	 * This class is a link, which leads back to the source file at a certain line.
	 */
	private static class SEErrorHyperLink implements IHyperlink {
		private ErrorLinkTrace errorTrace = null;
		private IFile javasourcefile = null;

		public SEErrorHyperLink(ErrorLinkTrace errorTrace) {
			this.errorTrace = errorTrace;
		}

		public int getLinkOffset() {
			if(errorTrace==null) return 0;
			return errorTrace.getLinkOffset();
		}
		public int getLinkLength() {
			if(errorTrace==null) return 0;
			return errorTrace.getLinkLength();
		}

		/** convert testName to java source file, and return it as an IFile*/
		private IFile getSourceFile(){
			String debugmsg = StringUtils.debugmsg(false);

			try{
				if(javasourcefile==null){
					String className = errorTrace.getFileName().trim().split(StringUtils.NUMBER)[0];
					if(className.endsWith(".")) className = className.substring(0, className.length()-1);//remove the last point, it is not part of class-name
					String filename = className.replaceAll("\\.", Matcher.quoteReplacement(File.separator))+".java";
					javasourcefile = Activator.getActiveProjectSourceFile(filename);
				}
			}catch(Exception e){
				Activator.error(debugmsg, e);
			}
			return javasourcefile; 
		}

		/**Go back to the source code at Eclipse editor and focus at a certain line*/
		public void linkActivated(){
			try{
				IFile file = getSourceFile();

				if(file!= null) {
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();

					//Open the source code
					IEditorPart editor = IDE.openEditor(page, file, true);

					//Goto the line where problem occurs
					IMarker marker = file.createMarker(PLUGIN_LINE_MARKER_ID);
					String error = errorTrace.getError();
					if(error!=null && !error.isEmpty()) marker.setAttribute(IMarker.MESSAGE, error);
					marker.setAttribute(IMarker.LINE_NUMBER, Integer.parseInt(errorTrace.getLine()));
					IDE.gotoMarker(editor, marker);
				}else{
					
				}

			}catch(Exception e){
				Activator.error(StringUtils.debugmsg(false), e);
			}

		}

		public void linkEntered() {
		}

		public void linkExited() {
		}

	}

}
