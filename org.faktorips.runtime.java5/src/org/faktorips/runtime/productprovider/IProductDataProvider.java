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

import org.faktorips.runtime.IVersionChecker;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.w3c.dom.Element;

/**
 * A {@link IProductDataProvider} provides the content of product data identified by its toc entry.
 * To get the table of contents you have to call 'loadToc'. The product data could change over time.
 * If the user tries to request data from product data provider that has changed since last
 * modification check, you get a {@link DataModifiedException}. To get correct the actualized
 * content you first have to reload your table of content by calling {@link #loadToc()}
 * 
 * @author dirmeier
 */
public interface IProductDataProvider extends IVersionChecker {

    /**
     * Reload the toc in the product data provider and returns it. This call also set the
     * modification time. When product data has changed the user have to call {@link #loadToc()} to
     * actualize the contents. Otherwise all other methods would throwing
     * {@link DataModifiedException}
     * 
     * @return The loaded toc
     */
    public IReadonlyTableOfContents loadToc();

    /**
     * Getting the product component data element for given tocEntry. If the toc has been modified
     * this method throws a {@link DataModifiedException}. To update the modification time you have
     * to reload the toc.
     * 
     * @param tocEntry The toc entry for the product component you want to load
     * @return the xml element of the requested product component
     * @throws DataModifiedException when toc data was modified
     */
    public Element getProductCmptData(ProductCmptTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the product component generation data element for given tocEntry. If the toc has been
     * modified this method throws a {@link DataModifiedException}. To update the modification time
     * you have to reload the toc.
     * 
     * @param tocEntry The toc entry for the product component generation you want to load
     * @return the xml element of the requested product component generation
     * @throws DataModifiedException when toc data was modified
     */
    public Element getProductCmptGenerationData(GenerationTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the test case data element for given tocEntry. If the toc has been modified this
     * method throws a {@link DataModifiedException}. To update the modification time you have to
     * reload the toc.
     * 
     * @param tocEntry The toc entry for the test case element you want to load
     * @return the xml element of the requested test case element
     * @throws DataModifiedException when toc data was modified
     */
    public Element getTestcaseElement(TestCaseTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the input stream of table content for given tocEntry. If the toc has been modified
     * this method throws a {@link DataModifiedException}. To update the modification time you have
     * to reload the toc.
     * 
     * @param tocEntry The toc entry for the table content you want to load
     * @return the input stream of the requested table content
     * @throws DataModifiedException when toc data was modified
     */
    public InputStream getTableContentAsStream(TableContentTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the input stream of enum content for given tocEntry. If the toc has been modified
     * this method throws a {@link DataModifiedException}. To update the modification time you have
     * to reload the toc.
     * 
     * @param tocEntry The toc entry for the enum content you want to load
     * @return the input stream of the requested enum content
     * @throws DataModifiedException when toc data was modified
     */
    public InputStream getEnumContentAsStream(EnumContentTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the actual version of the product data provider. This should be a cached version and
     * does not need to be requested from a file or server every time.
     * 
     * @return the version of the product data provider
     */
    public String getVersion();

    /**
     * Checks whether the given version is compatible to the base version. The base is the real
     * version of the service or file - not the cached version of the prodct data provider
     * 
     * @param version The version to be checked against base version
     * @return true if the version is compatible
     */
    public boolean isCompatibleToBaseVersion(String version);

    /**
     * This is a builder to build a {@link IProductDataProvider}. We use the builder pattern here to
     * guarantee one instance of {@link IProductDataProvider} is only used by one
     * {@link DetachedContentRuntimeRepositoryManager}
     * 
     * @author dirmeier
     */
    public static interface Builder {

        /**
         * This methods calls the constructor of the {@link IProductDataProvider} implementation
         * 
         * @return a new {@link IProductDataProvider}
         */
        public IProductDataProvider build();

    }

}
