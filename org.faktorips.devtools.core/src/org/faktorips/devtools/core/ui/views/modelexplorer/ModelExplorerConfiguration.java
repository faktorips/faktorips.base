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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * Configuration class for <code>ModelExlporer</code>s, that can be asked if a specific
 * class or object is allowed and should be displayed in the explorer. 
 * The configuration also checks if the given object or class is subclass of an allowed 
 * type (class or interface). Thus the published interfaces instead of the concrete classes 
 * should be used when instanciating this configuration.
 * @author Stefan Widmaier
 */
public class ModelExplorerConfiguration {
	
	private HashSet allowedIpsElementTypes = new HashSet();

	private HashSet allowedResourceTypes = new HashSet();
	
	/**
	 * Constructs a default ModelExplorerConfiguration that allows all structural
	 * IpsElements that need to be displayed. The created instance allows types:
	 * <code>IpsProject</code>, <code>IpsPackageFragmentRoot</code>
	 * and <code>IpsPackageFragment</code>.
	 */
	private ModelExplorerConfiguration() {
		this(new Class[0], new Class[0]);
	}

	/**
	 * Constructs a ModelExplorerConfiguration that allows the given list of
	 * IpsElement types and the given list of resource-types. <code>IpsProject</code>, 
	 * <code>IpsPackageFragmentRoot</code> and <code>IpsPackageFragment</code> are 
	 * allowed by default. 
	 */
	public ModelExplorerConfiguration(Class[] ipsElementTypes, Class[] resourceTypes) {
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
	 * Returns true if the given IpsElement's class is allowed by this
	 * configuration, false otherwise.
	 */
	public boolean isAllowedIpsElement(Object type) {
		return isAllowedIpsElementType(type.getClass());
	}
	/**
	 * Returns true if the given type is allowed by this
	 * configuration, false otherwise. This method also checks if 
	 * superclasses an implemented interfaces of the given class are
	 * allowed, and in that case returns true.
	 */
	public boolean isAllowedIpsElementType(Class type) {
		return isAllowedType(type, allowedIpsElementTypes);
	}

	/**
	 * Returns true if the given Objects's class is allowed by this
	 * configuration, false otherwise.
	 */
	public boolean isAllowedResource(Object type) {
		return isAllowedResourceType(type.getClass());
	}
	
	/**
	 * Returns true if the given type is allowed by this
	 * configuration, false otherwise. This method also checks if 
	 * superclasses an implemented interfaces of the given class are
	 * allowed, and in that case returns true.
	 */
	public boolean isAllowedResourceType(Class type) {
		return isAllowedType(type, allowedResourceTypes);
	}
	
	/**
	 * Returns true if the given class, one of its superclasses
	 * or implemented interfaces are contained in the given set.
	 */
	private boolean isAllowedType(Class type, HashSet allowedTypes){
		for (Iterator iter = allowedTypes.iterator(); iter.hasNext();) {
			Class allowedClass = (Class) iter.next();
			if(allowedClass.isAssignableFrom(type)){
				return true;
			}
		}
		return false;
	}

    /**
     * Returns true if the given object is of type <code>IProject</code> or
     * <code>IIpsProject</code>, false otherwise.
     */
    public boolean representsProject(Object item) {
        return item instanceof IIpsProject || item instanceof IProject;
    }

    /**
     * Returns true if the given object is of type <code>IFolder</code>,
     * <code>IIpsPackageFragmentRoot</code> or <code>IIpsPackageFragment</code>, false
     * otherwise.
     */
    public boolean representsFolder(Object item) {
        return item instanceof IIpsPackageFragment || item instanceof IIpsPackageFragmentRoot
                || item instanceof IFolder;
    }

    /**
     * Returns true if the given object is of type <code>IProject</code> or
     * <code>IIpsProject</code>, false otherwise.
     */
    public boolean representsFile(Object item) {
        if (item instanceof IIpsObject) {
            return true;
        }
        if (item instanceof IFile) {
            return true;
        }
        if (item instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot)item;
            return root.isBasedOnIpsArchive();
        }
        return false;
    }
}
