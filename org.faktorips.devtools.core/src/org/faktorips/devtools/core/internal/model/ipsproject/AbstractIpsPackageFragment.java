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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragment extends IpsElement implements IIpsPackageFragment {

    public AbstractIpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }

    public AbstractIpsPackageFragment() {
        super();
    }

    public IIpsPackageFragmentRoot getRoot() {
        return (IIpsPackageFragmentRoot)getParent();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsSrcFiles();
    }

    public IIpsPackageFragment getParentIpsPackageFragment() {
        int lastIndex = getName().lastIndexOf("."); //$NON-NLS-1$
        if (lastIndex < 0) {
            if (isDefaultPackage()) {
                return null;
            } else {
                return getRoot().getDefaultIpsPackageFragment();
            }
        } else {
            String parentPath = getName().substring(0, lastIndex);
            return new IpsPackageFragment(getParent(), parentPath);
        }
    }

    public IPath getRelativePath() {
        return new Path(getName().replace('.', '/'));
    }

    public boolean isDefaultPackage() {
        return name.equals(""); //$NON-NLS-1$
    }

    public IIpsSrcFile getIpsSrcFile(String name) {
        IpsObjectType type = IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
        if (type != null) {
            return new IpsSrcFile(this, name);
        }
        return null;
    }

    public IIpsSrcFile getIpsSrcFile(String filenameWithoutExtension, IpsObjectType type) {
        return new IpsSrcFile(this, filenameWithoutExtension + '.' + type.getFileExtension());
    }

    public Image getImage() {
        try {
            IIpsElement[] children = getChildren();
            if (children != null && children.length > 0) {
                return IpsPlugin.getDefault().getImage("IpsPackageFragment.gif"); //$NON-NLS-1$
            }
        } catch (CoreException e) {
            // nothing to do. If we can't get the content, we consider the package empty.
        }
        return IpsPlugin.getDefault().getImage("IpsPackageFragmentEmpty.gif"); //$NON-NLS-1$
    }

    public String getLastSegmentName() {
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return name;
        } else {
            return name.substring(index + 1);
        }
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

}
