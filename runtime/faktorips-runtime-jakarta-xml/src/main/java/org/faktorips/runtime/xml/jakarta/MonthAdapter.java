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

import java.time.Month;

import org.faktorips.runtime.xml.IIpsMonthAdapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} for {@link Month}. The adapter can be used for individual elements/attributes
 * or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jakarta.xml.MonthAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class MonthAdapter extends XmlAdapter<Integer, Month> implements IIpsMonthAdapter {

    @Override
    public Month unmarshal(Integer i) {
        return IIpsMonthAdapter.super.unmarshal(i);
    }

    @Override
    public Integer marshal(Month m) {
        return IIpsMonthAdapter.super.marshal(m);
    }
}
