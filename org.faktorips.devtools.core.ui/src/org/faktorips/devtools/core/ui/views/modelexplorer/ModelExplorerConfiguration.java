/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;

/**
 * Configuration class for <code>ModelExlporer</code>s, that can be asked if a specific class or
 * object is allowed and should be displayed in the explorer. The configuration also checks if the
 * given object or class is subclass of an allowed type (class or interface). Thus the published
 * interfaces instead of the concrete classes should be used when instanciating this configuration.
 * 
 * @author Stefan Widmaier
 */
public class ModelExplorerConfiguration {

    private HashSet<Class<? extends IIpsElement>> allowedIpsElementTypes = new HashSet<Class<? extends IIpsElement>>();

    private HashSet<Class<? extends IResource>> allowedResourceTypes = new HashSet<Class<? extends IResource>>();

    private final IpsObjectType[] allowedIpsObjectTypes;

    /**
     * Constructs a ModelExplorerConfiguration that allows the given list of IpsElement types and
     * the given list of resource-types. <code>IpsProject</code>,
     * <code>IpsPackageFragmentRoot</code> and <code>IpsPackageFragment</code> are allowed by
     * default.
     */
    public ModelExplorerConfiguration(IpsObjectType[] allowedIpsObjectTypes) {
        this.allowedIpsObjectTypes = allowedIpsObjectTypes;
        // add default allowed types
        allowedIpsElementTypes.add(IIpsProject.class);
        allowedIpsElementTypes.add(IIpsPackageFragmentRoot.class);
        allowedIpsElementTypes.add(IIpsPackageFragment.class);

        // hard coded IpsObjectParts. Use a extensible mechanism if there will be any
        allowedIpsElementTypes.add(IProductCmptGeneration.class);
        allowedIpsElementTypes.add(IAttribute.class);
        allowedIpsElementTypes.add(IEnumAttribute.class);
        allowedIpsElementTypes.add(IAssociation.class);
        allowedIpsElementTypes.add(IMethod.class);
        allowedIpsElementTypes.add(IColumn.class);
        allowedIpsElementTypes.add(ITableStructureUsage.class);

        allowedResourceTypes.add(IFolder.class);
        allowedResourceTypes.add(IFile.class);
        allowedResourceTypes.add(IProject.class);
    }

    /**
     * Returns true if the given IpsElement's class is allowed by this configuration, false
     * otherwise.
     */
    public boolean isAllowedIpsElement(IIpsElement type) {
        if (type instanceof IIpsSrcFile) {
            return isAllowedIpsSrcFile((IIpsSrcFile)type);
        }
        if (type instanceof IIpsObject) {
            return isAllowedIpsElementType(((IIpsObject)type).getIpsObjectType());
        } else {
            for (Class<? extends IIpsElement> allowedElement : allowedIpsElementTypes) {
                if (allowedElement.isAssignableFrom(type.getClass())) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isAllowedIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        IpsObjectType ipsObjectType = ipsSrcFile.getIpsObjectType();
        if (ipsObjectType == null) {
            return false;
        }

        return isAllowedIpsElementType(ipsSrcFile.getIpsObjectType());
    }

    /**
     * Returns true if the given type is allowed by this configuration, false otherwise. This method
     * also checks if superclasses an implemented interfaces of the given class are allowed, and in
     * that case returns true.
     */
    public boolean isAllowedIpsElementType(IpsObjectType type) {
        for (IpsObjectType ipsObjectType : allowedIpsObjectTypes) {
            if (ipsObjectType == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given Objects's class is allowed by this configuration, false otherwise.
     */
    public boolean isAllowedResource(IResource type) {
        return isAllowedResourceType(type.getClass());
    }

    /**
     * Returns true if the given type is allowed by this configuration, false otherwise. This method
     * also checks if superclasses an implemented interfaces of the given class are allowed, and in
     * that case returns true.
     */
    public boolean isAllowedResourceType(Class<? extends IResource> type) {
        return isAllowedType(type, allowedResourceTypes);
    }

    /**
     * Returns true if the given class, one of its superclasses or implemented interfaces are
     * contained in the given set.
     */
    private <T> boolean isAllowedType(Class<? extends T> type, HashSet<Class<? extends T>> allowedTypes) {
        for (Iterator<Class<? extends T>> iter = allowedTypes.iterator(); iter.hasNext();) {
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
