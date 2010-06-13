/***************************************************************************************************
 * Copyright (c) 2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

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
