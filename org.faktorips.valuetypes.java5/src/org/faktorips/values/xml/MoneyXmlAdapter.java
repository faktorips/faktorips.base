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

package org.faktorips.values.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.values.Money;

/**
 * Maps a Faktor-IPS {@link Money}.
 */
public class MoneyXmlAdapter extends XmlAdapter<String, Money> {

    @Override
    public String marshal(Money v) throws Exception {
        if (v == null || v.isNull()) {
            return "";
        }

        return v.toString();
    }

    @Override
    public Money unmarshal(String v) throws Exception {
        return Money.valueOf(v);
    }

}
