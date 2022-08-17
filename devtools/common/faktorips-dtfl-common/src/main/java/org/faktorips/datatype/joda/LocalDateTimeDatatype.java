/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.joda;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.DateUtil;

/**
 * {@link Datatype} for {@code org.joda.time.LocalDateTime}.
 */
public class LocalDateTimeDatatype extends ValueClassNameDatatype {

    public static final LocalDateTimeDatatype DATATYPE = new LocalDateTimeDatatype();

    private static final String NAME = "LocalDateTime"; //$NON-NLS-1$

    public LocalDateTimeDatatype() {
        super(NAME);
    }

    @Override
    public Object getValue(String value) {
        return value;
    }

    @Override
    public boolean supportsCompare() {
        return true;
    }

    @Override
    public boolean isParsable(String value) {
        return IpsStringUtils.isEmpty(value) || DateUtil.isIsoDateTime(value);
    }

}
