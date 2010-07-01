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

package org.faktorips.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.internal.AbstractTocBasedRuntimeRepository;
import org.faktorips.runtime.internal.EnumSaxHandler;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

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

    /**
     * 
     * @param name The name of the runtime repository
     * @param cacheFactory the cache factory used by this runtime repository
     * @param cl the {@link ClassLoader} used to load the classes
     */
    public AbstractClassLoadingRuntimeRepository(String name, ICacheFactory cacheFactory, ClassLoader cl) {
        super(name, cacheFactory);
        this.cl = cl;
    }

    @Override
    protected IProductComponent createProductCmpt(ProductCmptTocEntry tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        ProductComponent productCmpt;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] { IRuntimeRepository.class, String.class,
                    String.class, String.class });
            productCmpt = (ProductComponent)constructor.newInstance(new Object[] { this, tocEntry.getIpsObjectId(),
                    tocEntry.getKindId(), tocEntry.getVersionId() });
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        Element docElement = getDocumentElement(tocEntry);
        productCmpt.initFromXml(docElement);
        return productCmpt;
    }

    @Override
    protected <T> List<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> clazz) {
        InputStream is = getXmlAsStream(tocEntry);

        EnumSaxHandler saxhandler = new EnumSaxHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), saxhandler);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse the enumeration content of the resource "
                    + tocEntry.getXmlResourceName());
        }
        T enumValue = null;
        ArrayList<T> enumValues = new ArrayList<T>();
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<T> constructor = null;
            for (Constructor<?> currentConstructor : constructors) {
                if ((currentConstructor.getModifiers() & Modifier.PROTECTED) > 0) {
                    Class<?>[] parameterTypes = currentConstructor.getParameterTypes();
                    if (parameterTypes.length == 2 && parameterTypes[0] == List.class
                            && parameterTypes[1] == IRuntimeRepository.class) {
                        @SuppressWarnings("unchecked")
                        // neccessary as Class.getDeclaredConstructors() is of type Constructor<?>[]
                        // while returning Contructor<T>[]
                        // The Javaoc Class.getDeclaredConstructors() for more information
                        Constructor<T> castedConstructor = (Constructor<T>)currentConstructor;
                        constructor = castedConstructor;
                    }
                }
            }
            if (constructor == null) {
                throw new RuntimeException(
                        "No valid constructor found to create enumerations instances for the toc entry " + tocEntry);
            }
            for (List<String> enumValueAsStrings : saxhandler.getEnumValueList()) {
                constructor.setAccessible(true);
                enumValue = constructor.newInstance(new Object[] { enumValueAsStrings, this });
                enumValues.add(enumValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't create enumeration instance for toc entry " + tocEntry, e);
        }
        return enumValues;
    }

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry tocEntry) {
        ProductComponent productCmpt = (ProductComponent)getProductComponent(tocEntry.getParent().getIpsObjectId());
        if (productCmpt == null) {
            throw new RuntimeException("Can't get product component for toc entry " + tocEntry);
        }
        ProductComponentGeneration productCmptGen;
        try {
            Constructor<?> constructor = getConstructor(tocEntry);
            productCmptGen = (ProductComponentGeneration)constructor.newInstance(new Object[] { productCmpt });
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        Element genElement = getDocumentElement(tocEntry);
        productCmptGen.initFromXml(genElement);
        return productCmptGen;
    }

    private Constructor<?> getConstructor(GenerationTocEntry tocEntry) {
        Class<?> implClass = getClass(getProductComponentGenerationImplClass(tocEntry), cl);
        try {
            String productCmptClassName = tocEntry.getParent().getImplementationClassName();
            Class<?> productCmptClass = getClass(productCmptClassName, cl);
            return implClass.getConstructor(new Class[] { productCmptClass });
        } catch (Exception e) {
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
    public void getAllProductComponentsInternal(Class<?> productCmptClass, List<IProductComponent> result) {
        List<ProductCmptTocEntry> entries = getTableOfContents().getProductCmptTocEntries();
        for (ProductCmptTocEntry entry : entries) {
            Class<?> clazz = getClass(entry.getImplementationClassName(), cl);
            if (productCmptClass.isAssignableFrom(clazz)) {
                result.add(getProductComponentInternal(entry));
            }
        }
    }

    @Override
    protected ITable createTable(TableContentTocEntry tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        Table table;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[0]);
            table = (Table)constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Can't create table instance for toc entry " + tocEntry, e);
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

    @Override
    protected IpsTestCaseBase createTestCase(TestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        IpsTestCaseBase test;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] { String.class });
            test = (IpsTestCaseBase)constructor.newInstance(new Object[] { tocEntry.getIpsObjectQualifiedName() });
        } catch (Exception e) {
            throw new RuntimeException("Can't create test case instance for toc entry " + tocEntry, e);
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

}