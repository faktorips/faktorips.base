/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link IIpsXmlAdapter} for {@link LocalDateTime}.
 */
public interface IIpsLocalDateTimeAdapter extends IIpsXmlAdapter<String, LocalDateTime> {

    @Override
    default LocalDateTime unmarshal(String v) {
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
    default String marshal(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
