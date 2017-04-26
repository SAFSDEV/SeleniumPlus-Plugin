package com.sas.seleniumplus.popupmenu;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TestCaseWizard extends Dialog {
	private Text txtPackage;
	private Text txtTestClass;
	private Label lblMessage;
	private String packageName = "";
	private String testclassName = "";

	public TestCaseWizard(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Create a Test File");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);

		layout.marginRight = 10;
		layout.marginLeft = 10;
		layout.marginTop = 20;
		container.setLayout(layout);

		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setText("Package:");

		txtPackage = new Text(container, SWT.BORDER);
		txtPackage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtPackage.setEditable(false);
		txtPackage.setText(packageName);

		Label lblPassword = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lblPassword.setLayoutData(gd_lblNewLabel);
		lblPassword.setText("Test Class:");

		txtTestClass = new Text(container, SWT.BORDER);
		txtTestClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtTestClass.setFocus();
		txtTestClass.setText(testclassName);
		txtTestClass.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		new Label(container, SWT.NONE);
		lblMessage = new Label(container, SWT.NONE);

		dialogChanged();

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		// Copy data from SWT widgets into fields on button press.
		// Reading data from the widgets later will cause an SWT
		// widget diposed exception.
		packageName = txtPackage.getText();
		testclassName = txtTestClass.getText();
		super.okPressed();
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		String testclass = txtTestClass.getText();

		if (testclass.length() == 0) {
			updateStatus("test class name must be specified");
			return;
		}

		if (testclass.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("test class name must be valid");
			return;
		}

		int dotLoc = testclass.lastIndexOf('.');
		if (dotLoc != -1) {
			updateStatus("remove test class extension");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String string) {
		if (string != null) {
			lblMessage.setText(string);
			if (getButton(IDialogConstants.OK_ID) != null) {
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
			return;
		}
		lblMessage.setText("");
		getButton(IDialogConstants.OK_ID).setEnabled(true);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String user) {
		this.packageName = user;
	}

	public String getTestClassName() {
		return testclassName;
	}

	public void setTestClassName(String password) {
		this.testclassName = password;
	}

}