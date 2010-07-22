/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
     */
    public IRuntimeRepository getActualRuntimeRepository();

    /**
     * Use this method to add a referenced {@link IRuntimeRepositoryManager}. The
     * {@link IRuntimeRepository} returned by {@link #getActualRuntimeRepository()} asks all
     * references managers for their repositories and adding the references.
     * 
     * @param manager The manager to connect with this manager
     */
    public void addDirectlyReferencedManager(IRuntimeRepositoryManager manager);

    /**
     * Get the list of direct references managers.
     * 
     * @return All directly referenced managers
     * 
     */
    public List<IRuntimeRepositoryManager> getDirectlyReferencedRepositoryManagers();

    /**
     * Collect all referenced manager. This request all referenced managers from the direct
     * references managers recursively.
     * 
     * @return A list of all {@link IRuntimeRepositoryManager} that are referenced directly or
     *         indirectly
     */
    public List<IRuntimeRepositoryManager> getAllReferencedRepositoryManagers();

}