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

import java.time.MonthDay;

import org.faktorips.runtime.xml.IIpsMonthDayAdapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} for {@link MonthDay}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jakarta.xml.MonthDayAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class MonthDayAdapter extends XmlAdapter<String, MonthDay> implements IIpsMonthDayAdapter {
    @Override
    public MonthDay unmarshal(String v) {
        return IIpsMonthDayAdapter.super.unmarshal(v);
    }

    @Override
    public String marshal(MonthDay v) {
        return IIpsMonthDayAdapter.super.marshal(v);
    }
}
