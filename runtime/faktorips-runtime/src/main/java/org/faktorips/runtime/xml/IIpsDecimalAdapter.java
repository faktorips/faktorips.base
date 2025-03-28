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

import org.apache.commons.lang3.StringUtils;
import org.faktorips.values.Decimal;

/**
 * {@link IIpsXmlAdapter} for {@link Decimal}.
 */
public interface IIpsDecimalAdapter extends IIpsXmlAdapter<String, Decimal> {

    @Override
    default Decimal unmarshal(String v) {
        return Decimal.valueOf(v);
    }

    @Override
    default String marshal(Decimal v) {
        return v == null || v.isNull() ? StringUtils.EMPTY : v.toString();
    }
}
