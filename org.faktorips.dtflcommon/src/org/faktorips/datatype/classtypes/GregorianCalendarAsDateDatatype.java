/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

/**
 * A datatype using the GregorianCalendar class to represent dates without any information about the
 * time.
 * 
 * @author Jan Ortmann
 */
public class GregorianCalendarAsDateDatatype extends GregorianCalendarDatatype {

    /**
     * Constructs a new instance with the name "GregorianCalendar".
     */
    public GregorianCalendarAsDateDatatype() {
        this("GregorianCalendar"); //$NON-NLS-1$
    }

    /**
     * Constructs a new instance with the given name.
     */
    public GregorianCalendarAsDateDatatype(String name) {
        super(name, false);
    }

}
