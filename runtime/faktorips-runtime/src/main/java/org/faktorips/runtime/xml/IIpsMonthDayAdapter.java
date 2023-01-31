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

import java.time.MonthDay;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link IIpsXmlAdapter} for {@link MonthDay}.
 */
public interface IIpsMonthDayAdapter extends IIpsXmlAdapter<String, MonthDay> {

    @Override
    default MonthDay unmarshal(String v) {
        if (IpsStringUtils.isBlank(v)) {
            return null;
        }
        return MonthDay.parse(v);
    }

    @Override
    default String marshal(MonthDay v) {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
