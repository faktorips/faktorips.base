/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
