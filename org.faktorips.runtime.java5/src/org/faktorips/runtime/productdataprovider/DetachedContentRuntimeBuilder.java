package org.faktorips.runtime.productdataprovider;

import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IRuntimeRepositoryManager;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;

/**
 * A builder to create the {@link DetachedContentRuntimeRepositoryManager} and
 * {@link DetachedContentRuntimeRepository}
 * 
 * @author dirmeier
 */
public class DetachedContentRuntimeBuilder {

    private final IProductDataProviderFactory dataProviderFactory;
    private String repositoryName = "";
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private IFormulaEvaluatorFactory formulaEvaluatorFactory;
    private ICacheFactory cacheFactory = new DefaultCacheFactory(classLoader);

    /**
     * The only mandatory parameter is the {@link IProductDataProviderFactory}. It is used to create
     * the {@link IProductDataProvider}s.
     * 
     * @param productDataProviderFactory The builder to create {@link IProductDataProvider}s
     */
    public DetachedContentRuntimeBuilder(IProductDataProviderFactory productDataProviderFactory) {
        this.dataProviderFactory = productDataProviderFactory;
    }

    /**
     * @return Returns the dataProviderFactory.
     */
    public IProductDataProviderFactory getProductDataProviderFactory() {
        return dataProviderFactory;
    }

    /**
     * The optional repository name. If you do not specify a name the blank {@link String} "" is
     * used
     * 
     * @param name the name of the repositories
     */
    public DetachedContentRuntimeBuilder setRepositoryName(String name) {
        this.repositoryName = name;
        return this;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * The {@link ClassLoader} to instantiate the classes. If you do not specify a
     * {@link ClassLoader} the {@link Thread#getContextClassLoader()} of
     * {@link Thread#currentThread()} is used.
     * 
     * @param classLoader The {@link ClassLoader} to instantiate the classes
     */
    public DetachedContentRuntimeBuilder setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * An optional {@link IFormulaEvaluatorFactory} to create {@link IFormulaEvaluator}s. If you do
     * not set the {@link IFormulaEvaluatorFactory} the repository tries to load the generated
     * subclasses of product components containting the compiled formula. This would not work if you
     * want to change product data while application is running.
     * 
     * @param formulaEvaluatorFactory {@link IFormulaEvaluatorFactory} to create
     *            {@link IFormulaEvaluator}s
     */
    public DetachedContentRuntimeBuilder setFormulaEvaluatorFactory(IFormulaEvaluatorFactory formulaEvaluatorFactory) {
        this.formulaEvaluatorFactory = formulaEvaluatorFactory;
        return this;
    }

    public IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
        return formulaEvaluatorFactory;
    }

    /**
     * Optionally you could specify a {@link ICacheFactory} to use your own cache implementation. If
     * you do not set a {@link ICacheFactory} the {@link DefaultCacheFactory} is used.
     * 
     * @param cacheFactory The cacheFactory to set.
     */
    public DetachedContentRuntimeBuilder setCacheFactory(ICacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        return this;
    }

    /**
     * @return Returns the cacheFactory.
     */
    public ICacheFactory getCacheFactory() {
        return cacheFactory;
    }

    /**
     * This method builds a {@link DetachedContentRuntimeRepositoryManager}. All the values set in
     * the builder are used to create the manager and the {@link DetachedContentRuntimeRepository}s
     * 
     * @return The created {@link DetachedContentRuntimeRepositoryManager}
     */
    public IRuntimeRepositoryManager build() {
        return new DetachedContentRuntimeRepositoryManager(this);
    }

}