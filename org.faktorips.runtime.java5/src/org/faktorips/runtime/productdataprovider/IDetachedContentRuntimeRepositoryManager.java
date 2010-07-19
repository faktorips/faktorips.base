/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.productdataprovider;

import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;

public interface IDetachedContentRuntimeRepositoryManager {

    /**
     * Call a modification check on the product data provider. If there are any changes in the
     * product data, this method creates a new {@link IRuntimeRepository}. If there are no changes
     * this method simply returns the existing one.
     */
    public IRuntimeRepository getActualRuntimeRepository();

    /**
     * Use this method to add a referenced {@link IDetachedContentRuntimeRepositoryManager}. The
     * {@link IRuntimeRepository} returned by {@link #getActualRuntimeRepository()} asks all
     * references managers for their repositories and adding the references.
     * 
     * @param manager The manager to connect with this manager
     */
    public void addDirectlyReferencedManager(IDetachedContentRuntimeRepositoryManager manager);

    /**
     * Get the list of direct references managers.
     * 
     * @return All directly referenced managers
     * 
     */
    public List<IDetachedContentRuntimeRepositoryManager> getDirectlyReferencedRepositoryManagers();

    /**
     * Collect all referenced manager. This request all referenced managers from the direct
     * references managers recursively.
     * 
     * @return A list of all {@link IDetachedContentRuntimeRepositoryManager} that are referenced
     *         directly or indirectly
     */
    public List<IDetachedContentRuntimeRepositoryManager> getAllReferencedRepositoryManagers();

}