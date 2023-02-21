/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.jakarta;

import org.faktorips.runtime.xml.IIpsMoneyAdapter;
import org.faktorips.values.Money;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} for {@link Money}. The adapter can be used for individual elements/attributes
 * or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jakarta.xml.MoneyAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class MoneyAdapter extends XmlAdapter<String, Money> implements IIpsMoneyAdapter {

    @Override
    public String marshal(Money v) {
        return IIpsMoneyAdapter.super.marshal(v);
    }

    @Override
    public Money unmarshal(String v) {
        return IIpsMoneyAdapter.super.unmarshal(v);
    }

}
