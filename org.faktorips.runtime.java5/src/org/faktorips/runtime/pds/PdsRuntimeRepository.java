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

import java.util.List;

import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.AbstractTocBasedRuntimeRepository;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.test.IpsTestCaseBase;

public class PdsRuntimeRepository extends AbstractTocBasedRuntimeRepository {

    public PdsRuntimeRepository(String name, ICacheFactory cacheFactory) {
        super(name, cacheFactory);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected <T> List<T> createEnumValues(IEnumContentTocEntry tocEntry, Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IProductComponent createProductCmpt(IProductCmptTocEntry tocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry generationTocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ITable createTable(ITableContentTocEntry tocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IpsTestCaseBase createTestCase(ITestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AbstractReadonlyTableOfContents loadTableOfContents() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isModifiable() {
        // TODO Auto-generated method stub
        return false;
    }

    // private IProductDataProvider productDataProvider;
    //
    // private ReadonlyTableOfContents productDataToc;
    //
    // @Override
    // public IFormulaEvaluatorBuilder getFormulaEvaluatorBuilder() {
    // return new GroovyEvaluator.Builder();
    // }
    //
    // @Override
    // public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent
    // productCmpt) {
    // if (productCmpt == null) {
    // throw new NullPointerException("The parameter productCmpt must not be null.");
    // }
    // TocEntryObject tocEntry = productDataToc.getProductCmptTocEntry(productCmpt.getId());
    // DateTime validFrom = tocEntry.getLatestGenerationEntry().getValidFrom();
    // String ipsObjectId = tocEntry.getIpsObjectId();
    // Element productCmptData = productDataProvider.getProductCmptData(ipsObjectId);
    // String implClassName = getGenerationImplClassName(productCmptData);
    // Element generationData = getGenerationElement(productCmptData, validFrom);
    // return loadProductComponentGeneration(productCmpt, implClassName, generationData);
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
    // TocEntryObject tocEntry = toc.getProductCmptTocEntry(id);
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
    // TocEntryObject tocEntry = toc.getProductCmptTocEntry(id);
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
    // IProductCmptTocEntry tocEntry = productDataToc.getProductCmptTocEntry(id);
    // if (tocEntry == null) {
    // return null;
    // }
    // GenerationTocEntry tocEntryGeneration = tocEntry.getGenerationEntry(effectiveDate);
    // if (tocEntryGeneration == null) {
    // return null;
    // }
    // IProductComponent productCmpt = getProductComponent(id);
    // Element productCmptData = productDataProvider.getProductCmptData(id);
    // String generationImplClassName = getGenerationImplClassName(productCmptData);
    // Element generationData = getGenerationElement(productCmptData,
    // tocEntryGeneration.getValidFrom());
    // return loadProductComponentGeneration(productCmpt, generationImplClassName, generationData);
    // }
    //
    // private String getGenerationImplClassName(Element productCmptData) {
    // String productCmptType = productCmptData.getAttribute("productCmptType");
    // for (ITocEntryObject entry : toc.getModelTypeTocEntries()) {
    // if (entry.getIpsObjectQualifiedName().equals(productCmptType)) {
    // return entry.getGenerationImplClassName();
    // }
    // }
    // throw new IllegalArgumentException("Could not find a generation implemenation class for " +
    // productCmptType);
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
    //
    // @Override
    // protected <T> List<T> createEnumValues(ITocEntryObject tocEntry, Class<T> clazz) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // protected IProductComponent createProductCmpt(IProductCmptTocEntry tocEntry) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // protected ITable createTable(ITableContentTocEntry tocEntry) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // protected IpsTestCaseBase createTestCase(ITocEntryObject tocEntry, IRuntimeRepository
    // runtimeRepository) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // public boolean isModifiable() {
    // return false;
    // }
    //
    // @Override
    // protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry
    // tocEntryGeneration) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // protected AbstractReadonlyTableOfContents loadTableOfContents() {
    // // TODO Auto-generated method stub
    // return null;
    // }

}
