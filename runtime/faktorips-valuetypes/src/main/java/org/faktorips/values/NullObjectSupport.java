/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

/**
 * This interface marks a class as supporting the null object pattern. Instances of this class are
 * either a "normal" object or the null object.
 * 
 * @see org.faktorips.values.NullObject
 * 
 * @author Jan Ortmann
 */
public interface NullObjectSupport {

    /**
     * Returns <code>true</code> if this is the object representing <code>null</code>, otherwise
     * <code>false</code>.
     */
    boolean isNull();

    /**
     * Returns <code>false</code> if this is the object representing <code>null</code>, otherwise
     * <code>true</code>.
     */
    boolean isNotNull();

}
