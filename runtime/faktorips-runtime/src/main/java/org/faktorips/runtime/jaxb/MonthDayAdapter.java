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

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link XmlAdapter} for {@link MonthDay}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = de.faktorzehn.commons.jaxb.MonthAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class MonthDayAdapter extends XmlAdapter<String, MonthDay> {

    @Override
    public MonthDay unmarshal(String v) {
        if (IpsStringUtils.isBlank(v)) {
            return null;
        }
        return MonthDay.parse(v);
    }

    @Override
    public String marshal(MonthDay v) {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
