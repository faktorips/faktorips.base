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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;

/**
 * The {@link DetachedContentRuntimeRepositoryManager} manages the access to the
 * {@link DetachedContentRuntimeRepository}. To get a runtime repository that provides the actual
 * product data you have to call {@link #getActualRuntimeRepository()}.
 * <p>
 * To create a new {@link DetachedContentRuntimeRepositoryManager} use the
 * {@link DetachedContentRuntimeRepositoryManager.Builder}
 * 
 * 
 * @see DetachedContentRuntimeRepository
 * 
 * 
 * @author dirmeier
 */

public class DetachedContentRuntimeRepositoryManager implements IDetachedContentRuntimeRepositoryManager {

    private volatile DetachedContentRuntimeRepository actualRuntimeRepository;

    private final DetachedContentRuntimeRepositoryManager.Builder builder;

    private List<IDetachedContentRuntimeRepositoryManager> managers = new CopyOnWriteArrayList<IDetachedContentRuntimeRepositoryManager>();

    private volatile List<IDetachedContentRuntimeRepositoryManager> allManagers;

    /**
     * This is the constructor for the {@link DetachedContentRuntimeRepositoryManager}. The
     * constructor is only called from the internal {@link Builder}.
     * 
     */
    private DetachedContentRuntimeRepositoryManager(Builder builder) {
        this.builder = builder;

    }

    public synchronized IRuntimeRepository getActualRuntimeRepository() {
        if (actualRuntimeRepository == null || !actualRuntimeRepository.isUpToDate()
                || !isReferencedRepositorysUpToDate()) {
            actualRuntimeRepository = new DetachedContentRuntimeRepository(builder);
            for (IDetachedContentRuntimeRepositoryManager manager : managers) {
                actualRuntimeRepository.addDirectlyReferencedRepository(manager.getActualRuntimeRepository());
            }
        }
        return actualRuntimeRepository;
    }

    /**
     * Returns true if every manager directly referenced to this one did not change it repository.
     * 
     * @return true if any direct referenced repository changed
     */
    private boolean isReferencedRepositorysUpToDate() {
        List<IRuntimeRepository> directReferencedRepos = new ArrayList<IRuntimeRepository>(actualRuntimeRepository
                .getDirectlyReferencedRepositories());
        for (IDetachedContentRuntimeRepositoryManager manager : managers) {
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

    public final void addDirectlyReferencedManager(IDetachedContentRuntimeRepositoryManager manager) {
        allManagers = null;
        managers.add(manager);
    }

    public List<IDetachedContentRuntimeRepositoryManager> getDirectlyReferencedRepositoryManagers() {
        return Collections.unmodifiableList(managers);
    }

    /**
     * {@inheritDoc}
     */
    public List<IDetachedContentRuntimeRepositoryManager> getAllReferencedRepositoryManagers() {
        List<IDetachedContentRuntimeRepositoryManager> result = allManagers;
        if (result == null) {
            synchronized (this) {
                result = allManagers;
                if (result == null) {
                    result = new ArrayList<IDetachedContentRuntimeRepositoryManager>(managers.size());
                    // list is so small, linear search is ok.
                    LinkedList<IDetachedContentRuntimeRepositoryManager> candidates = new LinkedList<IDetachedContentRuntimeRepositoryManager>();
                    candidates.add(this);
                    while (!candidates.isEmpty()) {
                        IDetachedContentRuntimeRepositoryManager candidate = candidates.get(0);
                        candidates.remove(0);
                        if (candidate != this && !result.contains(candidate)) {
                            result.add(candidate);
                        }
                        for (IDetachedContentRuntimeRepositoryManager newCandidate : candidate
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

    /**
     * A builder to create the {@link DetachedContentRuntimeRepositoryManager} and
     * {@link DetachedContentRuntimeRepository}
     * 
     * @author dirmeier
     */
    public static class Builder {

        private final IProductDataProviderFactory dataProviderFactory;
        private String repositoryName = "";
        private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        private IFormulaEvaluatorFactory formulaEvaluatorFactory;
        private ICacheFactory cacheFactory = new DefaultCacheFactory();

        /**
         * The only mandatory parameter is the {@link IProductDataProviderFactory}. It is used to
         * create the {@link IProductDataProvider}s.
         * 
         * @param productDataProviderFactory The builder to create {@link IProductDataProvider}s
         */
        public Builder(IProductDataProviderFactory productDataProviderFactory) {
            this.dataProviderFactory = productDataProviderFactory;
        }

        /**
         * @return Returns the dataProviderFactory.
         */
        IProductDataProviderFactory getProductDataProviderFactory() {
            return dataProviderFactory;
        }

        /**
         * The optional repository name. If you do not specify a name the blank {@link String} "" is
         * used
         * 
         * @param name the name of the repositories
         */
        public DetachedContentRuntimeRepositoryManager.Builder setRepositoryName(String name) {
            this.repositoryName = name;
            return this;
        }

        String getRepositoryName() {
            return repositoryName;
        }

        /**
         * The {@link ClassLoader} to instantiate the classes. If you do not specify a
         * {@link ClassLoader} the {@link Thread#getContextClassLoader()} of
         * {@link Thread#currentThread()} is used.
         * 
         * @param classLoader The {@link ClassLoader} to instantiate the classes
         */
        public DetachedContentRuntimeRepositoryManager.Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        ClassLoader getClassLoader() {
            return classLoader;
        }

        /**
         * An optional {@link IFormulaEvaluatorFactory} to create {@link IFormulaEvaluator}s. If you
         * do not set the {@link IFormulaEvaluatorFactory} the repository tries to load the
         * generated subclasses of product components containting the compiled formula. This would
         * not work if you want to change product data while application is running.
         * 
         * @param formulaEvaluatorFactory {@link IFormulaEvaluatorFactory} to create
         *            {@link IFormulaEvaluator}s
         */
        public DetachedContentRuntimeRepositoryManager.Builder setFormulaEvaluatorFactory(IFormulaEvaluatorFactory formulaEvaluatorFactory) {
            this.formulaEvaluatorFactory = formulaEvaluatorFactory;
            return this;
        }

        IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
            return formulaEvaluatorFactory;
        }

        /**
         * Optionally you could specify a {@link ICacheFactory} to use your own cache
         * implementation. If you do not set a {@link ICacheFactory} the {@link DefaultCacheFactory}
         * is used.
         * 
         * @param cacheFactory The cacheFactory to set.
         */
        public DetachedContentRuntimeRepositoryManager.Builder setCacheFactory(ICacheFactory cacheFactory) {
            this.cacheFactory = cacheFactory;
            return this;
        }

        /**
         * @return Returns the cacheFactory.
         */
        ICacheFactory getCacheFactory() {
            return cacheFactory;
        }

        /**
         * This method builds a {@link DetachedContentRuntimeRepositoryManager}. All the values set
         * in the builder are used to create the manager and the
         * {@link DetachedContentRuntimeRepository}s
         * 
         * @return The created {@link DetachedContentRuntimeRepositoryManager}
         */
        public IDetachedContentRuntimeRepositoryManager build() {
            return new DetachedContentRuntimeRepositoryManager(this);
        }

    }

}
