/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.productvariant.ProductVariantRuntimeHelper;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.internal.toc.TocEntry;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.values.InternationalString;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An abstract repository that handles the common stuff between the
 * {@link AbstractTocBasedRuntimeRepository} and the concrete runtime repositories. This abstract
 * layer is responsible for loading the classes and instantiates the objects. The content of the
 * objects - the concrete data - is provided by the concrete implementation.
 * 
 * @author dirmeier
 */
public abstract class AbstractClassLoadingRuntimeRepository extends AbstractTocBasedRuntimeRepository {

    private final ClassLoader cl;

    private final ProductVariantRuntimeHelper productVariantHelper = new ProductVariantRuntimeHelper();

    /**
     * 
     * @param name The name of the runtime repository
     * @param cacheFactory the cache factory used by this runtime repository
     * @param cl the {@link ClassLoader} used to load the classes
     */
    public AbstractClassLoadingRuntimeRepository(String name, ICacheFactory cacheFactory, ClassLoader cl) {
        super(name, cacheFactory, cl);
        this.cl = cl;
    }

    @Override
    protected IProductComponent createProductCmpt(ProductCmptTocEntry tocEntry) {
        Element prodCmptElement = getDocumentElement(tocEntry);
        if (!getProductVariantHelper().isProductVariantXML(prodCmptElement)) {
            ProductComponent productCmpt = createProductComponentInstance(tocEntry.getImplementationClassName(),
                    tocEntry.getIpsObjectId(), tocEntry.getKindId(), tocEntry.getVersionId());
            productCmpt.initFromXml(prodCmptElement);
            return productCmpt;
        } else {
            ProductComponent originalProdCmpt = getProductVariantHelper().getOriginalProdCmpt(this, prodCmptElement);
            ProductComponent productCmpt = createProductComponentInstance(originalProdCmpt.getClass().getName(),
                    tocEntry.getIpsObjectId(), tocEntry.getKindId(), tocEntry.getVersionId());
            getProductVariantHelper().initProductComponentVariation(originalProdCmpt, productCmpt, prodCmptElement);
            return productCmpt;
        }
    }

    protected ProductComponent createProductComponentInstance(String implementationClassName,
            String ipsObjectId,
            String kindId,
            String versionId) {
        ProductComponent productCmpt;
        try {
            Constructor<?> constructor = getProductComponentConstructor(implementationClassName);
            productCmpt = (ProductComponent)constructor
                    .newInstance(new Object[] { this, ipsObjectId, kindId, versionId });
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't create product component instance for class name \""
                    + implementationClassName + "\". RuntimeId=" + ipsObjectId, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Can't create product component instance for class name \""
                    + implementationClassName + "\". RuntimeId=" + ipsObjectId, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Can't create product component instance for class name \""
                    + implementationClassName + "\". RuntimeId=" + ipsObjectId, e);
        }
        return productCmpt;
    }

    private Constructor<?> getProductComponentConstructor(String implementationClassName) {
        try {
            Class<?> implClass = getClass(implementationClassName, getClassLoader());
            Class<?> runtimeRepoClass = getClass(IRuntimeRepository.class.getName(), getClassLoader());
            Constructor<?> constructor = implClass.getConstructor(new Class[] { runtimeRepoClass, String.class,
                    String.class, String.class });
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't create product component instance for class name \""
                    + implementationClassName);
        }
    }

    @Override
    protected <T> List<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> enumClass) {
        List<List<Object>> enumValueList = getEnumValueListFromSaxHandler(tocEntry);
        if (enumValueList.isEmpty()) {
            return Collections.emptyList();
        } else {
            Constructor<T> constructor = getCandidateConstructorThrowRuntimeException(tocEntry, enumClass,
                    getParameterSize(enumValueList));
            return getCreatedEnumValueList(tocEntry, enumValueList, constructor);
        }
    }

    private <T> List<T> getCreatedEnumValueList(EnumContentTocEntry tocEntry,
            List<List<Object>> enumValueList,
            Constructor<T> constructor) {
        T enumValue = null;
        ArrayList<T> enumValues = new ArrayList<T>();
        int valueCounterForIndexParameter = 0;
        for (List<Object> enumValueAsStrings : enumValueList) {
            constructor.setAccessible(true);
            Object[] enumAttributeValues = enumValueAsStrings.toArray();
            Object[] parameters = new Object[enumAttributeValues.length + 2];
            setValuesForParamters(valueCounterForIndexParameter, enumAttributeValues, parameters);
            enumValue = createEnumValue(constructor, parameters, tocEntry);
            enumValues.add(enumValue);
            valueCounterForIndexParameter++;
        }
        return enumValues;
    }

    private void setValuesForParamters(int valueCounterForIndexParameter,
            Object[] enumAttributeValues,
            Object[] parameters) {
        parameters[0] = valueCounterForIndexParameter;
        System.arraycopy(enumAttributeValues, 0, parameters, 1, enumAttributeValues.length);
        parameters[enumAttributeValues.length + 1] = this;
    }

    private int getParameterSize(List<List<Object>> enumValueList) {
        return enumValueList.get(0).size() + 2;
    }

    private <T> Constructor<T> getCandidateConstructorThrowRuntimeException(EnumContentTocEntry tocEntry,
            Class<T> enumClass,
            int parameterSize) {
        Class<?> runtimeRepoClass = getClass(IRuntimeRepository.class.getName(), getClassLoader());
        Constructor<T> constructor = getCorrectConstructor(parameterSize, runtimeRepoClass, enumClass);
        if (constructor == null) {
            throw new RuntimeException("No valid constructor found to create enumerations instances for the toc entry "
                    + tocEntry);
        }
        return constructor;
    }

    private List<List<Object>> getEnumValueListFromSaxHandler(EnumContentTocEntry tocEntry) {
        EnumSaxHandler saxhandler = parseEnumValues(tocEntry);
        List<List<Object>> enumValueList = saxhandler.getEnumValueList();
        return enumValueList;
    }

    private EnumSaxHandler parseEnumValues(EnumContentTocEntry tocEntry) {
        InputStream is = getXmlAsStream(tocEntry);
        EnumSaxHandler saxhandler = new EnumSaxHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), saxhandler);
        } catch (SAXException e) {
            throw new RuntimeException("Can't parse the enumeration content of the resource "
                    + tocEntry.getXmlResourceName(), e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Can't parse the enumeration content of the resource "
                    + tocEntry.getXmlResourceName(), e);
        } catch (IOException e) {
            throw new RuntimeException("Can't parse the enumeration content of the resource "
                    + tocEntry.getXmlResourceName(), e);
        }
        return saxhandler;
    }

    private <T> Constructor<T> getCorrectConstructor(int parameterSize, Class<?> runtimeRepoClass, Class<T> enumClass) {
        Constructor<?>[] constructors = enumClass.getDeclaredConstructors();
        Constructor<T> constructor = null;
        for (Constructor<?> currentConstructor : constructors) {
            if (isProtected(currentConstructor)) {
                Class<?>[] parameterTypes = currentConstructor.getParameterTypes();
                if (parameterTypes.length == parameterSize) {
                    boolean correct = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class<?> parameterClass = parameterTypes[i];
                        if (i == parameterTypes.length - 1) {
                            if (parameterClass != runtimeRepoClass) {
                                correct = false;
                                break;
                            }
                        } else if (isParameterClassValid(parameterClass)) {
                            correct = false;
                            break;
                        }
                    }
                    if (correct) {
                        @SuppressWarnings("unchecked")
                        // neccessary as Class.getDeclaredConstructors() is of type
                        // Constructor<?>[]
                        // while returning Contructor<T>[]
                        // The Javaoc Class.getDeclaredConstructors() for more information
                        Constructor<T> castedConstructor = (Constructor<T>)currentConstructor;
                        constructor = castedConstructor;
                        break;
                    }
                }
            }
        }
        return constructor;
    }

    private boolean isParameterClassValid(Class<?> parameterClass) {
        return parameterClass != String.class && parameterClass != InternationalString.class
                && parameterClass != Integer.TYPE;
    }

    private boolean isProtected(Constructor<?> currentConstructor) {
        return (currentConstructor.getModifiers() & Modifier.PROTECTED) > 0;
    }

    private <T> T createEnumValue(Constructor<T> constructor, Object[] parameters, TocEntry tocEntry) {
        T enumValue;
        try {
            enumValue = constructor.newInstance(parameters);
        } catch (InstantiationException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (IllegalAccessException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InvocationTargetException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        return enumValue;
    }

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry tocEntry) {
        Element genElement = getDocumentElement(tocEntry);
        if (!getProductVariantHelper().isProductVariantXML(genElement)) {
            ProductComponent productCmpt = (ProductComponent)getProductComponent(tocEntry.getParent().getIpsObjectId());
            if (productCmpt == null) {
                throw new RuntimeException("Can't get product component for toc entry " + tocEntry);
            }
            ProductComponentGeneration productCmptGen = createProductComponentGenerationInstance(tocEntry, productCmpt);
            productCmptGen.initFromXml(genElement);
            return productCmptGen;
        } else {
            return getProductVariantHelper().initProductComponentGenerationVariation(this, tocEntry, genElement);
        }
    }

    protected ProductVariantRuntimeHelper getProductVariantHelper() {
        return productVariantHelper;
    }

    protected ProductComponentGeneration createProductComponentGenerationInstance(GenerationTocEntry tocEntry,
            ProductComponent productCmpt) {
        ProductComponentGeneration productCmptGen;
        try {
            Constructor<?> constructor = getProdGenerationConstructor(tocEntry);
            productCmptGen = (ProductComponentGeneration)constructor.newInstance(new Object[] { productCmpt });
        } catch (IllegalAccessException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InstantiationException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InvocationTargetException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        return productCmptGen;
    }

    private RuntimeException createCannotInstantiateException(Exception e, TocEntry tocEntry) {
        return new RuntimeException("Can't create instance for toc entry " + tocEntry, e);
    }

    private Constructor<?> getProdGenerationConstructor(GenerationTocEntry tocEntry) {
        Class<?> implClass = getClass(getProductComponentGenerationImplClass(tocEntry), cl);
        try {
            String productCmptClassName = tocEntry.getParent().getImplementationClassName();
            Class<?> productCmptClass = getClass(productCmptClassName, cl);
            return implClass.getConstructor(new Class[] { productCmptClass });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't get constructor for class " + implClass.getName() + " , toc entry "
                    + tocEntry, e);
        }
    }

    /**
     * Override the default implementation for better performance. The default implementation
     * instantiates all product component before using the class filter. In this implementation we
     * use the information in the toc to filter the list of product components before instantiation.
     */
    @Override
    protected <T extends IProductComponent> void getAllProductComponentsInternal(Class<T> productCmptClass,
            List<T> result) {
        List<ProductCmptTocEntry> entries = getTableOfContents().getProductCmptTocEntries();
        for (ProductCmptTocEntry entry : entries) {
            Class<?> clazz = getClass(entry.getImplementationClassName(), cl);
            if (productCmptClass.isAssignableFrom(clazz)) {
                // checked by isAssignableFrom
                @SuppressWarnings("unchecked")
                T productComponentInternal = (T)getProductComponentInternal(entry.getIpsObjectId());
                result.add(productComponentInternal);
            }
        }
    }

    @Override
    protected ITable createTable(TableContentTocEntry tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        Table<?> table;
        try {
            Constructor<?> constructor = getTableConstructor(implClass, tocEntry);
            table = (Table<?>)constructor.newInstance(new Object[0]);
        } catch (IllegalAccessException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InstantiationException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InvocationTargetException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }

        InputStream is = getXmlAsStream(tocEntry);

        try {
            table.initFromXml(is, this);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse xml for " + tocEntry.getIpsObjectId(), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close the input stream for : " + tocEntry.getIpsObjectId(), e);
            }
        }
        return table;
    }

    private Constructor<?> getTableConstructor(Class<?> implClass, TableContentTocEntry tocEntry) {
        Constructor<?> constructor;
        try {
            constructor = implClass.getConstructor(new Class[0]);
        } catch (NoSuchMethodException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        return constructor;
    }

    @Override
    protected IpsTestCaseBase createTestCase(TestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository) {
        IpsTestCaseBase test;
        try {
            Constructor<?> constructor = getTestCaseConstructor(tocEntry);
            test = (IpsTestCaseBase)constructor.newInstance(new Object[] { tocEntry.getIpsObjectQualifiedName() });
        } catch (IllegalAccessException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InstantiationException e) {
            throw createCannotInstantiateException(e, tocEntry);
        } catch (InvocationTargetException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        /*
         * sets the runtime repository which will be used to instantiate the test case, this could
         * be a different one (e.g. contains more dependence repositories) as the test case belongs
         * to, because the test case itself could contain objects from different repositories, the
         * runtime repository should contain all needed repositories
         */
        test.setRepository(runtimeRepository);
        if (test instanceof IpsTestCase2) {
            // only classes of type ips test case 2 supports xml input
            Element docElement = getDocumentElement(tocEntry);
            ((IpsTestCase2)test).initFromXml(docElement);
        }
        test.setFullPath(tocEntry.getIpsObjectId());
        return test;
    }

    private Constructor<?> getTestCaseConstructor(TestCaseTocEntry tocEntry) {
        try {
            Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
            Constructor<?> constructor = implClass.getConstructor(new Class[] { String.class });
            return constructor;
        } catch (NoSuchMethodException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return cl;
    }

    /**
     * This method returns the xml element of the product component identified by the tocEntry
     */
    protected abstract Element getDocumentElement(ProductCmptTocEntry tocEntry);

    /**
     * This method returns the xml element of the product component generation identified by the
     * tocEntry
     */
    protected abstract Element getDocumentElement(GenerationTocEntry tocEntry);

    /**
     * This method returns the xml element of the test case identified by the tocEntry
     */
    protected abstract Element getDocumentElement(TestCaseTocEntry tocEntry);

    /**
     * This method returns the name of the product component generation implementation class
     * identified by the tocEntry. This could either be an implementation class using the formula
     * evaluation or an implementation class containing the compiled formulas.
     */
    protected abstract String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry);

    /**
     * Returns the XML data for the specified tocEntry as {@link InputStream}
     * 
     * @param tocEntry Specifying the requested EnumContent
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(EnumContentTocEntry tocEntry);

    /**
     * Returns the XML data for the specified tocEntry as {@link InputStream}
     * 
     * @param tocEntry Specifying the requested TableContent
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(TableContentTocEntry tocEntry);

    @Override
    protected <T> T createCustomObject(CustomTocEntryObject<T> tocEntry) {
        T runtimeObject = tocEntry.createRuntimeObject(this);
        if (runtimeObject instanceof IClRepositoryObject) {
            initClRepositoryObject(tocEntry, (IClRepositoryObject)runtimeObject);
        }
        return runtimeObject;
    }

    protected <T> void initClRepositoryObject(CustomTocEntryObject<T> tocEntry, IClRepositoryObject runtimeObject) {
        Element docElement = getDocumentElement(tocEntry);
        runtimeObject.initFromXml(docElement);
    }

    /**
     * This method returns the xml element of the product component identified by the tocEntry
     */
    protected abstract <T> Element getDocumentElement(CustomTocEntryObject<T> tocEntry);
}
