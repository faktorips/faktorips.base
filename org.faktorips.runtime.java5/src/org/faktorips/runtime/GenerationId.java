/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

import org.faktorips.runtime.internal.DateTime;

/**
 * The identification of a generation consists of the qualified product component name and the valid
 * from date.
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

    public String getQName() {
        return qName;
    }

    public DateTime getValidFrom() {
        return validFrom;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GenerationId)) {
            return false;
        }
        if (obj.hashCode() != hashCode) {
            return false; // optimization
        }
        GenerationId other = (GenerationId)obj;
        return qName.equals(other.qName) && validFrom.equals(other.validFrom);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return qName + " " + validFrom.toIsoFormat();
    }

    public int compareTo(GenerationId other) {
        int c = qName.compareTo(other.qName);
        if (c != 0) {
            return c;
        }
        return validFrom.compareTo(other.validFrom);
    }

}
