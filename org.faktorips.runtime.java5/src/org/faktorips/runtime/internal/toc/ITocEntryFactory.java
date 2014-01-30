/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import javax.imageio.spi.ServiceRegistry;

import org.w3c.dom.Element;

/**
 * A {@link ITocEntryFactory} implementation is used to load {@link TocEntryObject}s of a given type
 * identified by their XML tag.
 * <p>
 * To make a {@link ITocEntryFactory} available during design time, it has to be registered with the
 * {@code org.faktorips.devtools.stdbuilder.tocEntryFactory} extension point.
 * </p>
 * <p>
 * At runtime, the extension point mechanism is not available and the {@link ServiceRegistry} is
 * used. For the {@link ServiceRegistry} to find the implementation, the full qualified name of the
 * implementation class must be put in the file
 * {@code META-INF/services/org.faktorips.runtime.internal.toc.ITocEntryFactory}. <strong>That file
 * and the implementation class need to be found by the {@link ClassLoader} provided to the
 * {@link AbstractReadonlyTableOfContents}'s constructor.</strong>
 * </p>
 * 
 * @author schwering
 */
public interface ITocEntryFactory<T extends TocEntryObject> {

    /**
     * Creates a {@link TocEntryObject} from it's XML representation.
     * 
     * @param entryElement the XML element representing the {@link TocEntryObject}
     * @return a {@link TocEntryObject}
     */
    T createFromXml(Element entryElement);

    /**
     * Returns the XML tag identifying a {@link TocEntryObject} this factory can create.
     * 
     * @return the XML tag identifying a {@link TocEntryObject} this factory can create
     */
    String getXmlTag();
}
