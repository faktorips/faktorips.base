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

import java.io.InputStream;

import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.internal.AbstractClassLoadingRuntimeRepository;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.w3c.dom.Element;

/**
 * /** The {@link DetachedContentRuntimeRepository} is a runtime repository that is able to parse
 * the product data provided from a {@link IProductDataProvider} and instantiates the necessary
 * objects.
 * <p>
 * Because the data from an {@link IProductDataProvider} could change over time the
 * {@link DetachedContentRuntimeRepository} have to look for modifications and reinstantiates the
 * classes if necessary. Once product data has changed, this repository only provides the already
 * cached data. If the user requests new data it gets a {@link DataModifiedRuntimeException}.
 * <p>
 * This implements a optimistic locking mechanism. That means that nothing happens until the
 * requested data is invalid. Once the product data has changed, this repository could still get
 * cached data. That means every request that only calls data from the cache would continue without
 * errors. To guarantee that a client does not work on out-dated data it has to get the actual
 * {@link DetachedContentRuntimeRepository} from {@link DetachedContentRuntimeRepositoryManager} for
 * every new request.
 * 
 * @author dirmeier
 */
public class DetachedContentRuntimeRepository extends AbstractClassLoadingRuntimeRepository {

    private final IProductDataProvider productDataProvider;
    private final IFormulaEvaluatorFactory formulaEvaluatorFactory;

    DetachedContentRuntimeRepository(String repositoryName, ICacheFactory cacheFactory, ClassLoader classLoader,
            IProductDataProvider productDataProvider, IFormulaEvaluatorFactory formulaEvaluatorFactory) {

        super(repositoryName, cacheFactory, classLoader);
        this.productDataProvider = productDataProvider;
        this.formulaEvaluatorFactory = formulaEvaluatorFactory;
        super.initialize();
    }

    @Override
    protected synchronized IReadonlyTableOfContents loadTableOfContents() {
        IReadonlyTableOfContents toc = productDataProvider.getToc();
        return toc;
    }

    /**
     * Getting the {@link IFormulaEvaluatorFactory} set in the constructor. This could be null if
     * formula should not be evaluated on the fly. The repository would load the classes containing
     * the compiled formula.
     * 
     * @return the {@link IFormulaEvaluatorFactory} of this repository or null if formula should not
     *         be evaluated
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
            throw createDataModifiedRuntimeException(e);
        }
    }

    @Override
    protected Element getDocumentElement(GenerationTocEntry tocEntry) {
        try {
            return productDataProvider.getProductCmptGenerationData(tocEntry);
        } catch (DataModifiedException e) {
            throw createDataModifiedRuntimeException(e);
        }
    }

    @Override
    protected Element getDocumentElement(TestCaseTocEntry tocEntry) {
        try {
            return productDataProvider.getTestcaseElement(tocEntry);
        } catch (DataModifiedException e) {
            throw createDataModifiedRuntimeException(e);
        }
    }

    @Override
    protected String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry) {
        if (getFormulaEvaluatorFactory() != null) {
            return tocEntry.getParent().getGenerationImplClassName();
        } else {
            return tocEntry.getImplementationClassName();
        }
    }

    @Override
    protected InputStream getXmlAsStream(EnumContentTocEntry tocEntry) {
        try {
            return productDataProvider.getEnumContentAsStream(tocEntry);
        } catch (DataModifiedException e) {
            throw createDataModifiedRuntimeException(e);
        }
    }

    @Override
    protected InputStream getXmlAsStream(TableContentTocEntry tocEntry) {
        try {
            return productDataProvider.getTableContentAsStream(tocEntry);
        } catch (DataModifiedException e) {
            throw createDataModifiedRuntimeException(e);
        }
    }

    /**
     * Returning the actual version set in the product data provider.
     * 
     * @see IProductDataProvider#getVersion()
     * 
     * @return version of the product data provider
     */
    public String getProductDataVersion() {
        return productDataProvider.getVersion();
    }

    boolean isUpToDate() {
        return productDataProvider.isCompatibleToBaseVersion();
    }

    private RuntimeException createDataModifiedRuntimeException(DataModifiedException e) {
        return new DataModifiedRuntimeException(e);
    }

    public boolean isModifiable() {
        return false;
    }

}
