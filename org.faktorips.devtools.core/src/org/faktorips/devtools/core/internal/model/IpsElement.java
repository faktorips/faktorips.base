/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsProject;



/**
 *
 */
public abstract class IpsElement implements IIpsElement, IAdaptable {

    protected String name;
    protected IIpsElement parent;
    
    final static IIpsElement[] NO_CHILDREN = new IIpsElement[0];
    
    IpsElement(IIpsElement parent, String name) {
        this.parent = parent;
        this.name = name;
    }
    
    /**
     * Constructor for testing purposes.
     */
    IpsElement() {
    }

    /**
     * This method does not query any <code>AdapterManager</code>s to get the 
     * adapter - if the requested adapter is an <code>IResource</code>, the
     * enclosing resource is returned.
     * 
     * {@inheritDoc}
     */
    public Object getAdapter(Class adapter) {
    	if (adapter.equals(IResource.class)) {
    		return this.getEnclosingResource();
    	}
		return null;
	}

	/** 
     * Overridden.
     */
    public String getName() {
        return name;
    }

    /** 
     * Overridden.
     */
    public final IIpsElement getParent() {
        return parent;
    }
    
    /**
     * Overridden.
     */
    public boolean exists() {
        if (!getParent().exists()) {
            return false;
        }
        if (getCorrespondingResource()==null) {
        	// if no corresponding resource exists, the EnclosingResource.exists() is handled
        	// by calling getParent().exists() above. So if we have arrived here, we have
        	// to return true (the parent exists) to avoid a NullPointerException in the
        	// rest of the code.
            return true;    
        }
        return getCorrespondingResource().exists();
    }
    
    /** 
     * Overridden.
     */
    public IResource getEnclosingResource() {
        IResource resource = getCorrespondingResource();
        if (resource!=null) {
            return resource;
        }
        return getParent().getEnclosingResource();
    }
    
    /**
     * Overridden.
     */
    public IIpsModel getIpsModel() {
        return IpsPlugin.getDefault().getIpsModel();
    }

    /** 
     * Overridden.
     */
    public IIpsProject getIpsProject() {
        if (getParent()==null) {
            return null;
        }
        return getParent().getIpsProject();
    }
    
    /** 
     * Overridden.
     */
    public IIpsElement[] getChildren() throws CoreException {
        return NO_CHILDREN;
    }

    /** 
     * Overridden.
     */
    public boolean hasChildren() throws CoreException {
        return getChildren().length>0;
    }
    
    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof IIpsElement)) {
            return false;
        }
        IIpsElement other = (IIpsElement)o;
        return other.getName().equals(getName()) 
        	&& ( (parent==null && other.getParent()==null)
        		|| ( parent!=null && parent.equals(other.getParent()) ) );
    }
    
    public String toString() {
        if (getParent()==null) {
            return getName();
        }
        return getParent().toString() + "/" + getName(); //$NON-NLS-1$
    }


}
