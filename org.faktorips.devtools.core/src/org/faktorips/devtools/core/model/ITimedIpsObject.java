package org.faktorips.devtools.core.model;

import java.util.GregorianCalendar;

/**
 * An IPS object that changes over time. The changes are represented by generations.
 * Each generation contains the object's state for a certain period of time.
 * These periods don't overlap and there is no gap between them. 
 */
public interface ITimedIpsObject extends IIpsObject {
    
    /**
     * Returns true if this object's data changes at the indicated point in time,
     * otherwise false.
     */
    public boolean changesOn(GregorianCalendar date);
    
    /**
     * Returns the object's generations. Each generation contains the object's data
     * for a certain (none overlapping) period of time. The returned generations are
     * ordered by their valid from date with the oldest generations coming first.  
     */
    public IIpsObjectGeneration[] getGenerations();
    
    
    /**
     * Returns the generation effective on the given date. That is the generation
     * which's effective date lies before the given date and among all generations with such an
     * effective date, this is the one with the most recent effective date.
     * Returns <code>null if no generation is found or date is <code>null</code>.
     */    
    public IIpsObjectGeneration findGenerationEffectiveOn(GregorianCalendar date);
    
    /**
     * Returns the generation identified by the given effective date, that is 
     * the generation which has the same effective date.
     * <p>
     * Returns <code>null</code> if no generation is found or date is <code>null</code>.
     * <p>
     * Note: This method returns <code>null</code> if it does not find a generation
     * with the <b>exact</b> effective date. Given a two generations with effective
     * dates 01-01-2004 and 01-01-2005 this method would return the first generation
     * if 01-01-2004 is passed as argument and the second generation if 01-01-2005
     * is passed in. For all other dates the methods would return <code>null</code> 
     */
    public IIpsObjectGeneration getGenerationByEffectiveDate(GregorianCalendar date);
    
    /**
     * Creates a new, empty generation.
     */
    public IIpsObjectGeneration newGeneration();

    /**
     * Creates a new generation filled with data from the generation matching the given valid from date.
     */
	public IIpsObjectGeneration newGeneration(GregorianCalendar validFrom);

    /**
     * Returns the number of generation.
     */
    public int getNumOfGenerations();
    
    
}
