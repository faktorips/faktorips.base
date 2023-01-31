/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.jaxb;

import java.time.MonthDay;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.xml.IIpsMonthDayAdapter;

/**
 * {@link XmlAdapter} for {@link MonthDay}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jaxb.MonthAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 * 
 * @deprecated for removal since 23.6; use {@code org.faktorips.runtime.xml.javax.MonthDayAdapter}
 *                 or {@code org.faktorips.runtime.xml.jakarta.MonthDayAdapter} instead
 */
@Deprecated
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
