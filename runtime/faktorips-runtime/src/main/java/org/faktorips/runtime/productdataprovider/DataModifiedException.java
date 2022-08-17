/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

/**
 * Exception thrown if the requested data has been modified since last correct modification check.
 * 
 * @author dirmeier
 */
public class DataModifiedException extends Exception {

    private static final long serialVersionUID = 1L;
    // CSOFF: VisibilityModifier
    public final String oldVersion;
    public final String newVersion;
    // CSON: VisibilityModifier

    public DataModifiedException(String message, String oldVersion, String newVersion) {
        super(message);
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " old: " + oldVersion + " new: " + newVersion;
    }

}
