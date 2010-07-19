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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryManager;


public abstract class AbstractRuntimeRepositoryManager implements IRuntimeRepositoryManager {

    private volatile IRuntimeRepository actualRuntimeRepository;
    private List<IRuntimeRepositoryManager> managers = new CopyOnWriteArrayList<IRuntimeRepositoryManager>();
    private volatile List<IRuntimeRepositoryManager> allManagers;

    public AbstractRuntimeRepositoryManager() {
        super();
    }

    public synchronized IRuntimeRepository getActualRuntimeRepository() {
        if (!isRepositoryUpToDate(actualRuntimeRepository) || !isReferencedRepositorysUpToDate()) {
            actualRuntimeRepository = createNewRuntimeRepository();
            for (IRuntimeRepositoryManager manager : managers) {
                actualRuntimeRepository.addDirectlyReferencedRepository(manager.getActualRuntimeRepository());
            }
        }
        return actualRuntimeRepository;
    }

    protected abstract boolean isRepositoryUpToDate(IRuntimeRepository actualRuntimeRepository);

    protected abstract IRuntimeRepository createNewRuntimeRepository();

    /**
     * Returns true if every manager directly referenced to this one did not change it repository.
     * 
     * @return true if any direct referenced repository changed
     */
    private boolean isReferencedRepositorysUpToDate() {
        List<IRuntimeRepository> directReferencedRepos = new ArrayList<IRuntimeRepository>(actualRuntimeRepository
                .getDirectlyReferencedRepositories());
        for (IRuntimeRepositoryManager manager : managers) {
            IRuntimeRepository referencedRepository = manager.getActualRuntimeRepository();
            // the repository of every manager have to be in the list of direct referenced
            // repositories.
            if (!directReferencedRepos.remove(referencedRepository)) {
                // If any manager creates a new repository return false
                return false;
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

    /**
     * {@inheritDoc}
     */
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