/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ExpressionXMLBuilderHelperTest extends XmlAbstractTestCase {

    private ExpressionXMLBuilderHelper helper;
    private IIpsProject ipsProject;
    private MultiStatus buildStatus;
    private List<Element> formulaElements;
    private Element node1;
    private Element node2;
    private Element node3;
    private Element node4;

    @Before
    public void setUp() throws Exception {
        buildStatus = mock(MultiStatus.class);
        StandardBuilderSet builderSet = mock(StandardBuilderSet.class);
        ipsProject = mock(IIpsProject.class);
        when(builderSet.getIpsProject()).thenReturn(ipsProject);
        helper = spy(new ExpressionXMLBuilderHelper(builderSet));
        doReturn("TestReturn").when(helper).generateJavaCode((IFormula)any(),
                (IProductCmptTypeMethod)any(), eq(buildStatus));

        node1 = mock(Element.class);
        when(node1.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("NormaleFormel1");
        node2 = mock(Element.class);
        when(node2.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("NormaleFormel2");
        node3 = mock(Element.class);
        when(node3.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("StatischeFormel1");
        node4 = mock(Element.class);
        when(node4.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("StatischeFormel2");
        formulaElements = Arrays.asList(node1, node2, node3, node4);
    }

    @Test
    public void testAddCompiledFormulaExpressions_StatischeFormeln() throws Exception {
        IFormula formula1 = mock(IFormula.class);
        when(formula1.getFormulaSignature()).thenReturn("StatischeFormel1");
        IProductCmptTypeMethod method1 = mock(IProductCmptTypeMethod.class);
        when(formula1.findFormulaSignature(ipsProject)).thenReturn(method1);
        IFormula formula2 = mock(IFormula.class);
        when(formula2.getFormulaSignature()).thenReturn("StatischeFormel2");
        IProductCmptTypeMethod method2 = mock(IProductCmptTypeMethod.class);
        when(formula2.findFormulaSignature(ipsProject)).thenReturn(method2);
        List<IFormula> formulas = Arrays.asList(formula1, formula2);

        helper.addCompiledFormulaExpressions(getTestDocument(), formulas, formulaElements, buildStatus);

        verify(node1, never()).appendChild((Node)any());
        verify(node2, never()).appendChild((Node)any());
        verify(node3).appendChild((Node)any());
        verify(node4).appendChild((Node)any());
    }

    @Test
    public void testAddCompiledFormulaExpressions_NichtStatischeFormeln() throws Exception {
        IFormula formula1 = mock(IFormula.class);
        when(formula1.getFormulaSignature()).thenReturn("NormaleFormel1");
        IProductCmptTypeMethod method1 = mock(IProductCmptTypeMethod.class);
        when(formula1.findFormulaSignature(ipsProject)).thenReturn(method1);
        IFormula formula2 = mock(IFormula.class);
        when(formula2.getFormulaSignature()).thenReturn("NormaleFormel2");
        IProductCmptTypeMethod method2 = mock(IProductCmptTypeMethod.class);
        when(formula2.findFormulaSignature(ipsProject)).thenReturn(method2);
        List<IFormula> formulas = Arrays.asList(formula1, formula2);

        helper.addCompiledFormulaExpressions(getTestDocument(), formulas, formulaElements, buildStatus);

        verify(node1).appendChild((Node)any());
        verify(node2).appendChild((Node)any());
        verify(node3, never()).appendChild((Node)any());
        verify(node4, never()).appendChild((Node)any());
    }
}
