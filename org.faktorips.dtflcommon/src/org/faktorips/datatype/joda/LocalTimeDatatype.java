/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.datatype.joda;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.values.DateUtil;

/**
 * {@link Datatype} for {@code org.joda.time.LocalTime}.
 */
public class LocalTimeDatatype extends ValueClassNameDatatype {

    public static final LocalTimeDatatype DATATYPE = new LocalTimeDatatype();

    private static final String NAME = "LocalTime"; //$NON-NLS-1$

    public LocalTimeDatatype() {
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
        return StringUtils.isEmpty(value) || DateUtil.isIsoTime(value);
    }

}
