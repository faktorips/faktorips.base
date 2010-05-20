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
import org.faktorips.runtime.internal.formula.IFormulaEvaluatorBuilder;
import org.faktorips.runtime.internal.formula.groovy.GroovyEvaluator;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.w3c.dom.Element;

public class ProductDataProviderRuntimeRepository extends AbstractClassLoaderRuntimeRepository {

    private final IProductDataProvider productDataProvider;

    public ProductDataProviderRuntimeRepository(String name, ClassLoader cl, IProductDataProvider productDataProvider) {
        super(name, new ExpirableCacheFactory(productDataProvider), cl);
        this.productDataProvider = productDataProvider;
        reload();
    }

    @Override
    public IFormulaEvaluatorBuilder getFormulaEvaluatorBuilder() {
        return new GroovyEvaluator.Builder();
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
        return tocEntry.getParent().getGenerationImplClassName();
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
