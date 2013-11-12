package org.faktorips.datatype.joda;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.values.DateUtil;

/**
 * {@link Datatype} for {@code org.joda.time.LocalDateTime}.
 */
public class LocalDateTimeDatatype extends ValueClassNameDatatype {

    public static final String ORG_JODA_TIME_LOCAL_DATE_TIME = "org.joda.time.LocalDateTime"; //$NON-NLS-1$
    public static final ValueDatatype DATATYPE = new LocalDateTimeDatatype();

    public LocalDateTimeDatatype() {
        super(ORG_JODA_TIME_LOCAL_DATE_TIME);
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
        return StringUtils.isEmpty(value) || DateUtil.isIsoDateTime(value);
    }

}
