/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpttype;

import java.util.Arrays;

import org.faktorips.util.ArgumentCheck;

/**
 * The aggregation kind as specified in the UML super structure document.
 * 
 * @author Jan Ortmann
 */
public enum AggregationKind {

    NONE("none", "None"), //$NON-NLS-1$ //$NON-NLS-2$
    SHARED("shared", "Shared"), //$NON-NLS-1$ //$NON-NLS-2$
    COMPOSITE("composite", "Composite"); //$NON-NLS-1$ //$NON-NLS-2$

    private final String id;
    private final String name;

    AggregationKind(String id, String name) {
        ArgumentCheck.notNull(id);
        ArgumentCheck.notNull(name);
        this.id = id;
        this.name = name;
    }

    public static final AggregationKind getKind(String id) {
        return Arrays.stream(AggregationKind.values()).filter(s -> s.id.equals(id)).findAny().orElse(null);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
