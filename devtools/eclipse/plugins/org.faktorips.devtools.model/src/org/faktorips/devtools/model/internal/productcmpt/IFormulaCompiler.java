/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An {@link IFormulaCompiler} is used to compile formulas in the
 * {@link ProductCmpt#toXml(Document)} method.
 *
 * @since 23.6
 */
public interface IFormulaCompiler {

    /**
     * Finds and compiles formulas in the XML representation of a {@link ProductCmpt product
     * component}.
     *
     * @param propertyValueContainer the container containing the formulas
     * @param document the XML document that contains the formula elements
     * @param node the parent element of which the formula elements are children
     */
    void compileFormulas(IPropertyValueContainer propertyValueContainer, Document document, Element node);

}
