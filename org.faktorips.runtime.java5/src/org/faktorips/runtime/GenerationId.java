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

package org.faktorips.runtime;

import org.faktorips.runtime.internal.DateTime;

/**
 * The identification of a generation consists of the qualified product component name
 * and the valid from date.
 *  
 * @author Jan Ortmann
 */
public class GenerationId implements Comparable<GenerationId> {
    
    private String qName;
    private DateTime validFrom;
    private int hashCode;
    
    public GenerationId(String name, DateTime validFrom) {
        this.qName = name;
        this.validFrom = validFrom;
        this.hashCode = qName.hashCode() * 17 + validFrom.hashCode();
    }

    /**
     * @return Returns the qName.
     */
    public String getQName() {
        return qName;
    }

    /**
     * @return Returns the validFrom.
     */
    public DateTime getValidFrom() {
        return validFrom;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof GenerationId)) {
            return false;
        }
        if (obj.hashCode()!=hashCode) {
            return false; // optimization
        }
        GenerationId other = (GenerationId)obj;
        return qName.equals(other.qName) && validFrom.equals(other.validFrom);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return qName + " " + validFrom.toIsoFormat();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(GenerationId other) {
        int c = qName.compareTo(other.qName);
        if (c!=0) {
            return c;
        }
        return validFrom.compareTo(other.validFrom);
    }
}
