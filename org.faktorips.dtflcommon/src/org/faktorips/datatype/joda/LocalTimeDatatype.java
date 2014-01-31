/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.values.DateUtil;

/**
 * {@link Datatype} for {@code org.joda.time.LocalTime}.
 */
public class LocalTimeDatatype extends ValueClassNameDatatype {

    public static final String ORG_JODA_TIME_LOCAL_TIME = "org.joda.time.LocalTime"; //$NON-NLS-1$
    public static final ValueDatatype DATATYPE = new LocalTimeDatatype();

    public LocalTimeDatatype() {
        super(ORG_JODA_TIME_LOCAL_TIME);
    }

    @Override
    public Object getValue(String value) {
        return value;
    }

    public boolean supportsCompare() {
        return true;
    }

    @Override
    public boolean isParsable(String value) {
        return StringUtils.isEmpty(value) || DateUtil.isIsoTime(value);
    }

}
