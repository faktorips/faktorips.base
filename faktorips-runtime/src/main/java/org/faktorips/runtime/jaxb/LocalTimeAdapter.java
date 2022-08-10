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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.internal.IpsStringUtils;

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
 */
public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    @Override
    public LocalTime unmarshal(String v) {
        if (IpsStringUtils.isBlank(v)) {
            return null;
        }

        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException e) {
            // support old Faktor-IOS format with milliseconds
            LocalTime localTimeWithMilliseconds = DateTimeFormatter.ofPattern("HH:mm:ss:SSS")
                    .parse(v, LocalTime::from);
            return localTimeWithMilliseconds.withNano(0);
        }
    }

    @Override
    public String marshal(LocalTime v) {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
