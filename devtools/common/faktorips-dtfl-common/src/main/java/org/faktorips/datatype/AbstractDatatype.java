/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.datatype.util.DatatypeComparator;
import org.faktorips.runtime.MessageList;

/**
 * Abstract super class for Datatype implementations.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDatatype implements Datatype {

    public MessageList checkReadyToUse() {
        return new MessageList();
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return this instanceof EnumDatatype;
    }

    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype)) {
            return false;
        }
        return getQualifiedName().equals(((Datatype)o).getQualifiedName());
    }

    /**
     * Returns true if the dataType is instance of AnyDatatype otherwise it call
     * {@link #equals(Object)}.
     * 
     */
    public boolean matchDatatype(Datatype datatype) {
        if (datatype instanceof AnyDatatype) {
            return true;
        }
        return equals(datatype);
    }

    /**
     * Returns the type's name.
     */
    @Override
    public String toString() {
        return getQualifiedName();
    }

    /**
     * Compares the two type's alphabetically by their name.
     */
    @Override
    public int compareTo(Datatype o) {
        return DatatypeComparator.doCompare(this, o);
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

}
