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
import org.faktorips.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.memento.XmlMemento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public abstract class IpsObject extends IpsObjectPartContainer implements IIpsObject {
    
    private String description = "";

    /**
     * @param parent
     * @param name
     */
    protected IpsObject(IIpsSrcFile file) {
        super(file, "");
    }
    
    /**
     * Constructor for testing purposes.
     */
    protected IpsObject() {
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getIpsPackageFragment()
     */
    public IIpsPackageFragment getIpsPackageFragment() {
        return getIpsSrcFile().getIpsPackageFragment();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObject#getQualifiedNameType()
     */
    public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getQualifiedName()
     */
    public String getQualifiedName() {
        String folderName = getParent().getParent().getName();
        if (folderName.equals("")) {
            return getName();
        }
        return folderName + '.' + getName();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        String filename = getParent().getName();
        int index = filename.indexOf('.');
        if (index==-1) {
            throw new RuntimeException("Not . found in filename!");
        }
        return filename.substring(0, index);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        return null;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getIpsSrcFile()
     */
    public IIpsSrcFile getIpsSrcFile() {
        if (getParent() instanceof IIpsSrcFile) {
            return (IIpsSrcFile)getParent();    
        }
        return null;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return getIpsObjectType().getImage();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.Described#setDescription(java.lang.String)
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.Described#getDescription()
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
            String encoding = getIpsProject()==null?"UTF-8":getIpsProject().getXmlFileCharset();
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
     * Overridden method.
     * @see org.faktorips.util.memento.MementoSupport#newMemento()
     */
    public Memento newMemento() {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        return new XmlMemento(this, toXml(doc));
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.util.memento.MementoSupport#setState(org.faktorips.util.memento.Memento)
     */
    public void setState(Memento memento) {
        if (!memento.getOriginator().equals(this)) {
            throw new IllegalArgumentException("Memento " + memento + " wasn't created by " + this);
        }
        initFromXml(((XmlMemento)memento).getState());
    }

    /**
     * Overridden.
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
     * Overridden IMethod.
     * @throws CoreException 
     *
     * @see org.faktorips.devtools.core.model.IIpsObject#dependsOn()
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        return new QualifiedNameType[0];
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected final Element createElement(Document doc) {
        return doc.createElement(getIpsObjectType().getXmlElementName());
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        DescriptionHelper.setDescription(element, description);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        description = DescriptionHelper.getDescription(element);
    }
    
    public String toString() {
        if (getParent()==null) {
            return "unnamed object"; // can only happen in test cases. 
        }
        return super.toString();
    }
}
