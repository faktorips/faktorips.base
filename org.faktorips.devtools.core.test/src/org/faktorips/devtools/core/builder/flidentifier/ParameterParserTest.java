/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterParserTest extends AbstractParserTest {

    private static final String ANY_PARAMETER = "noParameter";

    static final String MY_PARAMETER = "anyParameter";

    @Mock
    private IFormulaMethod formulaSignature;

    @Mock
    private IParameter parameter;

    private ParameterParser parameterParser;

    @Before
    public void createParameterParser() throws Exception {
        parameterParser = new ParameterParser(getExpression(), getIpsProject());
    }

    @Before
    public void mockFormulaSignature() throws Exception {
        when(getExpression().findFormulaSignature(getIpsProject())).thenReturn(formulaSignature);
        when(formulaSignature.getParameters()).thenReturn(new IParameter[] { parameter });
        when(parameter.getName()).thenReturn(MY_PARAMETER);
        when(parameter.findDatatype(getIpsProject())).thenReturn(AnyDatatype.INSTANCE);
    }

    @Test
    public void testParse_wrongType() throws Exception {
        IdentifierNode parameterNode = parameterParser.parse(MY_PARAMETER, new TestNode(mock(IProductCmptType.class)),
                null);

        assertNull(parameterNode);
    }

    @Test
    public void testParse_noParameter() throws Exception {
        IdentifierNode parameterNode = parameterParser.parse(ANY_PARAMETER, null, null);

        assertNull(parameterNode);
    }

    @Test
    public void testParse_findParameter() throws Exception {
        ParameterNode parameterNode = (ParameterNode)parameterParser.parse(MY_PARAMETER, null, null);

        assertNotNull(parameterNode);
        assertEquals(parameter, parameterNode.getParameter());
        assertEquals(AnyDatatype.INSTANCE, parameterNode.getDatatype());
    }

    @Test
    public void testGetParameters() throws Exception {
        IParameter[] parameters = parameterParser.getParameters();

        assertEquals(1, parameters.length);
        assertEquals(parameter, parameters[0]);
    }

    @Test
    public void testParse_noDatatype() throws Exception {
        when(parameter.findDatatype(getIpsProject())).thenReturn(null);

        InvalidIdentifierNode node = (InvalidIdentifierNode)parameterParser.parse(MY_PARAMETER, null, null);

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

    @Test
    public void testGetProposals_noPrefix() throws Exception {
        parameterParser.parse(null, null, null);

        List<IdentifierNode> proposals = parameterParser.getProposals(StringUtils.EMPTY);

        assertEquals(1, proposals.size());
        assertEquals(parameter, ((ParameterNode)proposals.get(0)).getParameter());
    }

    @Test
    public void testGetProposals_invalidPrefix() throws Exception {
        parameterParser.parse(null, null, null);

        List<IdentifierNode> proposals = parameterParser.getProposals("gfassddf");

        assertTrue(proposals.isEmpty());
    }

    @Test
    public void testGetProposals_withPrefix() throws Exception {
        parameterParser.parse(null, null, null);

        List<IdentifierNode> proposals = parameterParser.getProposals("any");

        assertEquals(1, proposals.size());
        assertEquals(parameter, ((ParameterNode)proposals.get(0)).getParameter());
    }

    @Test
    public void testGetProposals_moreParameters() throws Exception {
        IParameter param1 = mock(IParameter.class);
        IParameter param2 = mock(IParameter.class);
        IParameter param3 = mock(IParameter.class);
        when(formulaSignature.getParameters()).thenReturn(new IParameter[] { parameter, param1, param2, param3 });
        when(param1.getName()).thenReturn("notInAny");
        when(param2.getName()).thenReturn("notInResult");
        when(param3.getName()).thenReturn("AnyX");
        when(param3.findDatatype(getIpsProject())).thenReturn(AnyDatatype.INSTANCE);

        parameterParser.parse(null, null, null);

        List<IdentifierNode> proposals = parameterParser.getProposals("any");

        assertEquals(2, proposals.size());
        assertEquals(parameter, ((ParameterNode)proposals.get(0)).getParameter());
        assertEquals(param3, ((ParameterNode)proposals.get(1)).getParameter());
    }

}
