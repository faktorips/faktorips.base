/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Enum indicating the kind of severity a message can have
 * 
 * @author IMaazi
 */

public enum Severity {

    /**
     * Severity none.
     */
    NONE,

    /**
     * Severity info.
     */
    INFO,

    /**
     * Severity warning.
     */
    WARNING,

    /**
     * Severity error.
     */
    ERROR;
}