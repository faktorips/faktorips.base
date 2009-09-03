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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Configuration class for <code>ModelExlporer</code>s, that can be asked if a specific class or
 * object is allowed and should be displayed in the explorer. The configuration also checks if the
 * given object or class is subclass of an allowed type (class or interface). Thus the published
 * interfaces instead of the concrete classes should be used when instanciating this configuration.
 * 
 * @author Stefan Widmaier
 */
public class ModelExplorerConfiguration {

    private HashSet<Class<?>> allowedIpsElementTypes = new HashSet<Class<?>>();

    private HashSet<Class<?>> allowedResourceTypes = new HashSet<Class<?>>();

    /**
     * Constructs a ModelExplorerConfiguration that allows the given list of IpsElement types and
     * the given list of resource-types. <code>IpsProject</code>,
     * <code>IpsPackageFragmentRoot</code> and <code>IpsPackageFragment</code> are allowed by
     * default.
     */
    public ModelExplorerConfiguration(Class<?>[] ipsElementTypes, Class<?>[] resourceTypes) {
        // add default allowed types
        allowedIpsElementTypes.add(IIpsProject.class);
        allowedIpsElementTypes.add(IIpsPackageFragmentRoot.class);
        allowedIpsElementTypes.add(IIpsPackageFragment.class);

        for (int i = 0, size = ipsElementTypes.length; i < size; i++) {
            allowedIpsElementTypes.add(ipsElementTypes[i]);
        }
        for (int i = 0, size = resourceTypes.length; i < size; i++) {
            allowedResourceTypes.add(resourceTypes[i]);
        }
    }

    /**
     * Returns true if the given IpsElement's class is allowed by this configuration, false
     * otherwise.
     */
    public boolean isAllowedIpsElement(IIpsElement type) {
        if (type instanceof IIpsSrcFile) {
            return isAllowedIpsSrcFile((IIpsSrcFile)type);
        } else {
            return isAllowedIpsElementType(type.getClass());
        }
    }

    private boolean isAllowedIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        IpsObjectType ipsObjectType = ipsSrcFile.getIpsObjectType();
        if (ipsObjectType == null) {
            return false;
        }

        return isAllowedIpsElementType(ipsObjectType.newObject(ipsSrcFile).getClass());
    }

    /**
     * Returns true if the given type is allowed by this configuration, false otherwise. This method
     * also checks if superclasses an implemented interfaces of the given class are allowed, and in
     * that case returns true.
     */
    public boolean isAllowedIpsElementType(Class<?> type) {
        return isAllowedType(type, allowedIpsElementTypes);
    }

    /**
     * Returns true if the given Objects's class is allowed by this configuration, false otherwise.
     */
    public boolean isAllowedResource(Object type) {
        return isAllowedResourceType(type.getClass());
    }

    /**
     * Returns true if the given type is allowed by this configuration, false otherwise. This method
     * also checks if superclasses an implemented interfaces of the given class are allowed, and in
     * that case returns true.
     */
    public boolean isAllowedResourceType(Class<?> type) {
        return isAllowedType(type, allowedResourceTypes);
    }

    /**
     * Returns true if the given class, one of its superclasses or implemented interfaces are
     * contained in the given set.
     */
    private boolean isAllowedType(Class<?> type, HashSet<Class<?>> allowedTypes) {
        for (Iterator<Class<?>> iter = allowedTypes.iterator(); iter.hasNext();) {
            Class<?> allowedClass = iter.next();
            if (allowedClass.isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the given object is of type <code>IProject</code> or <code>IIpsProject</code>
     * , false otherwise.
     */
    public boolean representsProject(Object item) {
        return item instanceof IIpsProject || item instanceof IProject;
    }

    /**
     * Returns true if the given object is of type <code>IFolder</code>,
     * <code>IIpsPackageFragmentRoot</code> or <code>IIpsPackageFragment</code>, false otherwise.
     */
    public boolean representsFolder(Object item) {
        return item instanceof IIpsPackageFragment || item instanceof IIpsPackageFragmentRoot
                || item instanceof IFolder;
    }

    /**
     * Returns true if the given object is of type <code>IProject</code> or <code>IIpsProject</code>
     * , false otherwise.
     */
    public boolean representsFile(Object item) {
        if (item instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot)item;
            if (root.isBasedOnIpsArchive()) {
                return true;
            }
        }

        if (item instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)item;
            IResource resource = ipsElement.getEnclosingResource();
            if (resource instanceof IFile) {
                return true;
            }
            return false;
        }

        if (item instanceof IFile) {
            return true;
        }

        return false;
    }
}
