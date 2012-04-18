package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.joda.LocalDateDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateDatatype}.
 */
public class LocalDateHelper extends BaseJodaDatatypeHelper {

    public LocalDateHelper() {
        super("toLocalDate"); //$NON-NLS-1$
    }

}
