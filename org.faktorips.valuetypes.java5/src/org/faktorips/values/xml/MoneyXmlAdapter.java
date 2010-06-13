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
