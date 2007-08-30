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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;

/**
 *
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragment extends IpsElement implements IIpsPackageFragment {

    public static final String SORT_ORDER_FILE = ".packageOrder"; //$NON-NLS-1$

    public AbstractIpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }

    public AbstractIpsPackageFragment() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getRoot() {
        return (IIpsPackageFragmentRoot)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsSrcFiles();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getParentIpsPackageFragment() {
        int lastIndex = getName().lastIndexOf("."); //$NON-NLS-1$
        if (lastIndex < 0) {
            if (isDefaultPackage()) {
                return null;
            }
            else {
                return getRoot().getDefaultIpsPackageFragment();
            }
        }
        else {
            String parentPath = getName().substring(0, lastIndex);
            return new IpsPackageFragment(this.getParent(), parentPath);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IPath getRelativePath() {
        return new Path(getName().replace('.', '/'));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDefaultPackage() {
        return this.name.equals(""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile getIpsSrcFile(String name) {
        IpsObjectType type = IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
        if (type != null) {
            return new IpsSrcFile(this, name);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        try {
            IIpsElement[] children = getChildren();
            if (children != null && children.length > 0) {
                return IpsPlugin.getDefault().getImage("IpsPackageFragment.gif"); //$NON-NLS-1$
            }
        }
        catch (CoreException e) {
            // nothing to do. If we can't get the content, we consider the package empty.
        }
        return IpsPlugin.getDefault().getImage("IpsPackageFragmentEmpty.gif"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getLastSegmentName() {
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return name;
        }
        else {
            return name.substring(index + 1);
        }
    }

    /**
     * Searches all objects of the given type and adds them to the result.
     *
     * @throws CoreException if an error occurs while searching
     */
    public abstract void findIpsObjects(IpsObjectType type, List result) throws CoreException;

}
