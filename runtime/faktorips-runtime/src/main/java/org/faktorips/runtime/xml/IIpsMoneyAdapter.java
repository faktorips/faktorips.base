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

import org.faktorips.values.Money;

/**
 * {@link IIpsXmlAdapter} for {@link Money}.
 */
public interface IIpsMoneyAdapter extends IIpsXmlAdapter<String, Money> {

    @Override
    default Money unmarshal(String v) {
        return Money.valueOf(v);
    }

    @Override
    default String marshal(Money v) {
        if (v == null || v.isNull()) {
            return "";
        }

        return v.toString();
    }
}
