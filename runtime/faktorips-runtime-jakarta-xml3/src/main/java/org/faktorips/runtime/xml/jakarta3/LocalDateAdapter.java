/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml.jakarta3;

import java.time.LocalDate;

import org.faktorips.runtime.xml.IIpsLocalDateAdapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} for {@link LocalDate}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jakarta.xml.LocalDateAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> implements IIpsLocalDateAdapter {

    @Override
    public LocalDate unmarshal(String v) {
        return IIpsLocalDateAdapter.super.unmarshal(v);
    }

    @Override
    public String marshal(LocalDate v) {
        return IIpsLocalDateAdapter.super.marshal(v);
    }
}
