/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.util;

import java.io.Serializable;
import java.util.Comparator;

import org.faktorips.datatype.Datatype;

/**
 * A {@link Comparator} of {@link Datatype Datatypes}, comparing them by their
 * {@link Datatype#getQualifiedName() qualified name}.
 */
public class DatatypeComparator implements Comparator<Datatype>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Comparator<String> QUALIFIED_NAME_COMPARATOR = Comparator.nullsFirst(Comparator
            .naturalOrder());
    private static final Comparator<Datatype> COMPARATOR = Comparator.nullsFirst(Comparator
            .comparing(Datatype::getQualifiedName, QUALIFIED_NAME_COMPARATOR));

    public static int doCompare(Datatype o1, Datatype o2) {
        return COMPARATOR.compare(o1, o2);
    }

    @Override
    public int compare(Datatype o1, Datatype o2) {
        return doCompare(o1, o2);
    }

}
