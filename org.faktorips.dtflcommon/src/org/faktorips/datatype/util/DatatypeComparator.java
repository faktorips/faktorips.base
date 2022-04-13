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

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.datatype.Datatype;

public class DatatypeComparator implements Comparator<Datatype>, Serializable {

    private static final long serialVersionUID = 1L;

    public static int doCompare(Datatype o1, Datatype o2) {
        return new DatatypeComparator().compare(o1, o2);
    }

    @Override
    public int compare(Datatype o1, Datatype o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return ObjectUtils.compare(o1.getQualifiedName(), o2.getQualifiedName());
    }

}
