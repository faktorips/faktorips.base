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

import java.time.Month;

/**
 * {@link IIpsXmlAdapter} for {@link Month}.
 */
public interface IIpsMonthAdapter extends IIpsXmlAdapter<Integer, Month> {

    @Override
    default Month unmarshal(Integer i) {
        if (i == null) {
            return null;
        }
        return Month.of(i);
    }

    @Override
    default Integer marshal(Month m) {
        if (m == null) {
            return null;
        }
        return m.getValue();
    }
}
