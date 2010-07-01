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
import org.faktorips.runtime.ExpirableCacheFactory;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
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
 * <p>
 * IMPORTANT FOR MULTIUSER/THREADING:<br>
 * In a multiuser/threading environment using the {@link ProductDataProviderRuntimeRepository} you
 * run into problems with reloading the table of contents after product data has changed: A client
 * that receives product data from the repository gets a {@link DataModifiedRuntimeException} when
 * the version has changed. To get the new deployed product data he has to call {@link #reload()} in
 * order to reload the table of contents. The problem occurs if there is another client receiving
 * product data the same time. Maybe the other client already received some product data but have to
 * load some additional data. In this case, the client needs to get also a
 * {@link DataModifiedRuntimeException} in order to not mix different versions of product data.
 * Because of the first client has already called the {@link #reload()} method, the second client
 * would not get any exception to recognize the new product data version.<br>
 * To avoid this problem you have to use the {@link ClientRuntimeRepository}. For all clients there
 * could be a shared instance of {@link ProductDataProviderRuntimeRepository}.
 * 
 * 
 * @author dirmeier
 */

public class ProductDataProviderRuntimeRepository extends AbstractClassLoadingRuntimeRepository {

    private final IProductDataProvider productDataProvider;
    private final IFormulaEvaluatorFactory formulaEvaluatorFactory;

    /**
     * This is the constructor for the {@link ProductDataProviderRuntimeRepository} expecting a name
     * for the repository, a {@link ClassLoader} to load the product data instances, a
     * {@link IProductDataProvider} to get product data and optionally a
     * {@link IFormulaEvaluatorFactory} to evaluate formula instead of loading compiled code.
     * 
     * @param name The name of the runtime repository
     * @param cl the {@link ClassLoader} to load the product data instances
     * @param productDataProvider the {@link IProductDataProvider} to get the product data content
     * @param formulaEvaluatorFactory a {@link IFormulaEvaluatorFactory} to create a
     *            {@link org.faktorips.runtime.formula.IFormulaEvaluator} for evaluating formula on
     *            the fly instead of loading classes with compiled formulas. If you have no
     *            {@link IFormulaEvaluatorFactory} the repository try to load the classes containing
     *            compiled formula. In this case you could not change the product data in the
     *            product data provider because once loaded classes could not change.
     */
    public ProductDataProviderRuntimeRepository(String name, ClassLoader cl, IProductDataProvider productDataProvider,
            IFormulaEvaluatorFactory formulaEvaluatorFactory) {
        super(name, new ExpirableCacheFactory(productDataProvider), cl);
        this.productDataProvider = productDataProvider;
        this.formulaEvaluatorFactory = formulaEvaluatorFactory;
        reload();
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
    protected Element getDocumentElement(TestCaseTocEntry tocEntry) {
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

    protected synchronized boolean isExpired(String version) {
        return !getProductDataVersion().equals(version);
    }

    protected synchronized String getProductDataVersion() {
        return productDataProvider.getProductDataVersion();
    }

    @Override
    public synchronized void reload() {
        if (getTableOfContents() == null) {
            super.reload();
        }
        String tocVersion = getTableOfContents().getProductDataVersion();
        if (tocVersion == null || !tocVersion.equals(productDataProvider.getProductDataVersion())) {
            super.reload();
        }
    }

    @Override
    protected InputStream getXmlAsStream(EnumContentTocEntry tocEntry) {
        try {
            return productDataProvider.getEnumContentAsStream(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
        }
    }

    @Override
    protected InputStream getXmlAsStream(TableContentTocEntry tocEntry) {
        try {
            return productDataProvider.getTableContentAsStream(tocEntry);
        } catch (DataModifiedException e) {
            throw dataModifiedException(e);
        }
    }

    private RuntimeException dataModifiedException(DataModifiedException e) {
        // clear caches is not necessary because every cache is expiring for itself
        return new DataModifiedRuntimeException(e);
    }

    public boolean isModifiable() {
        return false;
    }

}
