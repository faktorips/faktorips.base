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

package org.faktorips.runtime.internal;

/**
 * @author ortmann
 */
public class TestEnumValue {

    private final String id;

    public TestEnumValue(String id) {
        this.id = id;
    }

    public String getEnumValueId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

}
