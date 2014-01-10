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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryManager;

/**
 * 
 * The {@link AbstractRuntimeRepositoryManager} manages the referenced
 * {@link IRuntimeRepositoryManager}. It delegates the up to date check and the creation of new
 * repositories to the subclass.
 * 
 * @author dirmeier
 */
public abstract class AbstractRuntimeRepositoryManager implements IRuntimeRepositoryManager {

    private volatile IRuntimeRepository currentRuntimeRepository;
    private List<IRuntimeRepositoryManager> managers = new CopyOnWriteArrayList<IRuntimeRepositoryManager>();
    private volatile List<IRuntimeRepositoryManager> allManagers;

    /**
     * Creates a new {@link AbstractRuntimeRepositoryManager}
     */
    public AbstractRuntimeRepositoryManager() {
        super();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated since 3.9.0: The method name was a false translation from German. The correct
     *             method is called {@link #getCurrentRuntimeRepository()}
     */
    @Deprecated
    public synchronized IRuntimeRepository getActualRuntimeRepository() {
        return getCurrentRuntimeRepository();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized IRuntimeRepository getCurrentRuntimeRepository() {
        if (!isRepositoryUpToDate(currentRuntimeRepository) || !isReferencedRepositorysUpToDate()) {
            currentRuntimeRepository = createNewRuntimeRepository();
            for (IRuntimeRepositoryManager manager : managers) {
                IRuntimeRepository runtimeRepository = manager.getCurrentRuntimeRepository();
                if (runtimeRepository != null) {
                    currentRuntimeRepository.addDirectlyReferencedRepository(runtimeRepository);
                }
            }
        }
        return currentRuntimeRepository;
    }

    /**
     * This method checks whether the {@link IRuntimeRepository} is up to date or not. If this
     * method returns false, a new repository have to be created
     * 
     * @param currentRuntimeRepository The actual runtime repository that have to be checked
     * @return true if the repository is still up to date
     */
    protected abstract boolean isRepositoryUpToDate(IRuntimeRepository currentRuntimeRepository);

    /**
     * Creates a new repository. This method have to create thre repository but do NOT have to
     * connect the repository to the other referenced repositories.
     * 
     * @return A newly created {@link IRuntimeRepository}
     */
    protected abstract IRuntimeRepository createNewRuntimeRepository();

    /**
     * Returns true if every manager directly referenced to this one did not change it repository.
     * 
     * @return true if any direct referenced repository changed
     */
    private boolean isReferencedRepositorysUpToDate() {
        List<IRuntimeRepository> directReferencedRepos = new ArrayList<IRuntimeRepository>(
                currentRuntimeRepository.getDirectlyReferencedRepositories());
        for (IRuntimeRepositoryManager manager : managers) {
            IRuntimeRepository referencedRepository = manager.getCurrentRuntimeRepository();
            if (referencedRepository != null) {
                // the repository of every manager have to be in the list of direct referenced
                // repositories.
                if (!directReferencedRepos.remove(referencedRepository)) {
                    // If any manager creates a new repository return false
                    return false;
                }
            }
        }
        // after iterating over all managers the list have to be empty. Otherwise a references
        // manager was deleted
        if (!directReferencedRepos.isEmpty()) {
            return false;
        }
        return true;
    }

    public final void addDirectlyReferencedManager(IRuntimeRepositoryManager manager) {
        allManagers = null;
        managers.add(manager);
    }

    public List<IRuntimeRepositoryManager> getDirectlyReferencedRepositoryManagers() {
        return Collections.unmodifiableList(managers);
    }

    public List<IRuntimeRepositoryManager> getAllReferencedRepositoryManagers() {
        List<IRuntimeRepositoryManager> result = allManagers;
        if (result == null) {
            synchronized (this) {
                result = allManagers;
                if (result == null) {
                    result = new ArrayList<IRuntimeRepositoryManager>(managers.size());
                    // list is so small, linear search is ok.
                    LinkedList<IRuntimeRepositoryManager> candidates = new LinkedList<IRuntimeRepositoryManager>();
                    candidates.add(this);
                    while (!candidates.isEmpty()) {
                        IRuntimeRepositoryManager candidate = candidates.get(0);
                        candidates.remove(0);
                        if (candidate != this && !result.contains(candidate)) {
                            result.add(candidate);
                        }
                        for (IRuntimeRepositoryManager newCandidate : candidate
                                .getDirectlyReferencedRepositoryManagers()) {
                            candidates.add(newCandidate);
                        }
                    }
                    allManagers = Collections.unmodifiableList(result);
                }
            }
        }
        return allManagers;
    }

}
