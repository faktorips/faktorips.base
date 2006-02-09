package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.memento.XmlMemento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * 
 */
public abstract class IpsObjectPart extends IpsObjectPartContainer implements IIpsObjectPart {
    
    private String description = "";
    private int id;
    
    protected IpsObjectPart(IIpsObject parent, int id) {
        super(parent, "");
        this.id = id;
    }
    
    protected IpsObjectPart(IIpsObjectPart parent, int id) {
        super(parent, "");
        this.id = id;
    }
    
    /**
     * Constructor for testing purposes.
     */
    protected IpsObjectPart() {
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#getId()
     */
    public int getId() {
        return id;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#getIpsObject()
     */
    public IIpsObject getIpsObject() {
        IIpsElement element = this;
        while (element!=null) {
            element = element.getParent();
            if (element instanceof IIpsObject) {
                return (IIpsObject)element;
            }
        }
        throw new RuntimeException("Can't get IpsObject for " + this);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.Described#getDescription()
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.Described#setDescription(java.lang.String)
     */
    public void setDescription(String newDescription) {
        ArgumentCheck.notNull(description, this);
        String oldDescription = description;
        this.description = newDescription;
        valueChanged(oldDescription, newDescription);
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
    
    
    protected void updateSrcFile() {
        ((IpsObject)getIpsObject()).updateSrcFile();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        return null;
    }
    
    public final MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        validate(result);
        return result;
    }

    /**
     * Validates the object part and reports invalid states by adding 
     * validation messages to the list. This is an application of the collecting
     * parameter pattern.
     * 
     * @throws NullPointerException if list is null.
     */
    protected void validate(MessageList list) throws CoreException {
        validateExtensionProperties(list);
    }

    protected boolean valueChanged(Object oldValue, Object newValue) {
        boolean changed = !ObjectUtils.equals(oldValue, newValue);
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
        boolean changed = oldValue != newValue;
        if (changed) {
            updateSrcFile();
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, int id) {
    	if (id != -1) {
    		this.id = id;
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#newPart(java.lang.String, int)
     */
    protected IIpsObjectPart newPart(String xmlTagName, int id) {
        return null;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_ID, "" + id);
        DescriptionHelper.setDescription(element, description);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reAddPart(org.faktorips.devtools.core.model.IIpsObjectPart)
     */
    protected void reAddPart(IIpsObjectPart part) {
        // TODO Auto-generated method stub

    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reinitPartCollections()
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
}
