/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.internal.model;

import java.util.Map;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.Expression;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;

/**
 * Implementation of {@link Expression} to use in formula library functions.
 * 
 * @author frank
 */
public class FormulaFunctionExpression extends Expression {

    public FormulaFunctionExpression(IFormulaFunction parent, String id) {
        super(parent, id);
    }

    public IFormulaFunction getFormulaFunction() {
        return (IFormulaFunction)getParent();
    }

    @Override
    public IBaseMethod findFormulaSignature(IIpsProject ipsProject) {
        return getFormulaFunction().getFormulaMethod();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return null;
    }

    @Override
    protected ITableContentUsage[] getTableContentUsages() {
        return new ITableContentUsage[0];
    }

    @Override
    protected void collectEnumTypesFromAttributes(Map<String, EnumDatatype> enumTypes) {
        // there are no attributes hence we have nothing to collect
    }

    @Override
    public boolean isFormulaMandatory() {
        return true;
    }
}
