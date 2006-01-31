package org.faktorips.devtools.core.model;




/**
 *
 */
public interface IIpsObjectPart extends IIpsObjectPartContainer {
    
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
    
}
