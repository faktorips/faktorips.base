/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plugin.extensions;

import org.faktorips.devtools.model.internal.productcmpt.IFormulaCompiler;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IFormulaCompiler}-supplier collecting all implementations of the extension point
 * {@value #EXTENSION_POINT_ID_FORMULA_COMPILER}.
 */
public class FormulaCompilerExtension extends SimpleSingleLazyExtension<IFormulaCompiler> {

    public static final String EXTENSION_POINT_ID_FORMULA_COMPILER = "formulaCompiler"; //$NON-NLS-1$

    public FormulaCompilerExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints, EXTENSION_POINT_ID_FORMULA_COMPILER, ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IFormulaCompiler.class,
                () -> (propertyValueContainer, document, node) -> {
                    // do nothing
                });
    }
}
