/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsSrcFileProvider;

/**
 * 
 * A <code>IpsSrcFile</code>-Wrapper to add some additional information to the <code>IpsSrcFile</code>
 * 
 * @author dirmeier
 *
 */
public class InstanceViewerItem extends IpsSrcFileProvider {

	/**
	 * @param ipsSrcFile The IpsSrcFile represented by this viewer item
	 */
	public InstanceViewerItem(IIpsSrcFile ipsSrcFile) {
		super(ipsSrcFile);
	}

	private String definingMetaClass;
	
	private boolean duplicateName = false;

	/**
	 * Get the name of defining meta class
	 * @return the name of the meta class, defining the internal source file
	 */
	public String getDefiningMetaClass() {
		return definingMetaClass;
	}

	/**
	 * Set the name of the meta class defining the internal source file
	 * @param definingMetaClass the name of the meta class
	 */
	public void setDefiningMetaClass(String definingMetaClass) {
		this.definingMetaClass = definingMetaClass;
	}

	/**
	 * 
	 * @return the true if duplicateName is set
	 */
	public boolean isDuplicateName() {
		return duplicateName;
	}

	/**
	 * Set whether this item represents a source file that's name is already present 
	 * @param duplicateName the duplicateName to set
	 */
	public void setDuplicateName(boolean duplicateName) {
		this.duplicateName = duplicateName;
	}

}
