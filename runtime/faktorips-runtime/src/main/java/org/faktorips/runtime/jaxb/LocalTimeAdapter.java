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

import java.time.LocalTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.xml.IIpsLocalTimeAdapter;

/**
 * {@link XmlAdapter} for {@link LocalTime}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 *
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = de.faktorzehn.commons.jaxb.LocalTimeAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 * 
 * @deprecated for removal since 23.6; use {@code org.faktorips.runtime.xml.javax.LocalTimeAdapter}
 *                 or {@code org.faktorips.runtime.xml.jakarta.LocalTimeAdapter} instead
 */
@Deprecated
public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> implements IIpsLocalTimeAdapter {

    @Override
    public LocalTime unmarshal(String s) {
        return IIpsLocalTimeAdapter.super.unmarshal(s);
    }

    @Override
    public String marshal(LocalTime t) {
        return IIpsLocalTimeAdapter.super.marshal(t);
    }
}
