/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml;

import java.time.Year;

/**
 * {@link IIpsXmlAdapter} for {@link Year}.
 */
public interface IIpsYearAdapter extends IIpsXmlAdapter<Integer, Year> {

    @Override
    default Year unmarshal(Integer i) {
        if (i == null) {
            return null;
        }
        return Year.of(i);
    }

    @Override
    default Integer marshal(Year m) {
        return m == null ? null : m.getValue();
    }
}
