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

import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.IpsElement;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
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
        } catch (CoreRuntimeException e) {
            IpsLog.log(e);
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
    public AResource getCorrespondingResource() {
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
            StringBuilder sb = new StringBuilder();
            String packageFragmentName = getIpsPackageFragment().getName();
            if (!StringUtils.isEmpty(packageFragmentName)) {
                sb.append(getIpsPackageFragment().getName());
                sb.append('.');
            }

            sb.append(StringUtil.getFilenameWithoutExtension(getName()));
            qualifiedNameType = new QualifiedNameType(sb.toString(), getIpsObjectType());
        }
        return qualifiedNameType;
    }

    @Override
    public IIpsElement[] getChildren() throws CoreRuntimeException {
        if (isContentParsable()) {
            return new IIpsElement[] { getIpsObject() };
        }
        return new IIpsElement[0];
    }

    @Override
    public IIpsObject getIpsObject() {
        IpsSrcFileContent content = getContent();
        if (content == null) {
            if (exists()) {
                throw new CoreRuntimeException(new IpsStatus("Could not read content. " + this)); //$NON-NLS-1$
            } else {
                throw new CoreRuntimeException(
                        new IpsStatus("Can't get property value because file does not exist. " + this)); //$NON-NLS-1$
            }
        }
        return content.getIpsObject();
    }

    @Override
    public String getPropertyValue(String name) {
        IpsSrcFileContent content = getContent(false);
        if (content == null) {
            if (exists()) {
                throw new CoreRuntimeException(new IpsStatus("Could not read content. " + this)); //$NON-NLS-1$
            } else {
                throw new CoreRuntimeException(
                        new IpsStatus("Can't get property value because file does not exist. " + this)); //$NON-NLS-1$
            }
        }
        return content.getRootPropertyValue(name);
    }

    protected IpsSrcFileContent getContent() {
        return getContent(true);
    }

    private IpsSrcFileContent getContent(boolean loadCompleteContent) {
        return ((IpsModel)getIpsModel()).getIpsSrcFileContent(this, loadCompleteContent);
    }

    @Override
    public boolean isContentParsable() throws CoreRuntimeException {
        IpsSrcFileContent content = getContent();
        if (content == null) {
            return false;
        }
        return content.isParsable();
    }

    @Override
    public boolean isContainedInIpsRoot() {
        return getIpsPackageFragment().getRoot().exists();
    }

    @Override
    public Set<String> getXsdValidationErrors() {
        return getFromContentOrEmptySet(IpsSrcFileContent::getXsdValidationErrors);
    }

    @Override
    public Set<String> getXsdValidationWarnings() {
        return getFromContentOrEmptySet(IpsSrcFileContent::getXsdValidationWarnings);
    }

    private Set<String> getFromContentOrEmptySet(Function<IpsSrcFileContent, Set<String>> getter) {
        IpsSrcFileContent content = getContent();
        return content != null ? getter.apply(content) : Set.of();
    }
}
