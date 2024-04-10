/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl;

import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
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
    private Element node5;
    private Element node6;

    @Before
    public void setUp() throws Exception {
        buildStatus = mock(MultiStatus.class);
        JavaBuilderSet builderSet = mock(JavaBuilderSet.class);
        ipsProject = mock(IIpsProject.class);
        when(builderSet.getIpsProject()).thenReturn(ipsProject);
        helper = spy(new ExpressionXMLBuilderHelper(builderSet));
        doReturn("TestReturn").when(helper).generateJavaCode((IFormula)any(),
                (IProductCmptTypeMethod)any(), eq(buildStatus), any(MessageList.class));

        node1 = mock(Element.class);
        when(node1.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("NormaleFormel1");
        node2 = mock(Element.class);
        when(node2.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("NormaleFormel2");
        node3 = mock(Element.class);
        when(node3.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("StatischeFormel1");
        node4 = mock(Element.class);
        when(node4.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("StatischeFormel2");
        node5 = mock(Element.class);
        when(node5.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("FehlerhafteFormel");
        node6 = mock(Element.class);
        when(node6.getAttribute(IExpression.PROPERTY_FORMULA_SIGNATURE_NAME)).thenReturn("KompilierbareFormel");
        formulaElements = Arrays.asList(node1, node2, node3, node4, node5, node6);
    }

    @Test
    public void testAddCompiledFormulaExpressions_StatischeFormeln() throws Exception {
        IFormula formula1 = mock(IFormula.class);
        when(formula1.getFormulaSignature()).thenReturn("StatischeFormel1");
        IProductCmptTypeMethod method1 = mock(IProductCmptTypeMethod.class);
        when(method1.getParameters()).thenReturn(new IParameter[0]);
        when(formula1.findFormulaSignature(ipsProject)).thenReturn(method1);
        IFormula formula2 = mock(IFormula.class);
        when(formula2.getFormulaSignature()).thenReturn("StatischeFormel2");
        IProductCmptTypeMethod method2 = mock(IProductCmptTypeMethod.class);
        when(method2.getParameters()).thenReturn(new IParameter[0]);
        when(formula2.findFormulaSignature(ipsProject)).thenReturn(method2);
        List<IFormula> formulas = Arrays.asList(formula1, formula2);

        Map<String, MessageList> compilationErrors = helper.addCompiledFormulaExpressions(getTestDocument(), formulas,
                formulaElements, buildStatus);

        verify(node1, never()).appendChild((Node)any());
        verify(node2, never()).appendChild((Node)any());
        verify(node3).appendChild((Node)any());
        verify(node4).appendChild((Node)any());
        assertThat(compilationErrors.entrySet().size(), is(2));
        assertThat(compilationErrors.get("StatischeFormel1"), isEmpty());
        assertThat(compilationErrors.get("StatischeFormel2"), isEmpty());
    }

    @Test
    public void testAddCompiledFormulaExpressions_NichtStatischeFormeln() throws Exception {
        IFormula formula1 = mock(IFormula.class);
        when(formula1.getFormulaSignature()).thenReturn("NormaleFormel1");
        IProductCmptTypeMethod method1 = mock(IProductCmptTypeMethod.class);
        when(method1.getParameters()).thenReturn(new IParameter[0]);
        when(formula1.findFormulaSignature(ipsProject)).thenReturn(method1);
        IFormula formula2 = mock(IFormula.class);
        when(formula2.getFormulaSignature()).thenReturn("NormaleFormel2");
        IProductCmptTypeMethod method2 = mock(IProductCmptTypeMethod.class);
        when(method2.getParameters()).thenReturn(new IParameter[0]);
        when(formula2.findFormulaSignature(ipsProject)).thenReturn(method2);
        List<IFormula> formulas = Arrays.asList(formula1, formula2);

        Map<String, MessageList> compilationErrors = helper.addCompiledFormulaExpressions(getTestDocument(), formulas,
                formulaElements, buildStatus);

        verify(node1).appendChild((Node)any());
        verify(node2).appendChild((Node)any());
        verify(node3, never()).appendChild((Node)any());
        verify(node4, never()).appendChild((Node)any());
        assertThat(compilationErrors.entrySet().size(), is(2));
        assertThat(compilationErrors.get("NormaleFormel1"), isEmpty());
        assertThat(compilationErrors.get("NormaleFormel2"), isEmpty());
    }

    @Test
    public void testAddCompiledFormulaExpressions_CompilationErrors() throws Exception {
        IFormula formula1 = mock(IFormula.class);
        when(formula1.getFormulaSignature()).thenReturn("FehlerhafteFormel");
        IProductCmptTypeMethod method1 = mock(IProductCmptTypeMethod.class);
        when(method1.getParameters()).thenReturn(new IParameter[0]);
        when(formula1.findFormulaSignature(ipsProject)).thenReturn(method1);
        IFormula formula2 = mock(IFormula.class);
        when(formula2.getFormulaSignature()).thenReturn("KompilierbareFormel");
        IProductCmptTypeMethod method2 = mock(IProductCmptTypeMethod.class);
        when(method2.getParameters()).thenReturn(new IParameter[0]);
        when(formula2.findFormulaSignature(ipsProject)).thenReturn(method2);
        List<IFormula> formulas = Arrays.asList(formula1, formula2);
        doAnswer(invocation -> {
            MessageList messageList = invocation.getArgument(3, MessageList.class);
            messageList.add(Message.newError("666", "Compilation failed"));
            return "TestReturn";
        }).when(helper).generateJavaCode(eq(formula1),
                (IProductCmptTypeMethod)any(), eq(buildStatus), any(MessageList.class));

        Map<String, MessageList> compilationErrors = helper.addCompiledFormulaExpressions(getTestDocument(), formulas,
                formulaElements, buildStatus);

        verify(node5).appendChild((Node)any());
        verify(node6).appendChild((Node)any());
        assertThat(compilationErrors.entrySet().size(), is(2));
        assertThat(compilationErrors.get("FehlerhafteFormel"), containsErrorMessage());
        assertThat(compilationErrors.get("FehlerhafteFormel").getMessageByCode("666"),
                containsText("Compilation failed"));
        assertThat(compilationErrors.get("KompilierbareFormel"), isEmpty());
    }
}
