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

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.xml.IIpsLocalDateAdapter;

/**
 * {@link XmlAdapter} for {@link LocalDate}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 * 
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jaxb.LocalDateAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 * 
 * @deprecated for removal since 23.6; use {@code org.faktorips.runtime.xml.javax.LocalDateAdapter}
 *                 or {@code org.faktorips.runtime.xml.jakarta.LocalDateAdapter} instead
 */
@Deprecated
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
