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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
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
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsSrcFile extends IpsElement implements IIpsSrcFile {

    public AbstractIpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    public IIpsPackageFragment getIpsPackageFragment() {
        return (IIpsPackageFragment)getParent();
    }

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

    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    }

    public String getIpsObjectName() {
        String name = getName();
        int index = name.lastIndexOf('.');
        // index == -1 can never happen for IPS source files, they have a file extension!
        return name.substring(0, index);
    }

    public IResource getCorrespondingResource() {
        return getCorrespondingFile();
    }

    public QualifiedNameType getQualifiedNameType() {
        /*
         * TODO AW: I don't know if the following comment is true any longer because I needed to
         * remove the cache. It is possible now for an IpsSrcFile to be renamed. So
         * QualifiedNameType might change. But basically it shouldn't make any problems. Remove this
         * comment if no problems occurred after some time. This comment was created 18.12.2009.
         */
        /*
         * As QualifiedNameType is an immutable value object, we don't have any threading problems
         * here if two threads create two qualified name types we don't have a problem as the two
         * QNameTypes are equal.
         */
        StringBuffer buf = new StringBuffer();
        String packageFragmentName = getIpsPackageFragment().getName();
        if (!StringUtils.isEmpty(packageFragmentName)) {
            buf.append(getIpsPackageFragment().getName());
            buf.append('.');
        }

        buf.append(StringUtil.getFilenameWithoutExtension(getName()));
        return new QualifiedNameType(buf.toString(), getIpsObjectType());
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        if (isContentParsable()) {
            return new IIpsElement[] { getIpsObject() };
        }
        return new IIpsElement[0];
    }

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

    public boolean isContentParsable() throws CoreException {
        IpsSrcFileContent content = getContent();
        if (content == null) {
            return false;
        }
        return content.isParsable();
    }

    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsSrcFile.gif"); //$NON-NLS-1$
    }

}
