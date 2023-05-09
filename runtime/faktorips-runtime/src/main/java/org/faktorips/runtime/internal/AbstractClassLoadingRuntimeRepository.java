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

import java.io.InputStream;
import java.util.List;

import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TocEntry;

/**
 * An abstract repository that handles the common stuff between the
 * {@link AbstractXmlInputStreamRepository} and the concrete runtime repositories. This abstract
 * layer is responsible for loading the classes and instantiates the objects. The content of the
 * objects - the concrete data - is provided by the concrete implementation.
 * 
 * @author dirmeier
 */
public abstract class AbstractClassLoadingRuntimeRepository extends AbstractXmlInputStreamRepository {

    private final ClassLoader cl;

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
    public ClassLoader getClassLoader() {
        return cl;
    }

    @Override
    protected InputStream getXmlAsStream(TocEntry tocEntry) {
        String resource = tocEntry.getXmlResourceName();
        InputStream is = getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resource + " for ToC entry " + tocEntry);
        }
        return is;
    }

    /**
     * This method returns the name of the product component generation implementation class
     * identified by the tocEntry. This could either be an implementation class using the formula
     * evaluation or an implementation class containing the compiled formulas.
     */
    protected abstract String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry);

}
