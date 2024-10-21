/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
        qName = name;
        this.validFrom = validFrom;
        hashCode = qName.hashCode() * 17 + validFrom.hashCode();
    }

    public String getQName() {
        return qName;
    }

    public DateTime getValidFrom() {
        return validFrom;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GenerationId other) || (obj.hashCode() != hashCode)) {
            return false;
        }
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

    @Override
    public int compareTo(GenerationId other) {
        int c = qName.compareTo(other.qName);
        if (c != 0) {
            return c;
        }
        return validFrom.compareTo(other.validFrom);
    }

}
