/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.StringUtil;

/**
 * @author Jan Ortmann
 */
public abstract class AbstractIpsSrcFile extends IpsElement implements IIpsSrcFile {

    /**
     * Cached QNameType as QualifiedNameType is an immutable value object, we don't have any
     * threading problems here if two threads create two qualified name types we don't have a
     * problem as the two QNameTypes are equal.
     */
    private QualifiedNameType qualifiedNameType = null;

    public AbstractIpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    @Override
    public IIpsPackageFragment getIpsPackageFragment() {
        return (IIpsPackageFragment)getParent();
    }

    @Override
    public final boolean isReadOnly() {
        return !isMutable();
    }

    @Override
    public boolean exists() {
        try {
            IpsObjectPathEntry entry = (IpsObjectPathEntry)getIpsPackageFragment().getRoot().getIpsObjectPathEntry();
            if (entry == null) {
                return false;
            }
            return entry.exists(getQualifiedNameType());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    }

    @Override
    public String getIpsObjectName() {
        String name = getName();
        int index = name.lastIndexOf('.');
        // index == -1 can never happen for IPS source files, they have a file extension!
        return name.substring(0, index);
    }

    @Override
    public IResource getCorrespondingResource() {
        return getCorrespondingFile();
    }

    @Override
    public QualifiedNameType getQualifiedNameType() {
        /*
         * As QualifiedNameType is an immutable value object, we don't have any threading problems
         * here if two threads create two qualified name types we don't have a problem as the two
         * QNameTypes are equal.
         */
        if (qualifiedNameType == null) {
            StringBuffer buf = new StringBuffer();
            String packageFragmentName = getIpsPackageFragment().getName();
            if (!StringUtils.isEmpty(packageFragmentName)) {
                buf.append(getIpsPackageFragment().getName());
                buf.append('.');
            }

            buf.append(StringUtil.getFilenameWithoutExtension(getName()));
            qualifiedNameType = new QualifiedNameType(buf.toString(), getIpsObjectType());
        }
        return qualifiedNameType;
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        if (isContentParsable()) {
            return new IIpsElement[] { getIpsObject() };
        }
        return new IIpsElement[0];
    }

    @Override
    public IIpsObject getIpsObject() throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("Can't get ips object because file does not exist." + this)); //$NON-NLS-1$
        }

        IpsSrcFileContent content = getContent();
        if (content == null) {
            throw new CoreException(new IpsStatus("Could not read content." + this)); //$NON-NLS-1$
        }
        return content.getIpsObject();
    }

    @Override
    public String getPropertyValue(String name) throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("Can't get property value because file does not exist." + this)); //$NON-NLS-1$
        }
        return getContent(false).getRootPropertyValue(name);
    }

    protected IpsSrcFileContent getContent() {
        return getContent(true);
    }

    private IpsSrcFileContent getContent(boolean loadCompleteContent) {
        return ((IpsModel)getIpsModel()).getIpsSrcFileContent(this, loadCompleteContent);
    }

    @Override
    public boolean isContentParsable() throws CoreException {
        IpsSrcFileContent content = getContent();
        if (content == null) {
            return false;
        }
        return content.isParsable();
    }

}
