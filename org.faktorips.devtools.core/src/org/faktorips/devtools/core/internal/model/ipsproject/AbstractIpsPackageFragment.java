/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.util.AlphaNumericComparator;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragment extends IpsElement implements IIpsPackageFragment {

    public static final AlphaNumericSimpleNameComparator DEFAULT_CHILD_ORDER_COMPARATOR = new AlphaNumericSimpleNameComparator();

    public AbstractIpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }

    public AbstractIpsPackageFragment() {
        super();
    }

    @Override
    public IIpsPackageFragmentRoot getRoot() {
        return (IIpsPackageFragmentRoot)getParent();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsSrcFiles();
    }

    @Override
    public IIpsPackageFragment getParentIpsPackageFragment() {
        int lastIndex = getName().lastIndexOf(SEPARATOR);
        if (lastIndex < 0) {
            if (isDefaultPackage()) {
                return null;
            } else {
                return getRoot().getDefaultIpsPackageFragment();
            }
        } else {
            String parentPath = getName().substring(0, lastIndex);
            return getRoot().getIpsPackageFragment(parentPath);
        }
    }

    @Override
    public IPath getRelativePath() {
        return new Path(getName().replace(SEPARATOR, IPath.SEPARATOR));
    }

    @Override
    public boolean isDefaultPackage() {
        return getName().equals(NAME_OF_THE_DEFAULT_PACKAGE);
    }

    @Override
    public IIpsSrcFile getIpsSrcFile(String name) {
        IpsObjectType type = IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
        if (type != null) {
            return new IpsSrcFile(this, name);
        }
        return null;
    }

    @Override
    public IIpsSrcFile getIpsSrcFile(String filenameWithoutExtension, IpsObjectType type) {
        return new IpsSrcFile(this, filenameWithoutExtension + SEPARATOR + type.getFileExtension());
    }

    @Override
    public String getLastSegmentName() {
        int index = getName().lastIndexOf(SEPARATOR);
        if (index == -1) {
            return getName();
        } else {
            return getName().substring(index + 1);
        }
    }

    protected String getSubPackageName(String subPackageName) {
        return isDefaultPackage() ? subPackageName : getName() + SEPARATOR + subPackageName;
    }

    @Override
    public IIpsPackageFragment getSubPackage(String subPackageFragmentName) {
        String packageName = getSubPackageName(subPackageFragmentName);
        return getRoot().getIpsPackageFragment(packageName);
    }

    @Override
    public Comparator<IIpsElement> getChildOrderComparator() {
        return DEFAULT_CHILD_ORDER_COMPARATOR;
    }

    /**
     * Searches all objects of the given type and adds them to the result.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public abstract void findIpsObjects(IpsObjectType type, List<IIpsObject> result) throws CoreException;

    /**
     * Searches all ips source files of the given type and adds them to the result.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public abstract void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) throws CoreException;

    static class AlphaNumericSimpleNameComparator implements Comparator<IIpsElement>, Serializable {

        private static final long serialVersionUID = 1L;
        private static final AlphaNumericComparator ALPHA_NUMERIC_COMPARATOR = new AlphaNumericComparator();

        @Override
        public int compare(IIpsElement o1, IIpsElement o2) {
            if (o1 == null) {
                return o2 == null ? 0 : Integer.MIN_VALUE;
            }
            if (o2 == null) {
                return Integer.MAX_VALUE;
            }
            return ALPHA_NUMERIC_COMPARATOR.compare(o1.getName(), o2.getName());
        }

    }
}
