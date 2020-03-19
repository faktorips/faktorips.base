/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.util;

import org.faktorips.util.message.Message;

/**
 * Enum representation of {@link Message#getSeverity()}.
 */
public enum DesignTimeSeverity {

    /**
     * Severity none ({@link Message#NONE}).
     */
    NONE(0),

    /**
     * Severity info ({@link Message#INFO}).
     */
    INFO(10),

    /**
     * Severity warning ({@link Message#WARNING}).
     */
    WARNING(20),

    /**
     * Severity error ({@link Message#ERROR}).
     */
    ERROR(30);

    private final int intRepresentation;

    DesignTimeSeverity(int intRepresentation) {
        this.intRepresentation = intRepresentation;
    }

    /**
     * @return the int constant used in {@link Message#getSeverity()}.
     */
    public int getIntRepresentation() {
        return intRepresentation;
    }
}
