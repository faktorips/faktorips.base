/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
