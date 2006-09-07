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

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;

/**
 * Represents an IpsSrcFile with an immutable content. Instances of this IpsSrcFile cannot be accessed 
 * via the finder Methods of the IpsProject.
 * 
 * @author Thorsten Guenther, Peter Erzberger
 */
public class IpsSrcFileImmutable extends IpsSrcFile {

    private IpsSourceFileContents contents;
    private IIpsProject project;
    
	/**
	 * Create a new IpsSrcFileImmutable with a content based on the provided InputStream. The content of this
     * IpsSrcFile cannot be changed. 
	 * 
	 * @param project the IIpsProject this IpsSrcFile relates to
	 * @param name the name of this IpsSrcFile
     * @param content the content of this IpsSrcFile
	 */
	public IpsSrcFileImmutable(IIpsProject ipsProject, String name, InputStream content) {
		super(null, name);
		this.project = ipsProject;
		setContents(content);
	}

	/**
     * Returns null.
	 */
	public IFile getCorrespondingFile() {
		return null;
	}
	
	/**
     * Returns null.
	 */
	public IResource getCorrespondingResource() {
		return null;
	}

	/**
	 * Does nothing - no save on remote files.
	 */
	public void save(boolean force, IProgressMonitor monitor) throws CoreException {
		// No save on remote files
	}
	
	private void setContents(InputStream in) {
        
		try {
            String encoding = project.getXmlFileCharset();
			String data = StringUtil.readFromInputStream(in, encoding);
            contents = new IpsSourceFileContents(this, data, encoding);
		} catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus(e));
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
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
	}

	/**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return project;
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean isMutable() {
		return false;
	}

    /**
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() throws CoreException {
        return contents.getIpsObject();
    }
}