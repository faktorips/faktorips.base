package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IMember;
import org.w3c.dom.Element;


/**
 *
 */
public abstract class Member extends IpsObjectPart implements IMember {

    Member(IIpsObject parent, int id) {
        super(parent, id);
    }

    /**
     * Constructor for testing purposes.
     */
    Member() {
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMember#getIpsObject()
     */
    public IIpsObject getIpsObject() {
        return (IIpsObject)parent;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMember#setName(java.lang.String)
     */
    public void setName(String newName) {
        this.name = newName;
        updateSrcFile();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() {
        return null;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute("name"); //$NON-NLS-1$
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("name", name); //$NON-NLS-1$
    }
    
}
