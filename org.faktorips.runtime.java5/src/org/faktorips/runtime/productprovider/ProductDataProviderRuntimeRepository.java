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

import org.faktorips.runtime.AbstractClassLoaderRuntimeRepository;
import org.faktorips.runtime.ExpirableCacheFactory;
import org.faktorips.runtime.internal.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.w3c.dom.Element;

/**
 * The {@link ProductDataProviderRuntimeRepository} is a runtime repository for product data only.
 * It is able to parse the product data provided from a {@link IProductDataProvider} and
 * instantiates the necessary objects.
 * <p>
 * Because the data from an {@link IProductDataProvider} could change over time the
 * {@link ProductDataProviderRuntimeRepository} have to look for modifications and reinstantiates
 * the classes if necessary. It is also important to have an expirable implementation of the cache
 * so the {@link ProductDataProviderRuntimeRepository} uses the {@link ExpirableCacheFactory} to
 * create the cache objects.
 * 
 * @author dirmeier
 */

public class ProductDataProviderRuntimeRepository extends AbstractClassLoaderRuntimeRepository {

    private final IProductDataProvider productDataProvider;
    private final IFormulaEvaluatorFactory formulaEvaluatorFactory;

    public ProductDataProviderRuntimeRepository(String name, ClassLoader cl, IProductDataProvider productDataProvider,
            IFormulaEvaluatorFactory formulaEvaluatorFactory) {
        super(name, new ExpirableCacheFactory(productDataProvider), cl);
        this.productDataProvider = productDataProvider;
        this.formulaEvaluatorFactory = formulaEvaluatorFactory;
        reload();
    }

    @Override
    public IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
        try {
            return formulaEvaluatorFactory;
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate formula evaluator", e);
        }
    }

    @Override
    protected Element getDocumentElement(IProductCmptTocEntry tocEntry) {
        try {
            return productDataProvider.getProductCmptData(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
        }
    }

    @Override
    protected Element getDocumentElement(GenerationTocEntry tocEntry) {
        try {
            return productDataProvider.getProductCmptGenerationData(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
        }
    }

    @Override
    protected Element getDocumentElement(ITestCaseTocEntry tocEntry) {
        try {
            return productDataProvider.getTestcaseElement(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
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
    protected IReadonlyTableOfContents loadTableOfContents() {
        return productDataProvider.loadToc();
    }

    @Override
    protected InputStream getXmlAsStream(IEnumContentTocEntry tocEntry) {
        try {
            return productDataProvider.getEnumContentAsStream(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
        }
    }

    @Override
    protected InputStream getXmlAsStream(ITableContentTocEntry tocEntry) {
        try {
            return productDataProvider.getTableContentAsStream(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
        }
    }

    private RuntimeException dataModifiedException(DataModifiedException e) {
        // clear caches is not necessary because every cache is expiring for itself
        toc = loadTableOfContents();
        return new RuntimeException(e);
    }

    public boolean isModifiable() {
        return false;
    }

}
