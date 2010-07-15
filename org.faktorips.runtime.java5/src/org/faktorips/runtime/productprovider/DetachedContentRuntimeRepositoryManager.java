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

package org.faktorips.runtime.productprovider;

import java.io.InputStream;

import org.faktorips.runtime.AbstractClassLoadingRuntimeRepository;
import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.w3c.dom.Element;

/**
 * The {@link DetachedContentRuntimeRepositoryManager} manages the access to the internal
 * {@link ProductDataProviderRuntimeRepository}. To use the internal repository you have to get a
 * {@link DetachedContentRuntimeRepository}. This versionSafeRepository delegates every call to the
 * {@link ProductDataProviderRuntimeRepository} until the product data version has changed.
 * 
 * @see ProductDataProviderRuntimeRepository
 * @see DetachedContentRuntimeRepository
 * 
 * 
 * @author dirmeier
 */

public class DetachedContentRuntimeRepositoryManager {

    private final ProductDataProviderRuntimeRepository productDataProviderRuntimeRepository;

    public DetachedContentRuntimeRepositoryManager(String name, ClassLoader cl,
            IProductDataProvider.Builder productDataProviderBuilder, IFormulaEvaluatorFactory formulaEvaluatorFactory) {
        productDataProviderRuntimeRepository = new ProductDataProviderRuntimeRepository(name, cl,
                productDataProviderBuilder, formulaEvaluatorFactory);
    }

    public synchronized IRuntimeRepository startRequest() {
        productDataProviderRuntimeRepository.reloadIfModified();
        return productDataProviderRuntimeRepository.versionSafeRepository;
    }

    /**
     * The {@link ProductDataProviderRuntimeRepository} is a runtime repository that is able to
     * parse the product data provided from a {@link IProductDataProvider} and instantiates the
     * necessary objects.
     * <p>
     * Because the data from an {@link IProductDataProvider} could change over time the
     * {@link DetachedContentRuntimeRepositoryManager} have to look for modifications and
     * reinstantiates the classes if necessary. To ensure that the client operates on only one
     * version of product data, the access to this repository is only possible through a
     * {@link DetachedContentRuntimeRepository}.
     * 
     * @see DetachedContentRuntimeRepository
     */
    class ProductDataProviderRuntimeRepository extends AbstractClassLoadingRuntimeRepository {

        private final IProductDataProvider productDataProvider;

        private final IFormulaEvaluatorFactory formulaEvaluatorFactory;

        private volatile DetachedContentRuntimeRepository versionSafeRepository;

        /**
         * This is the constructor for the {@link ProductDataProviderRuntimeRepository} expecting a
         * name for the repository, a {@link ClassLoader} to load the product data instances, a
         * {@link IProductDataProvider.Builder} to get product data and optionally a
         * {@link IFormulaEvaluatorFactory} to evaluate formula instead of loading compiled code.
         * 
         * @param name The name of the runtime repository
         * @param classloader the {@link ClassLoader} to load the product data instances
         * @param productDataProviderBuilder the {@link IProductDataProvider.Builder} to create the
         *            {@link IProductDataProvider} from which we receive product data content
         * @param formulaEvaluatorFactory a {@link IFormulaEvaluatorFactory} to create a
         *            {@link org.faktorips.runtime.formula.IFormulaEvaluator} for evaluating formula
         *            on the fly instead of loading classes with compiled formulas. If you have no
         *            {@link IFormulaEvaluatorFactory} the repository try to load the classes
         *            containing compiled formula. In this case you could not change the product
         *            data in the product data provider because once loaded classes could not
         *            change. This parameter may be null.
         */
        private ProductDataProviderRuntimeRepository(String name, ClassLoader classloader,
                IProductDataProvider.Builder productDataProviderBuilder,
                IFormulaEvaluatorFactory formulaEvaluatorFactory) {
            super(name, new DefaultCacheFactory(), classloader);
            this.productDataProvider = productDataProviderBuilder.build();
            this.formulaEvaluatorFactory = formulaEvaluatorFactory;
            reload();
        }

        /**
         * Getting the {@link IFormulaEvaluatorFactory} set in the constructor. This could be null
         * if formula should not be evaluated on the fly. The repository would load the classes
         * containing the compiled formula.
         * 
         * @return the {@link IFormulaEvaluatorFactory} of this repository or null if formula should
         *         not be evaluated
         */
        @Override
        public IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
            return formulaEvaluatorFactory;
        }

        @Override
        protected Element getDocumentElement(ProductCmptTocEntry tocEntry) {
            try {
                return productDataProvider.getProductCmptData(tocEntry);
            } catch (DataModifiedException e) {
                throw createDataModifiedException(e);
            }
        }

        @Override
        protected Element getDocumentElement(GenerationTocEntry tocEntry) {
            try {
                return productDataProvider.getProductCmptGenerationData(tocEntry);
            } catch (DataModifiedException e) {
                throw createDataModifiedException(e);
            }
        }

        @Override
        protected Element getDocumentElement(TestCaseTocEntry tocEntry) {
            try {
                return productDataProvider.getTestcaseElement(tocEntry);
            } catch (DataModifiedException e) {
                throw createDataModifiedException(e);
            }
        }

        @Override
        protected String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry) {
            if (formulaEvaluatorFactory != null) {
                return tocEntry.getParent().getGenerationImplClassName();
            } else {
                return tocEntry.getImplementationClassName();
            }
        }

        @Override
        protected synchronized IReadonlyTableOfContents loadTableOfContents() {
            IReadonlyTableOfContents toc = productDataProvider.loadToc();
            return toc;
        }

        @Override
        protected InputStream getXmlAsStream(EnumContentTocEntry tocEntry) {
            try {
                return productDataProvider.getEnumContentAsStream(tocEntry);
            } catch (DataModifiedException e) {
                throw createDataModifiedException(e);
            }
        }

        @Override
        protected InputStream getXmlAsStream(TableContentTocEntry tocEntry) {
            try {
                return productDataProvider.getTableContentAsStream(tocEntry);
            } catch (DataModifiedException e) {
                throw createDataModifiedException(e);
            }
        }

        /**
         * Call a modification check on the product data provider. If there are any changes in the
         * product data, this method reloads the necessary data. If there are no changes this method
         * simply returns. This is used by the
         * {@link org.faktorips.runtime.productprovider.DetachedContentRuntimeRepositoryManager} to
         * reload the content after product data has changed.
         * 
         */
        private synchronized void reloadIfModified() {
            if (!productDataProvider.isCompatibleToBaseVersion(versionSafeRepository.getVersion())) {
                reload();
            }
        }

        @Override
        public synchronized void reload() {
            super.reload();
            versionSafeRepository = new DetachedContentRuntimeRepository(this, productDataProvider.getVersion());
        }

        /**
         * Check whether the given versionSafeRepository is still valid or not. The
         * versionSafeRepository is valid if it is equal to the singleton versionSafeRepository of
         * the {@link DetachedContentRuntimeRepositoryManager}. If the versionSafeRepository is
         * invalid, this method throws a {@link DataModifiedRuntimeException}.
         * 
         * @param versionSafeRepository The versionSafeRepository assert to be valid
         * @throws DataModifiedRuntimeException if versionSafeRepository is invalid
         * 
         */
        void assertValidTransaction(DetachedContentRuntimeRepository versionSafeRepository) {
            if (!this.versionSafeRepository.equals(versionSafeRepository)) {
                throw new DataModifiedRuntimeException("Transaction request out-dated data", versionSafeRepository
                        .getVersion(), productDataProvider.getVersion());
            }
        }

        private RuntimeException createDataModifiedException(DataModifiedException e) {
            // TODO do we clean the cache here or not?
            // initCaches();
            return new DataModifiedRuntimeException(e);
        }

        public boolean isModifiable() {
            return false;
        }

    }
}
