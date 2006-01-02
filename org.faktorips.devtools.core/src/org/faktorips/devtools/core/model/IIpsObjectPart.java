package org.faktorips.devtools.core.model;

import org.faktorips.util.memento.MementoSupport;



/**
 *
 */
public interface IIpsObjectPart extends IIpsElement, Validatable, XmlSupport, MementoSupport, Described, IExtensionPropertyAccess {
    
    public final static String PROPERTY_DESCRIPTION = "description";
    public final static String PROPERTY_ID = "id";
    
    /**
     * Returns the object this part belongs to.
     */
    public IIpsObject getIpsObject();
    
    /**
     * The part's id that uniquely identifies it in it's parent.
     */
    public int getId();

    /**
     * Deletes the part. 
     */
    public void delete();
    
    /**
     * Returns the description.
     */
    public String getDescription();
    
    /**
     * Sets the new description. 
     * 
     * @throws IllegalArgumentException if description is null.
     */
    public void setDescription(String newDescription);
    
    
}
