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
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.message.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public abstract class IpsObject extends IpsObjectPartContainer implements IIpsObject {
    
    private String description = ""; //$NON-NLS-1$

    protected IpsObject(IIpsSrcFile file) {
        super(file, ""); //$NON-NLS-1$
    }
    
    /**
     * Constructor for testing purposes.
     */
    protected IpsObject() {
    }

    /** 
     * Overridden.
     */
    public IIpsPackageFragment getIpsPackageFragment() {
        return getIpsSrcFile().getIpsPackageFragment();
    }

    /**
     * Overridden.
     */
    public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
    }

    /**
     * Overridden.
     */
    public String getQualifiedName() {
        String folderName = getParent().getParent().getName();
        if (folderName.equals("")) { //$NON-NLS-1$
            return getName();
        }
        return folderName + '.' + getName();
    }

    /**
     * Overridden.
     */
    public String getName() {
        String filename = getParent().getName();
        int index = filename.indexOf('.');
        if (index==-1) {
            throw new RuntimeException("filename has no extension: " + filename); //$NON-NLS-1$
        }
        return filename.substring(0, index);
    }

    /** 
     * Overridden.
     */
    public IResource getCorrespondingResource() {
        return null;
    }
    
    /**
     * Overridden.
     */
    public IIpsSrcFile getIpsSrcFile() {
        if (getParent() instanceof IIpsSrcFile) {
            return (IIpsSrcFile)getParent();    
        }
        return null;
    }
    
    /**
     * Overridden.
     */
    public Image getImage() {
        return getIpsObjectType().getImage();
    }

    /** 
     * Overridden.
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        objectHasChanged();
    }

    /** 
     * Overridden.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Notifies the model about the change.  
     */
    protected void objectHasChanged() {
        IpsModel model = (IpsModel)getIpsModel();
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content!=null) {
            content.markAsModified();
        }
        model.ipsSrcFileHasChanged((IIpsSrcFile)getIpsSrcFile());
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
     * Overridden.
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        return new QualifiedNameType[0];
    }
    
    /**
     * Overridden.
     */
    protected final Element createElement(Document doc) {
        return doc.createElement(getIpsObjectType().getXmlElementName());
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        DescriptionHelper.setDescription(element, description);
    }
    
    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element element) {
        super.initFromXml(element);
    }

    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        description = DescriptionHelper.getDescription(element);
    }
    
    public String toString() {
        if (getParent()==null) {
            return "unnamed object"; // can only happen in test cases.  //$NON-NLS-1$
        }
        return super.toString();
    }
}
