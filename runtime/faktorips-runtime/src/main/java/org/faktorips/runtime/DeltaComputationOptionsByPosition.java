/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Delta computation options that create child deltas per position and don't ignore any property.
 *
 * @see IDeltaComputationOptions.ComputationMethod#BY_POSITION
 *
 * @author Jan Ortmann
 */
public class DeltaComputationOptionsByPosition implements IDeltaComputationOptions {

    @Override
    public ComputationMethod getMethod(String association) {
        return ComputationMethod.BY_POSITION;
    }

    /**
     * Returns <code>true</code> if the specified object references are identical.
     */
    @Override
    public boolean isSame(IModelObject object1, IModelObject object2) {
        return object1 == object2;
    }

    /**
     * Returns <code>false</code>.
     */
    @Override
    public boolean ignore(Class<?> clazz, String property) {
        return false;
    }

    @Override
    public boolean isCreateSubtreeDelta() {
        return false;
    }

    @Override
    public boolean ignoreAssociations() {
        return false;
    }
}
