/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.IoUtil;
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
        super(IpsPlugin.getDefault().getIpsModel().getIpsProject("IpsSrcFileImmutableIpsProject") //$NON-NLS-1$
                .getIpsPackageFragmentRoot("immutablePackageFragmentRoot").getDefaultIpsPackageFragment(), name); //$NON-NLS-1$
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

    @Override
    public boolean isContainedInIpsRoot() {
        // default implementation will also come to this result but this is faster
        return false;
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
     * Does nothing
     */
    @Override
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        // No save
    }

    /**
     * This implementation throws an {@link UnsupportedOperationException}.
     */
    @Override
    public void delete() throws CoreException {
        throw new UnsupportedOperationException("Immutable IPS source files cannot be deleted."); //$NON-NLS-1$
    }

    private void setContents(InputStream in) {
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
        } finally {
            IoUtil.close(bufferedReader);
        }
        xmlContent = builder.toString();

        try {
            ipsObject = (IpsObject)getIpsObjectType().newObject(this);
            Document doc = IpsPlugin.getDefault().getDocumentBuilder().parse(getContentFromEnclosingResource());
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
    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    @Override
    public QualifiedNameType getQualifiedNameType() {
        return QualifiedNameType.newQualifedNameType(name);
    }

}
