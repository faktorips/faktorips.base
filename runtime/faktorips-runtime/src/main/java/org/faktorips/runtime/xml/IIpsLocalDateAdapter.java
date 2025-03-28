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

import java.time.LocalDate;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link IIpsXmlAdapter} for {@link LocalDate}.
 */
public interface IIpsLocalDateAdapter extends IIpsXmlAdapter<String, LocalDate> {

    @Override
    default LocalDate unmarshal(String v) {
        if (IpsStringUtils.isBlank(v)) {
            return null;
        }
        return LocalDate.parse(v);
    }

    @Override
    default String marshal(LocalDate v) {
        return v == null ? null : v.toString();
    }
}
