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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link XmlAdapter} for {@link LocalDateTime}. The adapter can be used for individual
 * elements/attributes or registered in {@code package-info.java}:
 *
 * <pre>
 * <code>
 &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
     &#64;javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = de.faktorzehn.commons.jaxb.LocalDateTimeAdapter.class),
     ...
 })
 package com.acme.foo;
 * </code>
 * </pre>
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public LocalDateTime unmarshal(String v) {
        if (IpsStringUtils.isBlank(v)) {
            return null;
        }

        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            // support old Faktor-IOS format with milliseconds
            LocalDateTime localDateTimeWithMilliseconds = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss:SSS")
                    .parse(v, LocalDateTime::from);
            return localDateTimeWithMilliseconds.withNano(0);
        }
    }

    @Override
    public String marshal(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
