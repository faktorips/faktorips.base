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

package org.faktorips.runtime.pds;

import java.io.InputStream;

import org.faktorips.runtime.AbstractClassLoaderRuntimeRepository;
import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.internal.formula.IFormulaEvaluatorBuilder;
import org.faktorips.runtime.internal.formula.groovy.GroovyEvaluator;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.w3c.dom.Element;

public class PdsRuntimeRepository extends AbstractClassLoaderRuntimeRepository {

    private final IProductDataProvider productDataProvider;

    public PdsRuntimeRepository(String name, ClassLoader cl, IProductDataProvider productDataProvider) {
        super(name, new DefaultCacheFactory(), cl);
        this.productDataProvider = productDataProvider;
        reload();
    }

    @Override
    public IFormulaEvaluatorBuilder getFormulaEvaluatorBuilder() {
        return new GroovyEvaluator.Builder();
    }

    @Override
    protected Element getDocumentElement(IProductCmptTocEntry tocEntry) {
        return productDataProvider.getProductCmptData(tocEntry);
    }

    @Override
    protected Element getDocumentElement(ITestCaseTocEntry tocEntry) {
        return productDataProvider.getTestcaseElement(tocEntry);
    }

    @Override
    protected String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry) {
        return tocEntry.getParent().getGenerationImplClassName();
    }

    @Override
    protected AbstractReadonlyTableOfContents loadTableOfContents() {
        return productDataProvider.loadToc();
    }

    @Override
    protected InputStream getXmlAsStream(IEnumContentTocEntry tocEntry) {
        return productDataProvider.getXmlAsStream(tocEntry);
    }

    @Override
    protected InputStream getXmlAsStream(ITableContentTocEntry tocEntry) {
        return productDataProvider.getXmlAsStream(tocEntry);
    }

    public boolean isModifiable() {
        return false;
    }

    // @Override
    // public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent
    // productCmpt) {
    // if (productCmpt == null) {
    // throw new NullPointerException("The parameter productCmpt must not be null.");
    // }
    // IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(productCmpt.getId());
    // DateTime validFrom = tocEntry.getLatestGenerationEntry().getValidFrom();
    // String ipsObjectId = tocEntry.getIpsObjectId();
    // Element productCmptData = productDataProvider.getProductCmptData(ipsObjectId);
    // String genImplClassName = tocEntry.getGenerationImplClassName();
    // Element generationData = getGenerationElement(productCmptData, validFrom);
    // return loadProductComponentGeneration(productCmpt, genImplClassName, generationData);
    // }
    //
    // @Override
    // protected IProductComponentGeneration
    // getNextProductComponentGenerationInternal(IProductComponentGeneration generation) {
    // IProductComponent productCmpt = generation.getProductComponent();
    // String generationImplClassName = generation.getClass().getName();
    // String id = generation.getProductComponent().getId();
    // TimeZone timeZone = TimeZone.getDefault();
    // Date validFromAsDate = generation.getValidFrom(timeZone);
    // Calendar validFromAsCalendar = Calendar.getInstance();
    // validFromAsCalendar.setTime(validFromAsDate);
    // IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
    // GenerationTocEntry tocEntryGeneration = tocEntry.getNextGenerationEntry(validFromAsCalendar);
    // if (tocEntryGeneration == null) {
    // return null;
    // }
    // DateTime validFrom = tocEntryGeneration.getValidFrom();
    // String ipsObjectId = tocEntry.getIpsObjectId();
    // Element productCmptData = productDataProvider.getProductCmptData(ipsObjectId);
    // Element generationData = getGenerationElement(productCmptData, validFrom);
    // return loadProductComponentGeneration(productCmpt, generationImplClassName, generationData);
    // }
    //
    // @Override
    // protected IProductComponentGeneration
    // getPreviousProductComponentGenerationInternal(IProductComponentGeneration generation) {
    // IProductComponent productCmpt = generation.getProductComponent();
    // String generationImplClassName = generation.getClass().getName();
    // String id = generation.getProductComponent().getId();
    // TimeZone timeZone = TimeZone.getDefault();
    // Date validFromAsDate = generation.getValidFrom(timeZone);
    // Calendar validFromAsCalendar = Calendar.getInstance();
    // validFromAsCalendar.setTime(validFromAsDate);
    // IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
    // GenerationTocEntry tocEntryGeneration =
    // tocEntry.getPreviousGenerationEntry(validFromAsCalendar);
    // if (tocEntryGeneration == null) {
    // return null;
    // }
    // DateTime validFrom = tocEntryGeneration.getValidFrom();
    // String ipsObjectId = tocEntry.getIpsObjectId();
    // Element productCmptData = productDataProvider.getProductCmptData(ipsObjectId);
    // Element generationData = getGenerationElement(productCmptData, validFrom);
    // return loadProductComponentGeneration(productCmpt, generationImplClassName, generationData);
    // }
    //
    // @Override
    // protected IProductComponentGeneration getProductComponentGenerationInternal(String id,
    // Calendar effectiveDate) {
    // IProductCmptTocEntry tocEntry = toc.getProductCmptTocEntry(id);
    // if (tocEntry == null) {
    // return null;
    // }
    // GenerationTocEntry tocEntryGeneration = tocEntry.getGenerationEntry(effectiveDate);
    // if (tocEntryGeneration == null) {
    // return null;
    // }
    // IProductComponent productCmpt = getProductComponent(id);
    // Element productCmptData = productDataProvider.getProductCmptData(id);
    // String generationImplClassName = tocEntry.getGenerationImplClassName();
    // Element generationData = getGenerationElement(productCmptData,
    // tocEntryGeneration.getValidFrom());
    // return loadProductComponentGeneration(productCmpt, generationImplClassName, generationData);
    // }
    //
    // private IProductComponentGeneration loadProductComponentGeneration(IProductComponent
    // productCmpt,
    // String generationImplClassName,
    // Element generationData) {
    // Class<?> clazz = getClass(generationImplClassName, getClassLoader());
    // Constructor<?> constructor;
    // try {
    // constructor = clazz.getConstructor(productCmpt.getClass());
    // ProductComponentGeneration productCmptGen = (ProductComponentGeneration)constructor
    // .newInstance(new Object[] { productCmpt });
    // productCmptGen.initFromXml(generationData);
    // return productCmptGen;
    // } catch (Exception e) {
    // throw new RuntimeException("Cannot load " + generationImplClassName, e);
    // }
    // }
    //
    // private Element getGenerationElement(Element prodCmptData, DateTime validFrom) {
    // NodeList nl = prodCmptData.getChildNodes();
    // for (int i = 0; i < nl.getLength(); i++) {
    // if ("Generation".equals(nl.item(i).getNodeName())) {
    // Element genElement = (Element)nl.item(i);
    // DateTime generationValidFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
    // if (generationValidFrom.equals(validFrom)) {
    // return genElement;
    // }
    // }
    // }
    // throw new IllegalArgumentException("Could not find generation for " + validFrom +
    // "in product component "
    // + prodCmptData.getAttribute("runtimeId"));
    // }

}
