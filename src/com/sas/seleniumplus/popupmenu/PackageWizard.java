/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
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

public class PackageWizard extends Dialog {
	private Text txtPackage;
	private Label lblMessage;
	private String packageName = "";
	private String prePackageName = "";

	public PackageWizard(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Create a Package");
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
		txtPackage.setEditable(true);
		txtPackage.setText(prePackageName);
		txtPackage.setSelection(prePackageName.length());

		txtPackage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		new Label(container, SWT.NONE);
		lblMessage = new Label(container, SWT.FILL);

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
		packageName = txtPackage.getText().trim();
		super.okPressed();
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		String testclass = txtPackage.getText().trim();
		String prePkgName = getPrePackageName();

		if (prePkgName.equalsIgnoreCase(testclass)){
			updateStatus("Append new folder");
			return;
		}

		if (testclass.length() == 0) {
			updateStatus("Package must be specified");
			return;
		}


		if (testclass.length() == 0) {
			updateStatus("Package must be specified");
			return;
		}

		if (testclass.length() == 0) {
			updateStatus("Package must be specified");
			return;
		}

		if (testclass.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("Package must be valid");
			return;
		}

		boolean startdot = testclass.startsWith(".");
		boolean enddot = testclass.endsWith(".");
		if (startdot || enddot ) {
			updateStatus("Invalid package name");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String string) {
		if (string != null) {
			lblMessage.setText(string);
			if (getButton(IDialogConstants.OK_ID) != null){
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
			return;
		}

		lblMessage.setText("");
		getButton(IDialogConstants.OK_ID).setEnabled(true);
	}

	public String getPackageName() {
		return packageName.trim();
	}

	public void setPackageName(String user) {
		this.packageName = user;
	}

	public String getPrePackageName() {
		return prePackageName.trim();
	}

	public void setPrePackageName(String prePkg) {
		this.prePackageName = prePkg + ".";
	}

}
