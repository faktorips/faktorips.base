/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.util.GregorianCalendar;

/**
 * An IPS object generation contains the object's state for a period of time.
 */
public interface IIpsObjectGeneration extends IIpsObjectPart {

    String PROPERTY_VALID_FROM = "validFrom"; //$NON-NLS-1$

    /**
     * The name of the XML tag used if this object is saved to XML.
     */
    String TAG_NAME = "Generation"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "IPSOBJECTGEN-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the valid-from date of this generation is invalid.
     */
    String MSGCODE_INVALID_VALID_FROM = MSGCODE_PREFIX + "InvalidValidFromDate"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the valid-from date of this generation is invalid.
     */
    String MSGCODE_INVALID_VALID_FROM_DUPLICATE_GENERATION = MSGCODE_PREFIX
            + "InvalidValidFromDateDuplicateGeneration"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the valid-from date of this generation has a invalid
     * format.
     */
    String MSGCODE_INVALID_FORMAT_VALID_FROM = MSGCODE_PREFIX + "InvalidValidFormatFromDate"; //$NON-NLS-1$

    /**
     * Returns the object this is a generation of.
     */
    ITimedIpsObject getTimedIpsObject();

    /**
     * Returns the generation number. The generation number is defined by the ordering of a timed
     * IPS object's generations by valid from date. The oldest generation is the first generation
     * returned and has the number 1.
     */
    int getGenerationNo();

    /**
     * Returns the point in time from that on this generation contains the object's data.
     */
    GregorianCalendar getValidFrom();

    /**
     * Sets the new point in time from that on this generation contains the object's data.
     * 
     * @throws NullPointerException if valid from is null.
     */
    void setValidFrom(GregorianCalendar validFrom);

    /**
     * Returns <code>Boolean.TRUE</code> if the valid from date is in the past, otherwise
     * <code>Boolean.FALSE</code>. Returns <code>null</code> if valid from is <code>null</code>.
     */
    Boolean isValidFromInPast();

    /**
     * Returns the date this generations is no longer valid or null, if this generation is valid
     * forever.
     */
    GregorianCalendar getValidTo();

    /**
     * Returns the generation previous to this one. The order is defined by the generations' valid
     * from date.
     */
    IIpsObjectGeneration getPreviousByValidDate();

    /**
     * Returns the generation succeeding this one. The order is defined by the generations' valid
     * from date.
     */
    IIpsObjectGeneration getNextByValidDate();

}
