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

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.util.XmlUtil;
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
        updateSrcFile();
    }

    /** 
     * Overridden.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Updates the source file's (string) content with the object's xml text representation.
     */
    protected void updateSrcFile() {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        try {
            Element element = toXml(doc);
            String encoding = getIpsProject()==null?"UTF-8":getIpsProject().getXmlFileCharset(); //$NON-NLS-1$
            String newContents = XmlUtil.nodeToString(element, encoding);
            ((IpsSrcFile)getParent()).setContentsInternal(newContents);
        } catch (TransformerException e) {
            throw new RuntimeException(e); 
            // This is a programing error, rethrow as runtime exception
        } catch (CoreException ce) {
            throw new RuntimeException(ce); 
            // Can't happen due to io exceptions as the source file's content is 
            // already loaded. Everything else is a programing error.
        }
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
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        validateThis(result);
        validateExtensionProperties(result);
        IIpsElement[] children = getChildren();
        for (int i=0; i<children.length; i++) {
            ((IpsObjectPart)children[i]).validate(result);
        }
        return result;
    }
    
    /**
     * Validates the object and reports invalid states by adding 
     * validation messages to the list. This is an application of the collecting
     * parameter pattern.
     * 
     * @throws NullPointerException if list is null.
     */
    protected void validateThis(MessageList list) throws CoreException {
        return;
    }
    
    protected boolean valueChanged(Object oldValue, Object newValue) {
        boolean changed = false;
        if (oldValue!=null) {
            changed = !oldValue.equals(newValue);
        } else {
            changed = newValue!=null;
        }
        if (changed) {
            updateSrcFile();
        }
        return changed;
    }
    
    protected boolean valueChanged(boolean oldValue, boolean newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            updateSrcFile();
        }
        return changed;
    }
    
    protected boolean valueChanged(int oldValue, int newValue) {
        boolean changed = oldValue == newValue;
        if (changed) {
            updateSrcFile();
        }
        return changed;
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
