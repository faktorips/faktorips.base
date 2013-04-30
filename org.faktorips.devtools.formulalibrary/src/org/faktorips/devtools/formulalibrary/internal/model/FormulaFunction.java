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

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.method.BaseMethod;
import org.faktorips.devtools.core.internal.model.productcmpt.Expression;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.fl.FlFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IFormulaFunction}. It holds a {@link IBaseMethod} and a
 * {@link FormulaFunctionExpression}
 * 
 * @author frank
 */
public class FormulaFunction extends IpsObjectPart implements IFormulaFunction {

    public static final String TAG_NAME = "FormulaFunction"; //$NON-NLS-1$

    private IFormulaMethod formulaMethod = new FormulaMethod(this, getNextPartId());

    private IExpression expression = new FormulaFunctionExpression(this, getNextPartId());

    public FormulaFunction(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public String getName() {
        return formulaMethod.getFormulaName();
    }

    @Override
    public IFormulaMethod getFormulaMethod() {
        return formulaMethod;
    }

    @Override
    public IExpression getExpression() {
        return expression;
    }

    @Override
    public FlFunction getFlFunction() {
        return new FormulaLibraryFunctionFlFunctionAdapter(formulaMethod, getFunctionDescription());
    }

    private String getFunctionDescription() {
        String expressionString = getExpression() == null ? StringUtils.EMPTY : getExpression().getExpression();

        return IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(getFormulaMethod())
                + "\n\n" + Messages.FormulaFunction_expression //$NON-NLS-1$
                + "\n" + expressionString; //$NON-NLS-1$
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return new IIpsElement[] { formulaMethod, expression };
    }

    @Override
    protected void reinitPartCollectionsThis() {
        formulaMethod = null;
        expression = null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IFormulaMethod) {
            formulaMethod = (IFormulaMethod)part;
            return true;
        } else if (part instanceof FormulaFunctionExpression) {
            expression = (FormulaFunctionExpression)part;
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IBaseMethod) {
            formulaMethod = null;
            return true;
        } else if (part instanceof IExpression) {
            expression = null;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlNodeName = xmlTag.getNodeName();
        if (BaseMethod.XML_ELEMENT_NAME.equals(xmlNodeName)) {
            formulaMethod = new FormulaMethod(this, id);
            return formulaMethod;
        } else if (Expression.TAG_NAME.equals(xmlNodeName)) {
            expression = new FormulaFunctionExpression(this, id);
            return expression;
        } else {
            return null;
        }
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (IBaseMethod.class.equals(partType)) {
            formulaMethod = new FormulaMethod(this, getNextPartId());
            return formulaMethod;
        } else if (FormulaFunctionExpression.class.equals(partType)) {
            expression = new FormulaFunctionExpression(this, getNextPartId());
            return expression;
        }
        return null;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
}
