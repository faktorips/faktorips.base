/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.javax;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.xml.IIpsDecimalAdapter;
import org.faktorips.values.Decimal;

/**
 * {@link XmlAdapter} for {@link Decimal}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jaxb.DecimalAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class DecimalAdapter extends XmlAdapter<String, Decimal> implements IIpsDecimalAdapter {

    @Override
    public Decimal unmarshal(String v) {
        return IIpsDecimalAdapter.super.unmarshal(v);
    }

    @Override
    public String marshal(Decimal v) {
        return IIpsDecimalAdapter.super.marshal(v);
    }

}
