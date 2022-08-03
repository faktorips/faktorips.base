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

import java.io.Serializable;

import org.faktorips.runtime.internal.ProductConfiguration;

/**
 * A {@link IRuntimeRepositoryLookup} is an interface for a class that is able to provide a runtime
 * repository. It is used to serialize policy components especially the {@link ProductConfiguration}
 * .
 * <p>
 * An implementation needs to be serializable hence this interface already implements the
 * Serializable interface. After serializing and deserializing an object of this type the method
 * {@link #getRuntimeRepository()} needs to return an {@link IRuntimeRepository} with the same
 * content as before.
 * 
 */
public interface IRuntimeRepositoryLookup extends Serializable {

    /**
     * Returns an instance of {@link IRuntimeRepository} that is used to load the product component
     * and generation of a serialized {@link ProductConfiguration} after deserialization.
     * 
     * @return A {@link IRuntimeRepository} used to load product components and product component
     *             generations.
     */
    IRuntimeRepository getRuntimeRepository();

}
