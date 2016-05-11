/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
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
     * 
     * @deprecated As of 3.14 a {@link Formula} can be part of both {@link IProductCmpt product
     *             components} and {@link ProductCmptGeneration product component generations}. Use
     *             {@link #getPropertyValueContainer()} and the common interface
     *             {@link IPropertyValueContainer} instead.
     */
    @Deprecated
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the method signature this formula implements. Returns <code>null</code> if the method
     * signature is not found.
     */
    @Override
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject);

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IFormula}.
     * 
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    public IFormula findTemplateProperty(IIpsProject ipsProject);

}
