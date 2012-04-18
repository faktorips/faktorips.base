package org.faktorips.datatype.joda;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.datatype.ValueDatatype;

/**
 * {@link Datatype} for {@code org.joda.time.LocalDate}.
 */
public class LocalDateDatatype extends ValueClassNameDatatype {

    public static final String ORG_JODA_TIME_LOCAL_DATE = "org.joda.time.LocalDate"; //$NON-NLS-1$
    public static final ValueDatatype DATATYPE = new LocalDateDatatype();

    public LocalDateDatatype() {
        super(ORG_JODA_TIME_LOCAL_DATE);
    }

    @Override
    public Object getValue(String value) {
        return value;
    }

    public boolean supportsCompare() {
        return true;
    }

}
