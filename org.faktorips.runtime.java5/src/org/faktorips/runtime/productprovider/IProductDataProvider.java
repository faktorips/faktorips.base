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

import org.faktorips.runtime.IModificationChecker;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.w3c.dom.Element;

/**
 * A product data provider is able to get the content of product data identified by its toc entry.
 * To get the toc entries you have to call 'loadToc'.
 * <p/>
 * If the user tries to request data from product data provider that has changed since last
 * modification check, you get a {@link DataModifiedException}.
 * 
 * @author dirmeier
 */
public interface IProductDataProvider extends IModificationChecker {

    /**
     * Reload the toc in the product data provider and returns it. This setting the modification
     * time.
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
    public Element getProductCmptData(IProductCmptTocEntry tocEntry) throws DataModifiedException;

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
    public Element getTestcaseElement(ITestCaseTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the input stream of table content for given tocEntry. If the toc has been modified
     * this method throws a {@link DataModifiedException}. To update the modification time you have
     * to reload the toc.
     * 
     * @param tocEntry The toc entry for the table content you want to load
     * @return the input stream of the requested table content
     * @throws DataModifiedException when toc data was modified
     */
    public InputStream getTableContentAsStream(ITableContentTocEntry tocEntry) throws DataModifiedException;

    /**
     * Getting the input stream of enum content for given tocEntry. If the toc has been modified
     * this method throws a {@link DataModifiedException}. To update the modification time you have
     * to reload the toc.
     * 
     * @param tocEntry The toc entry for the enum content you want to load
     * @return the input stream of the requested enum content
     * @throws DataModifiedException when toc data was modified
     */
    public InputStream getEnumContentAsStream(IEnumContentTocEntry tocEntry) throws DataModifiedException;

}
