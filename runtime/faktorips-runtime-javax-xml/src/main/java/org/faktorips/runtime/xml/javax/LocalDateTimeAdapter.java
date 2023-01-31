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

import java.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.xml.IIpsLocalDateTimeAdapter;

/**
 * {@link XmlAdapter} for {@link LocalDateTime}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 *
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = org.faktorips.runtime.jaxb.LocalDateTimeAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> implements IIpsLocalDateTimeAdapter {

    @Override
    public LocalDateTime unmarshal(String v) {
        return IIpsLocalDateTimeAdapter.super.unmarshal(v);
    }

    @Override
    public String marshal(LocalDateTime v) {
        return IIpsLocalDateTimeAdapter.super.marshal(v);
    }
}
