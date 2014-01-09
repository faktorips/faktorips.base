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

package org.faktorips.devtools.core.fl;

public enum IdentifierKind {

    DEFAULT_IDENTIFIER,
    ATTRIBUTE;

    public boolean isDefaultIdentifier() {
        return this.equals(DEFAULT_IDENTIFIER);
    }

    public static IdentifierKind getDefaultIdentifierOrAttribute(boolean isDefaultIdentifier) {
        if (isDefaultIdentifier) {
            return DEFAULT_IDENTIFIER;
        }
        return ATTRIBUTE;
    }

}
