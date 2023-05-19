/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.beans.Expression;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An abstract repository that handles loading Faktor-IPS runtime information from XML as
 * {@link InputStream InputStreams}. The concrete runtime repositories provide these streams, for
 * example the {@link ClassloaderRuntimeRepository} uses
 * {@link ClassLoader#getResourceAsStream(String)} to load the XML resources.
 */
public abstract class AbstractXmlInputStreamRepository extends AbstractTocBasedRuntimeRepository {

    private final ProductVariantRuntimeHelper productVariantHelper = new ProductVariantRuntimeHelper();

    public AbstractXmlInputStreamRepository(String name, ICacheFactory cacheFactory, ClassLoader cl) {
        super(name, cacheFactory, cl);
    }

    /**
     * Returns the XML data for the specified {@link EnumContentTocEntry} as {@link InputStream}
     *
     * @param tocEntry Specifying the requested EnumContent
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(EnumContentTocEntry tocEntry);

    /**
     * Returns the XML data for the specified {@link TableContentTocEntry} as {@link InputStream}
     *
     * @param tocEntry Specifying the requested TableContent
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(TableContentTocEntry tocEntry);

    /**
     * Returns the XML data for the specified {@link TocEntry} as {@link InputStream}
     *
     * @param tocEntry Specifying the requested ips object
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(TocEntry tocEntry);

    protected ProductVariantRuntimeHelper getProductVariantHelper() {
        return productVariantHelper;
    }

    @Override
    protected <T> IpsEnum<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> enumClass) {
        EnumContent enumContent = getEnumContentFromSaxHandler(tocEntry);
        if (enumContent == null) {
            return null;
        } else if (enumContent.getEnumValues().isEmpty()) {
            return new IpsEnum<>(new ArrayList<>(), enumContent.getDescription());
        } else {
            Constructor<T> constructor = getCandidateConstructorThrowRuntimeException(tocEntry, enumClass,
                    getParameterSize(enumContent.getEnumValues()));
            return getCreatedEnumValueList(tocEntry, enumContent, constructor,
                    getEnumValuesDefinedInType(enumClass).size());
        }
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

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry generationTocEntry) {
        Element genElement = getDocumentElement(generationTocEntry);
        if (!getProductVariantHelper().isProductVariantXML(genElement)) {
            ProductComponent productCmpt = (ProductComponent)getProductComponent(
                    generationTocEntry.getParent().getIpsObjectId());
            if (productCmpt == null) {
                throw new RuntimeException("Can't get product component for toc entry " + generationTocEntry);
            }
            ProductComponentGeneration productCmptGen = createProductComponentGenerationInstance(generationTocEntry,
                    productCmpt);
            productCmptGen.initFromXml(genElement);
            return productCmptGen;
        } else {
            return getProductVariantHelper().initProductComponentGenerationVariation(this, generationTocEntry,
                    genElement);
        }
    }

    @Override
    protected ITable<?> createTable(TableContentTocEntry tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        Table<?> table;
        try {
            Constructor<?> constructor = getTableConstructor(implClass, tocEntry);
            table = (Table<?>)constructor.newInstance();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        try (InputStream is = getXmlAsStream(tocEntry)) {
            table.initFromXml(is, this, tocEntry.getIpsObjectId());
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException("Can't parse xml for " + tocEntry.getIpsObjectId(), e);
        }
        return table;
    }

    @Override
    protected IpsTestCaseBase createTestCase(TestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository) {
        IpsTestCaseBase test;
        try {
            Constructor<?> constructor = getTestCaseConstructor(tocEntry);
            test = (IpsTestCaseBase)constructor.newInstance(tocEntry.getIpsObjectQualifiedName());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
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

    @Override
    protected <T> T createCustomObject(CustomTocEntryObject<T> tocEntry) {
        T runtimeObject = tocEntry.createRuntimeObject(this);
        if (runtimeObject instanceof IClRepositoryObject) {
            initRepositoryObject(tocEntry, (IClRepositoryObject)runtimeObject);
        }
        return runtimeObject;
    }

    /**
     * This method returns the xml element of the product component generation identified by the
     * tocEntry
     */
    protected Element getDocumentElement(GenerationTocEntry tocEntry) {
        Element docElement = getDocumentElement(tocEntry.getParent());
        NodeList nl = docElement.getChildNodes();
        DateTime validFrom = tocEntry.getValidFrom();
        for (int i = 0; i < nl.getLength(); i++) {
            if ("Generation".equals(nl.item(i).getNodeName())) {
                Element genElement = (Element)nl.item(i);
                DateTime generationValidFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
                if (validFrom.equals(generationValidFrom)) {
                    return genElement;
                }
            }
        }
        throw new RuntimeException("Can't find the generation for the ToC entry " + tocEntry);
    }

    /**
     * This method returns the xml element of the product component identified by the tocEntry
     */
    protected Element getDocumentElement(ProductCmptTocEntry tocEntry) {
        return getDocumentElementInternal(tocEntry);
    }

    /**
     * This method returns the xml element of the test case identified by the tocEntry
     */
    protected Element getDocumentElement(TestCaseTocEntry tocEntry) {
        return getDocumentElementInternal(tocEntry);
    }

    /**
     * This method returns the xml element of the ips object identified by the tocEntry
     */
    protected <T> Element getDocumentElement(CustomTocEntryObject<T> tocEntry) {
        return getDocumentElementInternal(tocEntry);
    }

    protected ProductComponentGeneration createProductComponentGenerationInstance(GenerationTocEntry tocEntry,
            ProductComponent productCmpt) {
        ProductComponentGeneration productCmptGen;
        // CSOFF: IllegalCatch
        // Must catch Exception because of ugly java API in Expression
        try {
            Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
            Expression expression = new Expression(implClass, "new", new Object[] { productCmpt });
            productCmptGen = (ProductComponentGeneration)expression.getValue();
        } catch (Exception e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        // CSON: IllegalCatch
        return productCmptGen;
    }

    protected ProductComponent createProductComponentInstance(String implementationClassName,
            String ipsObjectId,
            String kindId,
            String versionId) {
        ProductComponent productCmpt;
        try {
            Constructor<?> constructor = getProductComponentConstructor(implementationClassName);
            productCmpt = (ProductComponent)constructor
                    .newInstance(this, ipsObjectId, kindId, versionId);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Can't create product component instance for class name \""
                    + implementationClassName + "\". RuntimeId=" + ipsObjectId, e);
        }
        return productCmpt;
    }

    private Constructor<?> getTableConstructor(Class<?> implClass, TableContentTocEntry tocEntry) {
        try {
            return implClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
    }

    private <T> void initRepositoryObject(CustomTocEntryObject<T> tocEntry, IClRepositoryObject runtimeObject) {
        Element docElement = getDocumentElement(tocEntry);
        runtimeObject.initFromXml(docElement);
    }

    private EnumContent getEnumContentFromSaxHandler(EnumContentTocEntry tocEntry) {
        InputStream xmlAsStream = getXmlAsStream(tocEntry);
        if (isAvailable(xmlAsStream)) {
            return parseEnumValues(tocEntry, xmlAsStream);
        } else {
            return null;
        }
    }

    private static EnumContent parseEnumValues(EnumContentTocEntry tocEntry, InputStream xmlAsStream) {
        EnumSaxHandler saxhandler = new EnumSaxHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(xmlAsStream), saxhandler);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throwCantParseEnumContentException(tocEntry, e);
        }
        return saxhandler.getEnumContent();
    }

    private static void throwCantParseEnumContentException(EnumContentTocEntry tocEntry, Exception e) {
        throw new RuntimeException(
                "Can't parse the enumeration content of the resource " + tocEntry.getXmlResourceName(), e);
    }

    private static boolean isAvailable(InputStream inputStream) {
        try {
            return inputStream != null && inputStream.available() > 0;
        } catch (IOException e) {
            // obviously not available
        }
        return false;
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
            throw new RuntimeException(
                    "No valid constructor found to create enumerations instances for the toc entry " + tocEntry);
        }
        return constructor;
    }

    private <T> IpsEnum<T> getCreatedEnumValueList(EnumContentTocEntry tocEntry,
            EnumContent enumContent,
            Constructor<T> constructor,
            int startIndex) {
        T enumValue = null;
        List<T> enumValues = new ArrayList<>();
        int valueCounterForIndexParameter = startIndex;
        for (List<Object> enumValueAsStrings : enumContent.getEnumValues()) {
            constructor.setAccessible(true);
            Object[] enumAttributeValues = enumValueAsStrings.toArray();
            Object[] parameters = new Object[enumAttributeValues.length + 2];
            setValuesForParamters(valueCounterForIndexParameter, enumAttributeValues, parameters);
            enumValue = createEnumValue(constructor, parameters, tocEntry);
            enumValues.add(enumValue);
            valueCounterForIndexParameter++;
        }
        return new IpsEnum<>(enumValues, enumContent.getDescription());
    }

    private Constructor<?> getTestCaseConstructor(TestCaseTocEntry tocEntry) {
        try {
            Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
            return implClass.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
    }

    private RuntimeException createCannotInstantiateException(Exception e, TocEntry tocEntry) {
        return new RuntimeException("Can't create instance for toc entry " + tocEntry, e);
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
                        // necessary as Class.getDeclaredConstructors() is of type Constructor<?>[]
                        // while returning Constructor<T>[]
                        // See the Javadoc of Class#getDeclaredConstructors() for more information
                        Constructor<T> castedConstructor = (Constructor<T>)currentConstructor;
                        constructor = castedConstructor;
                        break;
                    }
                }
            }
        }
        return constructor;
    }

    private boolean isProtected(Constructor<?> currentConstructor) {
        return (currentConstructor.getModifiers() & Modifier.PROTECTED) > 0;
    }

    private boolean isParameterClassValid(Class<?> parameterClass) {
        return parameterClass != String.class && !InternationalString.class.isAssignableFrom(parameterClass)
                && parameterClass != Integer.TYPE;
    }

    private void setValuesForParamters(int valueCounterForIndexParameter,
            Object[] enumAttributeValues,
            Object[] parameters) {
        parameters[0] = valueCounterForIndexParameter;
        System.arraycopy(enumAttributeValues, 0, parameters, 1, enumAttributeValues.length);
        parameters[enumAttributeValues.length + 1] = this;
    }

    private <T> T createEnumValue(Constructor<T> constructor, Object[] parameters, TocEntry tocEntry) {
        T enumValue;
        try {
            enumValue = constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw createCannotInstantiateException(e, tocEntry);
        }
        return enumValue;
    }

    private Constructor<?> getProductComponentConstructor(String implementationClassName) {
        try {
            Class<?> implClass = getClass(implementationClassName, getClassLoader());
            Class<?> runtimeRepoClass = getClass(IRuntimeRepository.class.getName(), getClassLoader());
            return implClass
                    .getConstructor(runtimeRepoClass, String.class, String.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Can't create product component instance for class name \"" + implementationClassName);
        }
    }

    private Element getDocumentElementInternal(TocEntry tocEntry) {
        String resource = tocEntry.getXmlResourceName();
        Document doc;
        try (InputStream is = getXmlAsStream(tocEntry)) {
            DocumentBuilder documentBuilder = XmlUtil.getDocumentBuilder();
            doc = documentBuilder.parse(new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8)));
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException("Can't parse xml resource " + resource, e);
        }
        Element element = doc.getDocumentElement();
        if (element == null) {
            throw new RuntimeException("Xml resource " + resource + " hasn't got a document element.");
        }
        return element;
    }
}
