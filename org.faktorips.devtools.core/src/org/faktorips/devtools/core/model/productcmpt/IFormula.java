/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;

/**
 * A {@link IFormula} is a {@link IExpression} use by a {@link IPropertyValueContainer}. It provides
 * additional testing capabilities.
 * 
 * @author Jan Ortmann
 */
public interface IFormula extends IPropertyValue, IExpression {

    /**
     * Returns the generation this formula belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the method signature this formula implements. Returns <code>null</code> if the method
     * signature is not found.
     */
    @Override
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject);

}
