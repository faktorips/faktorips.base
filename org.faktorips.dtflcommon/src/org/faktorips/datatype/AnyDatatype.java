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
 * A datatype that is used to represent any type of data, similiar to <code>java.lang.Object.</code>
 * 
 * @author Jan Ortmann
 */
public class AnyDatatype implements Datatype {

    public static final AnyDatatype INSTANCE = new AnyDatatype();

    private AnyDatatype() {
        super();
    }

    public MessageList validate() {
        return new MessageList();
    }

    @Override
    public String getName() {
        return "any"; //$NON-NLS-1$
    }

    @Override
    public String getQualifiedName() {
        return "any"; //$NON-NLS-1$
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public int compareTo(Datatype o) {
        return DatatypeComparator.doCompare(this, o);
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

}
