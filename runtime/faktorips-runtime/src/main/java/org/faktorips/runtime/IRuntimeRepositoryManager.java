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

import java.util.List;

/**
 * The {@link IRuntimeRepositoryManager} is able to provide a {@link IRuntimeRepository}. The
 * manager is used in scenarios where product data could change over time. In this case, the
 * repository would throw exceptions because product data is out-dated. By calling the method
 * {@link #getActualRuntimeRepository()} the client gets a new {@link IRuntimeRepository} that is
 * able to work with the actual data.
 * <p>
 * If you use a set of referenced repositories you have to connect your
 * {@link IRuntimeRepositoryManager}s instead of the repositories. That enables the manager to set
 * all the necessary references after creating a new repository.
 * 
 * @author dirmeier
 */
public interface IRuntimeRepositoryManager {

    /**
     * Call a modification check on the product data provider. If there are any changes in the
     * product data, this method creates a new {@link IRuntimeRepository}. If there are no changes
     * this method simply returns the existing one.
     * 
     * @deprecated since 3.9.0: The method name was a false translation from German. The correct
     *                 method is called {@link #getCurrentRuntimeRepository()}
     */
    @Deprecated
    IRuntimeRepository getActualRuntimeRepository();

    /**
     * Call a modification check on the product data provider. If there are any changes in the
     * product data, this method creates a new {@link IRuntimeRepository}. If there are no changes
     * this method simply returns the existing one. The current runtime repository can be
     * <strong>null</strong>.
     * <p>
     * Every direct referenced manger will be asked for it current repository. If any direct
     * referenced manager returns <code>null</code> as its current repository, this direct
     * referenced repository will be ignored.
     */
    IRuntimeRepository getCurrentRuntimeRepository();

    /**
     * Use this method to add a referenced {@link IRuntimeRepositoryManager}. The
     * {@link IRuntimeRepository} returned by {@link #getCurrentRuntimeRepository()} asks all
     * references managers for their repositories and adding the references.
     * 
     * @param manager The manager to connect with this manager
     */
    void addDirectlyReferencedManager(IRuntimeRepositoryManager manager);

    /**
     * Get the list of direct references managers.
     * 
     * @return All directly referenced managers
     * 
     */
    List<IRuntimeRepositoryManager> getDirectlyReferencedRepositoryManagers();

    /**
     * Collect all referenced manager. This request all referenced managers from the direct
     * references managers recursively.
     * 
     * @return A list of all {@link IRuntimeRepositoryManager} that are referenced directly or
     *             indirectly
     */
    List<IRuntimeRepositoryManager> getAllReferencedRepositoryManagers();

}
