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

package org.faktorips.devtools.core.model;

import java.util.GregorianCalendar;

/**
 * An IPS object generation contains the object's state for
 * a period of time. 
 */
public interface IIpsObjectGeneration extends IIpsObjectPart {
    
    public final static String PROPERTY_VALID_FROM = "validFrom"; //$NON-NLS-1$
    
    /**
     * The name of the xml-tag used if this object is saved to xml.
     */
    public final static String TAG_NAME = "Generation"; //$NON-NLS-1$
    

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
     * Returns the date this generations is no longer valid or null, if this generation
     * is valid forever.
     */
	public GregorianCalendar getValidTo();

    /**
     * Sets the new point in time from that on this generation contains
     * the object's data.
     * 
     * @throws NullPointerException if valid from is null.
     */
    public void setValidFrom(GregorianCalendar validFrom);

    /**
     * copies structure and values from the given source.
     */
	public void initFromGeneration(IIpsObjectGeneration source);

	/**
	 * Returns the generation previous to this one. The order is determined by
	 * the generation number and not by the valid from and valid to dates.
	 */
	public IIpsObjectGeneration getPrevious();
	
	/**
	 * Returns the generation succeding this one. The order is determined by
	 * the generation number and not by the valid from and valid to dates.
	 */
	public IIpsObjectGeneration getNext();
	
}
