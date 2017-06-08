package com.sas.seleniumplus.eclipse;

import org.eclipse.jdt.core.IPackageFragment;
import org.safs.projects.common.projects.pojo.POJOPackageFragment;

/**
 * Holds an Eclipse IPackageFragment and delegates calls to it.
 *
 */
class IPackageFragmentHolder extends POJOPackageFragment {
	private IPackageFragment packageFragment;

	public IPackageFragmentHolder(IPackageFragment packageFragment) {
		this.packageFragment = packageFragment;
	}
	
	@Override
	public String getElementName() {
		return packageFragment.getElementName();
	}
}
