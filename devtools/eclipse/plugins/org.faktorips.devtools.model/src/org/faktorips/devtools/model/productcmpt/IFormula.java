/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;

/**
 * A {@link IFormula} is a {@link IExpression} used by a {@link IPropertyValueContainer}. It
 * provides additional testing capabilities.
 * 
 * @author Jan Ortmann
 */
public interface IFormula extends IPropertyValue, IExpression {

    /**
     * Returns the method signature this formula implements. Returns <code>null</code> if the method
     * signature is not found.
     */
    @Override
    IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject);

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IFormula}.
     * 
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    IFormula findTemplateProperty(IIpsProject ipsProject);

}
