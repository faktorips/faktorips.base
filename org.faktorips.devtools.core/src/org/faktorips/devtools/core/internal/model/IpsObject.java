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

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getIpsPackageFragment() {
        return getIpsSrcFile().getIpsPackageFragment();
    }

    /**
     * {@inheritDoc}
     */
    public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        String folderName = getParent().getParent().getName();
        if (folderName.equals("")) { //$NON-NLS-1$
            return getName();
        }
        return folderName + '.' + getName();
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile getIpsSrcFile() {
        if (getParent() instanceof IIpsSrcFile) {
            return (IIpsSrcFile)getParent();    
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return getIpsObjectType().getImage();
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Notifies the model about the change.  
     */
    protected void objectHasChanged() {
        ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
        objectHasChanged(event);
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
    public QualifiedNameType[] dependsOn() throws CoreException {
        return new QualifiedNameType[0];
    }
    
    /**
     * {@inheritDoc}
     */
    protected final Element createElement(Document doc) {
        return doc.createElement(getIpsObjectType().getXmlElementName());
    }
    
    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        validateNamingConventions(list, getName(), PROPERTY_NAME);
    }
    
    /**
     *  Validate the naming conventions for the given name.
     *  
     *  @param list The list the message will be added if the name is invalid
     *  @param nameToValidate The name which will be validated against the naming conventions
     *  @param property The property which contains the name, the message will be related to this property
     */
    protected void validateNamingConventions(MessageList list, String nameToValidate, String property) throws CoreException {
        MessageList mlForNameValidation = new MessageList();
        mlForNameValidation.add(getIpsProject().getNamingConventions().validateUnqualifiedIpsObjectName(getIpsObjectType(), nameToValidate));
        for (Iterator iter = mlForNameValidation.iterator(); iter.hasNext();) {
            // create new messages related to this object and the given property
            Message msg = (Message)iter.next();
            Message newMsg = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, property);
            list.add(newMsg);
        }
    }
}
