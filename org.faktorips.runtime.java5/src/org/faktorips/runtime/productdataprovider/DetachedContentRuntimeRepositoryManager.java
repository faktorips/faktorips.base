/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryManager;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.internal.AbstractRuntimeRepositoryManager;

/**
 * The {@link DetachedContentRuntimeRepositoryManager} manages the access to the
 * {@link DetachedContentRuntimeRepository}. To get a runtime repository that provides the actual
 * product data you have to call {@link #getActualRuntimeRepository()}.
 * <p>
 * To create a new {@link DetachedContentRuntimeRepositoryManager} use the internal {@link Builder}.
 * 
 * @see DetachedContentRuntimeRepository
 * 
 * @author dirmeier
 */
public class DetachedContentRuntimeRepositoryManager extends AbstractRuntimeRepositoryManager {

    private final Builder builder;

    /**
     * This is the constructor for the {@link DetachedContentRuntimeRepositoryManager}. The
     * constructor is only called from the internal {@link Builder}.
     */
    private DetachedContentRuntimeRepositoryManager(Builder builder) {
        this.builder = builder;
    }

    @Override
    public synchronized IDetachedContentRuntimeRepository getActualRuntimeRepository() {
        if (super.getActualRuntimeRepository() instanceof DetachedContentRuntimeRepository) {
            return (IDetachedContentRuntimeRepository)super.getActualRuntimeRepository();
        } else {
            throw new RuntimeException("Illegal repository class in detached content runtime repository.");
        }
    }

    @Override
    protected boolean isRepositoryUpToDate(IRuntimeRepository actualRuntimeRepository) {
        if (actualRuntimeRepository instanceof DetachedContentRuntimeRepository) {
            DetachedContentRuntimeRepository detachedContentRR = (DetachedContentRuntimeRepository)actualRuntimeRepository;
            return detachedContentRR.isUpToDate();
        } else {
            return false;
        }
    }

    @Override
    protected IRuntimeRepository createNewRuntimeRepository() {
        return new DetachedContentRuntimeRepository(builder.getRepositoryName(), builder.getCacheFactory(),
                builder.getClassLoader(), builder.getProductDataProviderFactory().newInstance(),
                builder.getFormulaEvaluatorFactory());
    }

    public static class Builder {

        private final IProductDataProviderFactory dataProviderFactory;
        private String repositoryName = "";
        private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        private IFormulaEvaluatorFactory formulaEvaluatorFactory;
        private ICacheFactory cacheFactory = new DefaultCacheFactory(classLoader);

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
        public Builder setRepositoryName(String name) {
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
        public Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        ClassLoader getClassLoader() {
            return classLoader;
        }

        /**
         * An optional {@link IFormulaEvaluatorFactory} to create {@link IFormulaEvaluator}s. If you
         * do not set the {@link IFormulaEvaluatorFactory} the repository tries to load the
         * generated subclasses of product components containing the compiled formula. This would
         * not work if you want to change product data while application is running.
         * 
         * @param formulaEvaluatorFactory {@link IFormulaEvaluatorFactory} to create
         *            {@link IFormulaEvaluator}s
         */
        public Builder setFormulaEvaluatorFactory(IFormulaEvaluatorFactory formulaEvaluatorFactory) {
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
        public Builder setCacheFactory(ICacheFactory cacheFactory) {
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
        public IRuntimeRepositoryManager build() {
            return new DetachedContentRuntimeRepositoryManager(this);
        }

    }

}
