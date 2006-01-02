package org.faktorips.devtools.core.model;

import java.util.GregorianCalendar;

/**
 * An IPS object generation contains the object's state for
 * a period of time. 
 */
public interface IIpsObjectGeneration extends IIpsObjectPart {
    
    public final static String PROPERTY_VALID_FROM = "validFrom";
    
    /**
     * Returns the object this is a generation of.
     */
    public ITimedIpsObject getTimedIpsObject();
    
    /**
     * Returns the generation number. The generation number is defined by the ordering of a timed ips object's
     * generations by valid from date. The oldest generation is the first generation returned and 
     * has the number 1.  
     */
    public int getGenerationNo();
    
    /**
     * Returns the point in time from that on this generation 
     * contains the object's data.
     */
    public GregorianCalendar getValidFrom();
    
    /**
     * Sets the new point in time from that on this generation contains
     * the object's data.
     * 
     * @throws NullPointerException if valid from is null.
     */
    public void setValidFrom(GregorianCalendar validFrom);
}
