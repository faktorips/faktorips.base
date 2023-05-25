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

import java.time.Year;

import org.faktorips.runtime.xml.IIpsYearAdapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} for {@link Year}. The adapter can be used for individual elements/attributes
 * or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jakarta.xml.YearAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class YearAdapter extends XmlAdapter<Integer, Year> implements IIpsYearAdapter {

    @Override
    public Year unmarshal(Integer i) {
        return IIpsYearAdapter.super.unmarshal(i);
    }

    @Override
    public Integer marshal(Year m) {
        return IIpsYearAdapter.super.marshal(m);
    }
}
