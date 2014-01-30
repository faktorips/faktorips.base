/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for Strings.
 * 
 * @author Jan Ortmann
 */
public class StringDatatype extends ValueClassDatatype {

    public StringDatatype() {
        super(String.class);
    }

    public StringDatatype(String name) {
        super(String.class, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(String value) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String value) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
