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
 * A datatype that is used to represent any type of data, similiar to <code>java.lang.Object.</code>
 * 
 * @author Jan Ortmann
 */
public class AnyDatatype implements Datatype {

    public final static AnyDatatype INSTANCE = new AnyDatatype();

    private AnyDatatype() {
        super();
    }

    public MessageList validate() {
        return new MessageList();
    }

    public String getName() {
        return "any"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "any"; //$NON-NLS-1$
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isValueDatatype() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    public String getJavaClassName() {
        return null;
    }

    public int compareTo(Datatype o) {
        return 0;
    }

    public boolean hasNullObject() {
        return false;
    }

}
