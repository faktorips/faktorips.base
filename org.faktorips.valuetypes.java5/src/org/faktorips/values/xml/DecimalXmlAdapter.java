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

import org.faktorips.values.Decimal;

/**
 * Maps a Faktor-IPS {@link Decimal}.
 */
public class DecimalXmlAdapter extends XmlAdapter<String, Decimal> {

    @Override
    public String marshal(Decimal v) throws Exception {
        if (v == null || v.isNull()) {
            return "";
        }

        return v.toString();
    }

    @Override
    public Decimal unmarshal(String v) throws Exception {
        return Decimal.valueOf(v);
    }

}
