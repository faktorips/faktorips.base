package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.joda.LocalTimeDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalTimeDatatype}.
 */
public class LocalTimeHelper extends BaseJodaDatatypeHelper {

    public LocalTimeHelper() {
        super("toLocalTime"); //$NON-NLS-1$
    }

}
