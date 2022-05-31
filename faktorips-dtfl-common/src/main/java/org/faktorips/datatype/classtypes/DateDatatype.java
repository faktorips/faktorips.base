/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.DateUtil;

/**
 * A Datatype for the <code>java.util.Date </code> class. The string representation supported by
 * this datatype is <em>yyyy-MM-dd</em>.
 * 
 * @author Peter Erzberger
 */
public class DateDatatype extends ValueClassNameDatatype {

    private static final String DATE_FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    private SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    /**
     * Creates a new DateDatatype where the name is the short class name.
     */
    public DateDatatype() {
        super(Date.class.getSimpleName());
    }

    @Override
    public Object getValue(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return null;
        }
        try {
            if (!DateUtil.isIsoDate(value)) {
                throw new IllegalArgumentException("Date value must have the format " + DATE_FORMAT); //$NON-NLS-1$
            }
            return formatter.parse(value);
            // CSOFF: Illegal Catch
        } catch (Exception e) {
            // CSON: Illegal Catch
            IllegalArgumentException ill = new IllegalArgumentException(
                    "Unable to convert the provided string parameter: \"" + value + "\"  into a " + Date.class //$NON-NLS-1$ //$NON-NLS-2$
                            + " instance"); //$NON-NLS-1$
            ill.initCause(e);
            throw ill;
        }
    }

    @Override
    public boolean isParsable(String value) {
        return IpsStringUtils.isEmpty(value) || DateUtil.isIsoDate(value);
    }

    @Override
    public String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        return formatter.format(value);
    }

    /**
     * Calls the <code>getValue(String)</code> method and casts the result to a Date.
     */
    public Date getDateValue(String value) {
        return (Date)getValue(value);
    }

    @Override
    public boolean supportsCompare() {
        return true;
    }

}
