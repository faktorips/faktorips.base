/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;

/**
 * Represents an IpsSrcFile with immutable content 
 * 
 * @author Thorsten Guenther
 */
public class IpsSrcFileImmutable extends IpsSrcFile {

	/**
	 * Name of the sourcefile without version.
	 */
	private String unversionedName;
	
	/**
	 * Create a new IpsSrcFile with immutable content.
	 * 
	 * @param parent
	 * @param name
	 */
	public IpsSrcFileImmutable(IIpsElement parent, String name, String version, InputStream in) {
		super(parent, name);
		
		// the name of this remote file is the name and the version concatenated, but
		// the name given to the superclass must end to a valid ips-object-extension.
		// so we have to set the complete name (with version) explicitly.
		this.name = name + version;
		this.unversionedName = name;
		
		try {
			setContents(in);
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

	/**
	 * Returns a file which would be the local resource if the remote file would be local. Do NOT use
	 * this handle to manipulate the content - it is possible that the given handle points to an existing
	 * resource (e.g. the checked out version of a resource under version control).
	 */
	public IFile getCorrespondingFile() {
		return null;//((IFolder)getIpsPackageFragment().getCorrespondingResource()).getFile(unversionedName);
	}
	
	/**
	 * Returns a file which would be the local resource if the remote file would be local. Do NOT use
	 * this handle to manipulate the content - it is possible that the given handle points to an existing
	 * resource (e.g. the checked out version of a resource under version control).
	 */
	public IResource getCorrespondingResource() {
		return getCorrespondingFile();
	}

	/**
	 * Does nothing - no save on remote files.
	 */
	public void save(boolean force, IProgressMonitor monitor) throws CoreException {
		// No save on remote files
	}
	
	private void setContents(InputStream in) throws CoreException {
        
		try {
			String data = StringUtil.readFromInputStream(in, getEncoding());
	        
			IpsSourceFileContents contents = IpsPlugin.getDefault().getManager().getSrcFileContents(this);
	        if (contents == null) {
	            contents = new IpsSourceFileContents(this, data, getEncoding()); 
		        IpsPlugin.getDefault().getManager().putSrcFileContents(this, contents);
	        }

	        super.setContents(data);
		} catch (IOException e) {
			throw new CoreException(new IpsStatus(e));
		}
	}
	
	/**
	 * No modification allowed
	 */
	public void setContents(String newContents) throws CoreException {
		// No modification of a remote file
	}

	/**
	 * No modification allowed
	 */
	void setContentsInternal(String newContents) throws CoreException {
		// No modification of a remote file
	}

	/**
	 * No modification allowed
	 */
	public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
		// No modification of a remote file
	}

	/**
	 * Remote files can not be modified - so it is never dirty.
	 */
	public boolean isDirty() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(unversionedName));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isMutable() {
		return false;
	}
}
