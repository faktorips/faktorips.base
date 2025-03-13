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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link IIpsXmlAdapter} for {@link LocalTime}.
 */
public interface IIpsLocalTimeAdapter extends IIpsXmlAdapter<String, LocalTime> {

    @Override
    default LocalTime unmarshal(String v) {
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
    default String marshal(LocalTime v) {
        return v == null ? null : v.toString();
    }
}
