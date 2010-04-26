/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

/**
 * Represents a range. This class is used during the process of reading data of a range from an xml
 * representation into memory and create <code>org.faktorips.valueset.Range</code> implementation
 * instances.
 * 
 * @author Peter Erzberger
 */
public class Range {

    private String lower;
    private String upper;
    private String step;
    private boolean containsNull;

    /**
     * Creates a new Range.
     */
    public Range(String lower, String upper, String step, boolean containsNull) {
        super();
        this.lower = lower;
        this.upper = upper;
        this.step = step;
        this.containsNull = containsNull;
    }

    /**
     * @return Returns if this range contains null
     */
    public boolean containsNull() {
        return containsNull;
    }

    /**
     * @return Returns the lower bound of this range
     */
    public String getLower() {
        return lower;
    }

    /**
     * @return Returns the upper bound of this range
     */
    public String getUpper() {
        return upper;
    }

    /**
     * @return Returns the step of this range
     */
    public String getStep() {
        return step;
    }
}
