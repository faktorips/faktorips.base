/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import java.util.GregorianCalendar;

/**
 * An IPS object generation contains the object's state for a period of time.
 */
public interface IIpsObjectGeneration extends IIpsObjectPart {

    public final static String PROPERTY_VALID_FROM = "validFrom"; //$NON-NLS-1$

    /**
     * The name of the XML tag used if this object is saved to XML.
     */
    public final static String TAG_NAME = "Generation"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSOBJECTGEN-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the valid-from date of this generation is invalid.
     */
    public final static String MSGCODE_INVALID_VALID_FROM = MSGCODE_PREFIX + "InvalidValidFromDate"; //$NON-NLS-1$

    /**
     * Returns the object this is a generation of.
     */
    public ITimedIpsObject getTimedIpsObject();

    /**
     * Returns the generation number. The generation number is defined by the ordering of a timed
     * ips object's generations by valid from date. The oldest generation is the first generation
     * returned and has the number 1.
     */
    public int getGenerationNo();

    /**
     * Returns the point in time from that on this generation contains the object's data.
     */
    public GregorianCalendar getValidFrom();

    /**
     * Sets the new point in time from that on this generation contains the object's data.
     * 
     * @throws NullPointerException if valid from is null.
     */
    public void setValidFrom(GregorianCalendar validFrom);

    /**
     * Returns <code>Boolean.TRUE</code> if the valid from date is in the past, otherwise
     * <code>Boolean.FALSE</code>. Returns <code>null</code> if valid from is <code>null</code>.
     */
    public Boolean isValidFromInPast();

    /**
     * Returns the date this generations is no longer valid or null, if this generation is valid
     * forever.
     */
    public GregorianCalendar getValidTo();

    /**
     * Returns the generation previous to this one. The order is defined by the generations' valid
     * from date.
     */
    public IIpsObjectGeneration getPreviousByValidDate();

    /**
     * Returns the generation succeeding this one. The order is defined by the generations' valid
     * from date.
     */
    public IIpsObjectGeneration getNextByValidDate();

}
