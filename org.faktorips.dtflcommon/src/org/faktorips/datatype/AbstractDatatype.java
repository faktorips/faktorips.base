/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.util.message.MessageList;

/**
 * Abstract super class for Datatype implementations.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDatatype implements Datatype {

    public MessageList checkReadyToUse() {
        return new MessageList();
    }

    public boolean isVoid() {
        return false;
    }

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
    public int compareTo(Datatype o) {
        Datatype type = o;
        return getQualifiedName().compareTo(type.getQualifiedName());
    }

    public boolean hasNullObject() {
        return false;
    }

}
