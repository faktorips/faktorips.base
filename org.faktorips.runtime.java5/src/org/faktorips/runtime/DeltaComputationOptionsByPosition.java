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

package org.faktorips.runtime;

/**
 * Delta computation options that create child deltas per position and don't ignore any property.
 * 
 * TODO the following reference does not exist
 * 
 * 
 * @see IDeltaComputationOptions.ComputationMethod#BY_POSITION
 * 
 * @author Jan Ortmann
 */
public class DeltaComputationOptionsByPosition implements IDeltaComputationOptions {

    public ComputationMethod getMethod(String association) {
        return ComputationMethod.BY_POSITION;
    }

    /**
     * Returns <code>true</code> if the specified object references are identical.
     */
    public boolean isSame(IModelObject object1, IModelObject object2) {
        return object1 == object2;
    }

    /**
     * Returns <code>false</code>.
     */
    public boolean ignore(Class<?> clazz, String property) {
        return false;
    }

}
