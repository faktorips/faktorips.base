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
import java.util.List;

/**
 * An IPS object that changes over time. The changes are represented by generations. Each generation
 * contains the object's state for a certain period of time. These periods don't overlap and there
 * is no gap between them.
 */
public interface ITimedIpsObject extends IIpsObject {

    /**
     * Name of the property for the valid-to date for this product component
     */
    String PROPERTY_VALID_TO = "validTo"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TIMEDIPSOBJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the valid-to date for this object is invalid
     */
    String MSGCODE_INVALID_VALID_TO = MSGCODE_PREFIX + "InvalidValidToDate"; //$NON-NLS-1$

    /**
     * Returns true if this object's data changes at the indicated point in time, otherwise false.
     */
    boolean changesOn(GregorianCalendar date);

    /**
     * Get all generations like they are stored in this container. The generations have no concrete
     * order. If you want the generation ordered by valid date use
     * {@link #getGenerationsOrderedByValidDate()} instead
     * 
     * @return array with all registered generations
     */
    List<IIpsObjectGeneration> getGenerations();

    /**
     * Returns the object's generations. Each generation contains the object's data for a certain
     * (none overlapping) period of time. The returned generations are ordered by their valid from
     * date with the oldest generations coming first.
     * <p>
     * Generations with valid-from date <code>null</code> are sorted in first place (infinite valid
     * from).
     */
    IIpsObjectGeneration[] getGenerationsOrderedByValidDate();

    /**
     * Returns the index at the specified index.
     * 
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    IIpsObjectGeneration getGeneration(int index);

    /**
     * @return Returns the first generation of this timed IPS object or <code>null</code>, if no
     *             generation exists.
     */
    IIpsObjectGeneration getFirstGeneration();

    /**
     * Returns the generation effective on the given date. That is the generation which's effective
     * date lies before the given date and among all generations with such an effective date, this
     * is the one with the most recent effective date. Returns <code>null</code> if no generation is
     * found. Returns null if date is <code>null</code>
     * 
     * @since 3.7
     */
    IIpsObjectGeneration getGenerationEffectiveOn(GregorianCalendar date);

    /**
     * Returns the generation which is the best-matching generation for the given date.
     * <p>
     * If a generation exists that is effective on the given date, this generation is returned. If
     * no such generation exists, there are two possibilities:
     * <ul>
     * <li>The given date is before the first generation: the first generation is returned
     * <li>The given date is after the latest generation: the latest generation is returned
     * </ul>
     * <p>
     * Returns null if no generation exists at all.
     * 
     * @param date the date for which to retrieve the best matching generation
     */
    IIpsObjectGeneration getBestMatchingGenerationEffectiveOn(GregorianCalendar date);

    /**
     * Returns the generation identified by the given effective date, that is the generation which
     * has the same effective date.
     * <p>
     * Returns <code>null</code> if no generation is found or date is <code>null</code>.
     * <p>
     * Note: This method returns <code>null</code> if it does not find a generation with the
     * <strong>exact</strong> effective date. Given a two generations with effective dates
     * 01-01-2004 and 01-01-2005 this method would return the first generation if 01-01-2004 is
     * passed as argument and the second generation if 01-01-2005 is passed in. For all other dates
     * the methods would return <code>null</code>
     */
    IIpsObjectGeneration getGenerationByEffectiveDate(GregorianCalendar date);

    /**
     * Returns the latest generation of this object or {@code null}, if no generation exists.
     */
    IIpsObjectGeneration getLatestGeneration();

    /**
     * Creates a new, empty generation.
     */
    IIpsObjectGeneration newGeneration();

    /**
     * Creates a new generation that is valid from the specified validFrom date. If the timed IPS
     * object already has a generation that is valid on that date, the new generation is initialized
     * with the data from that (existing) generation.
     * 
     * @throws NullPointerException if validFrom is <code>null</code>.
     */
    IIpsObjectGeneration newGeneration(GregorianCalendar validFrom);

    /**
     * Returns the number of generation.
     */
    int getNumOfGenerations();

    /**
     * Returns the date from which this {@link ITimedIpsObject} is valid. This needs to be the same
     * date as the first generation is valid from.
     * 
     * @return The valid from of this product component.
     */
    GregorianCalendar getValidFrom();

    /**
     * Set the date from which this product component is valid. This method automatically adjusts
     * the valid from date of the first generation.
     * 
     * @param validFrom The new valid from date for this {@link ITimedIpsObject}
     */
    void setValidFrom(GregorianCalendar validFrom);

    /**
     * Returns the date when this product component expires.
     * 
     * @return The date this IpsObject is valid on or <code>null</code> if this IpsObject is valid
     *             forever.
     */
    GregorianCalendar getValidTo();

    /**
     * Set the date this IpsObject is valid to.
     */
    void setValidTo(GregorianCalendar validTo);

    /**
     * Reassigns the generations of this timed IPS object in such a way that all generations present
     * that expire prior to the new date, will be deleted and the first generation in this object
     * will be set to the new date given.
     * 
     * For example, consider an ITimedIpsObject with three {@link IIpsObjectGeneration
     * IIpsObjectGenerations}:
     * <ul>
     * <li>G1: valid from 2010-01-01</li>
     * <li>G2: valid from 2011-01-01</li>
     * <li>G3: valid from 2012-01-01</li>
     * </ul>
     * 
     * Calling reassignGenerations on this object with 2011-06-01 as new date, would produce these
     * generations:
     * <ul>
     * <li>G2: valid from 2011-06-01</li>
     * <li>G3: valid from 2012-01-01</li>
     * </ul>
     * 
     * The original G1 would be deleted, because it expired before the new date. The original G2
     * would begin 5 months later, because it was valid on the new date and after the method call,
     * this is the first valid from date. The original G3 would remain unmodified, because it is
     * valid only after the new date.
     * 
     * @param newDate new valid from date of this object
     */
    void reassignGenerations(GregorianCalendar newDate);

    /**
     * Retains only one generation in this object. If a generation exists at the given old date,
     * this generation is retained and set to be valid from the new date. If no such generation
     * exists, a new generation, valid from the new date, is created and retained as the only
     * generation.
     * 
     * For example, consider an ITimedIpsObject with three {@link IIpsObjectGeneration
     * IIpsObjectGenerations}:
     * <ul>
     * <li>G1: valid from 2010-01-01</li>
     * <li>G2: valid from 2011-01-01</li>
     * <li>G3: valid from 2012-01-01</li>
     * </ul>
     * 
     * Calling retainOnlyGeneration on this object with 2011-01-01 as the old and 2011-06-01 as new
     * date, would produce this generation:
     * 
     * <ul>
     * <li>G2: valid from 2011-06-01</li>
     * </ul>
     * 
     * G2 would be the only generation left in this object because it was valid at the oldDate
     * parameter and its valid from would be set to the new date 2011-06-01.
     * 
     * @param oldDate effective date of the generation to retain.
     * @param newDate new effective date of the only generation in this object.
     */
    void retainOnlyGeneration(GregorianCalendar oldDate, GregorianCalendar newDate);

    /**
     * Returns <code>true</code> if this {@link ITimedIpsObject} allows changing over time. If not,
     * <code>false</code> is returned.
     */
    boolean allowGenerations();
}
