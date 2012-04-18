package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateTimeDatatype}.
 */
public class LocalDateTimeHelper extends BaseJodaDatatypeHelper {

    public LocalDateTimeHelper() {
        super("toLocalDateTime"); //$NON-NLS-1$
    }

}
