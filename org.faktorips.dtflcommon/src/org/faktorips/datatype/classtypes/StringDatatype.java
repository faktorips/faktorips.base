/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassNameDatatype;

/**
 * Datatype for Strings.
 * 
 * @author Jan Ortmann
 */
public class StringDatatype extends ValueClassNameDatatype {

    public StringDatatype() {
        super(String.class.getSimpleName());
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
    @Override
    public boolean supportsCompare() {
        return true;
    }
}
