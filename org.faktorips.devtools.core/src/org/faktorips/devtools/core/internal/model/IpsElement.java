package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsProject;



/**
 *
 */
public abstract class IpsElement implements IIpsElement {

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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        return name;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getParent()
     */
    public final IIpsElement getParent() {
        return parent;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#exists()
     */
    public boolean exists() {
        if (!getParent().exists()) {
            return false;
        }
        if (getCorrespondingResource()==null) {
            return true;    
        }
        return getCorrespondingResource().exists();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getEnclosingResource()
     */
    public IResource getEnclosingResource() {
        IResource resource = getCorrespondingResource();
        if (resource!=null) {
            return resource;
        }
        return getParent().getEnclosingResource();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsProject#getIpsModel()
     */
    public IIpsModel getIpsModel() {
        return IpsPlugin.getDefault().getIpsModel();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getIpsProject()
     */
    public IIpsProject getIpsProject() {
        if (getParent()==null) {
            return null;
        }
        return getParent().getIpsProject();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() throws CoreException {
        return NO_CHILDREN;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#hasChildren()
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
        return getParent().toString() + "/" + getName();
    }


}
