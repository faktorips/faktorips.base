/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;

/**
 * Represents an IpsSrcFile with an immutable content. Instances of this IpsSrcFile cannot be
 * accessed via the finder Methods of the IpsProject.
 * 
 * @author Thorsten Guenther, Peter Erzberger
 */
public class IpsSrcFileImmutable extends IpsSrcFile {

    private IpsObject ipsObject;

    private String xmlContent;

    /**
     * Create a new IpsSrcFileImmutable with a content based on the provided InputStream. The
     * content of this IpsSrcFile cannot be changed.
     * 
     * @param name the name of this IpsSrcFile
     * @param content the content of this IpsSrcFile
     */
    public IpsSrcFileImmutable(String name, InputStream content) {
        super(new IpsProject(IpsPlugin.getDefault().getIpsModel(), "IpsSrcFileImmutableIpsProject"), name); //$NON-NLS-1$
        setContents(content);
    }

    /**
     * Returns <code>true</code> as the file is constructed from an existing input stream.
     */
    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isHistoric() {
        return true;
    }

    /**
     * Returns null.
     */
    @Override
    public IFile getCorrespondingFile() {
        return null;
    }

    /**
     * Returns null.
     */
    @Override
    public IResource getCorrespondingResource() {
        return null;
    }

    /**
     * Does nothing - no save on remote files.
     */
    @Override
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        // No save on remote files
    }

    private void setContents(InputStream in) {
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
        }
        xmlContent = builder.toString();

        try {
            ipsObject = (IpsObject)getIpsObjectType().newObject(this);
            Document doc = IpsPlugin.getDefault().newDocumentBuilder().parse(getContentFromEnclosingResource());
            ipsObject.initFromXml(doc.getDocumentElement());
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus(e));
        }
    }

    @Override
    public InputStream getContentFromEnclosingResource() throws CoreException {
        ByteArrayInputStream content = new ByteArrayInputStream(xmlContent.getBytes());
        return content;
    }

    /**
     * No modification allowed
     */
    @Override
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        // No modification of a remote file
    }

    /**
     * Remote files can not be modified - so it is never dirty.
     */
    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isContentParsable() {
        return ipsObject.isFromParsableFile();
    }

    @Override
    public IIpsObject getIpsObject() throws CoreException {
        return ipsObject;
    }

    @Override
    public QualifiedNameType getQualifiedNameType() {
        return QualifiedNameType.newQualifedNameType(name);
    }

}
