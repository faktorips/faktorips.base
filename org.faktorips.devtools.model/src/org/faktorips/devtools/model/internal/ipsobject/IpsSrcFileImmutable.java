/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.util.IoUtil;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;

/**
 * Represents an {@link IpsSrcFileExternal} with an immutable content. Instances of this IpsSrcFile
 * cannot be accessed via the finder Methods of the IpsProject.
 * 
 * @author Thorsten Guenther, Peter Erzberger
 */
public class IpsSrcFileImmutable extends IpsSrcFileExternal {

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
        super(IIpsModel.get().getIpsProject("IpsSrcFileImmutableIpsProject") //$NON-NLS-1$
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

    /**
     * Returns null.
     */
    @Override
    public AFile getCorrespondingFile() {
        return null;
    }

    /**
     * Returns null.
     */
    @Override
    public AResource getCorrespondingResource() {
        return null;
    }

    /**
     * Does nothing
     */
    @Override
    public void save(boolean force, IProgressMonitor monitor) throws CoreRuntimeException {
        // No save
    }

    /**
     * This implementation throws an {@link UnsupportedOperationException}.
     */
    @Override
    public void delete() throws CoreRuntimeException {
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
            IpsLog.log(e);
        } finally {
            IoUtil.close(bufferedReader);
        }
        xmlContent = builder.toString();

        try {
            ipsObject = (IpsObject)getIpsObjectType().newObject(this);
            Document doc = XmlUtil.getDefaultDocumentBuilder().parse(getContentFromEnclosingResource());
            ipsObject.initFromXml(doc.getDocumentElement());
        } catch (Exception e) {
            IpsLog.log(new IpsStatus(e));
        }
    }

    @Override
    public InputStream getContentFromEnclosingResource() throws CoreRuntimeException {
        ByteArrayInputStream content = new ByteArrayInputStream(xmlContent.getBytes());
        return content;
    }

    /**
     * No modification allowed
     */
    @Override
    public void setMemento(IIpsSrcFileMemento memento) throws CoreRuntimeException {
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
