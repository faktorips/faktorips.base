/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
 * The {@link AbstractRuntimeRepositoryManager} manages the referenced
 * {@link IRuntimeRepositoryManager}. It delegates the up-to-date check and the creation of new
 * repositories to the subclass.
 */
public abstract class AbstractRuntimeRepositoryManager implements IRuntimeRepositoryManager {

    private volatile IRuntimeRepository currentRuntimeRepository;
    private List<IRuntimeRepositoryManager> managers = new CopyOnWriteArrayList<>();
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
    @Override
    @Deprecated
    public synchronized IRuntimeRepository getActualRuntimeRepository() {
        return getCurrentRuntimeRepository();
    }

    @Override
    public IRuntimeRepository getCurrentRuntimeRepository() {
        IRuntimeRepository runtimeRepository = currentRuntimeRepository;
        if (isRepositoryUpToDate(runtimeRepository) && areReferencedRepositoriesUpToDate()) {
            return runtimeRepository;
        } else {
            synchronized (this) {
                runtimeRepository = currentRuntimeRepository;
                if (!(isRepositoryUpToDate(runtimeRepository) && areReferencedRepositoriesUpToDate())) {
                    runtimeRepository = createNewRuntimeRepository();
                    for (IRuntimeRepositoryManager manager : managers) {
                        IRuntimeRepository referencedRuntimeRepository = manager.getCurrentRuntimeRepository();
                        if (referencedRuntimeRepository != null) {
                            runtimeRepository.addDirectlyReferencedRepository(referencedRuntimeRepository);
                        }
                    }
                    currentRuntimeRepository = runtimeRepository;
                }
                return currentRuntimeRepository;
            }
        }
    }

    /**
     * Checks whether the {@link IRuntimeRepository} is up to date or not. If this method returns
     * {@code false}, a new repository has to be created.
     * 
     * @param currentRuntimeRepository the runtime repository that has to be checked
     * @return whether the repository is still up to date
     */
    protected abstract boolean isRepositoryUpToDate(IRuntimeRepository currentRuntimeRepository);

    /**
     * Creates a new repository. This method has to create the repository but must NOT connect the
     * repository to the other referenced repositories.
     * 
     * @return A newly created {@link IRuntimeRepository}
     */
    protected abstract IRuntimeRepository createNewRuntimeRepository();

    /**
     * Returns {@code true} if no manager directly referenced from this one did change its
     * repository.
     * 
     * @return whether no directly referenced repository changed
     */
    private boolean areReferencedRepositoriesUpToDate() {
        List<IRuntimeRepository> directReferencedRepos = new ArrayList<>(
                currentRuntimeRepository.getDirectlyReferencedRepositories());
        for (IRuntimeRepositoryManager manager : managers) {
            IRuntimeRepository referencedRepository = manager.getCurrentRuntimeRepository();
            if (referencedRepository != null) {
                // the repository of every manager has to be in the list of direct referenced
                // repositories.
                if (!directReferencedRepos.remove(referencedRepository)) {
                    // If any manager creates a new repository return false
                    return false;
                }
            }
        }
        // after iterating over all managers the list has to be empty. Otherwise a referenced
        // manager was
        // deleted
        if (!directReferencedRepos.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public final void addDirectlyReferencedManager(IRuntimeRepositoryManager manager) {
        allManagers = null;
        managers.add(manager);
    }

    @Override
    public List<IRuntimeRepositoryManager> getDirectlyReferencedRepositoryManagers() {
        return Collections.unmodifiableList(managers);
    }

    @Override
    public List<IRuntimeRepositoryManager> getAllReferencedRepositoryManagers() {
        List<IRuntimeRepositoryManager> result = allManagers;
        if (result != null) {
            return result;
        } else {
            synchronized (this) {
                result = allManagers;
                if (result == null) {
                    result = new ArrayList<>(managers.size());
                    // list is so small, linear search is ok.
                    LinkedList<IRuntimeRepositoryManager> candidates = new LinkedList<>();
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
                    result = Collections.unmodifiableList(result);
                    allManagers = result;
                }
                return allManagers;
            }
        }
    }

}
