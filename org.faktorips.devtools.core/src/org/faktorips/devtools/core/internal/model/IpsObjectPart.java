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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.w3c.dom.Element;


/**
 * 
 */
public abstract class IpsObjectPart extends IpsObjectPartContainer implements IIpsObjectPart {
    
    private String description = ""; //$NON-NLS-1$
    private int id;
    
    protected IpsObjectPart(IIpsObject parent, int id) {
        super(parent, ""); //$NON-NLS-1$
        this.id = id;
    }
    
    protected IpsObjectPart(IIpsObjectPart parent, int id) {
        super(parent, ""); //$NON-NLS-1$
        this.id = id;
    }
    
    /**
     * Constructor for testing purposes.
     */
    protected IpsObjectPart() {
    }
    
    /**
     * {@inheritDoc}
     */
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }
    
    /** 
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() {
        IIpsElement element = this;
        while (element!=null) {
            element = element.getParent();
            if (element instanceof IIpsObject) {
                return (IIpsObject)element;
            }
        }
        throw new RuntimeException("Can't get IpsObject for " + this); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDescription(String newDescription) {
        ArgumentCheck.notNull(description, this);
        String oldDescription = description;
        this.description = newDescription;
        valueChanged(oldDescription, newDescription);
    }

    /**
     * {@inheritDoc}
     */
    protected void updateSrcFile() {
        ((IpsObject)getIpsObject()).updateSrcFile();
    }
    
    /** 
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return null;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public boolean isValid() throws CoreException {
		return getValidationResultSeverity()!=Message.ERROR;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getValidationResultSeverity() throws CoreException {
		return validate().getSeverity();
	}

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
    	if (id != null) {
    		this.id = id.intValue();
    	}
    	else {
	        String s = element.getAttribute(PROPERTY_ID);
	        if (!StringUtils.isEmpty(s)) {
	            this.id = Integer.parseInt(s);
	        } // else keep the id set in the constructor. migration for old files without id!
    	}
    	description = DescriptionHelper.getDescription(element);
    }
    
    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_ID, "" + id); //$NON-NLS-1$
        DescriptionHelper.setDescription(element, description);
    }
    
    /**
     * Empty default-implementation.
     * 
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
    }
    
    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
    }

    /**
     * {@inheritDoc}
     * Two parts are equal if the have the same parent and the same id.
     */
    public boolean equals(Object o) {
        if (!(o instanceof IIpsObjectPart)) {
            return false;
        }
        IIpsObjectPart other = (IIpsObjectPart)o;
        return other.getId()==getId() 
        	&& ( (parent==null && other.getParent()==null)
        		|| ( parent!=null && parent.equals(other.getParent()) ) );
    	
    }
    
    /**
     * Mark ipsObjectParts as not adaptable. This prevents the CVSDecorator from displaying
     * decorations for ipsobjectparts in ModelExplorer and ProductExplorer.
     * {@inheritDoc}
     */
    public Object getAdapter(Class adapter) {
        return null;
    }
}
