/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ParameterParserTest extends AbstractParserTest {

    private static final String ANY_PARAMETER = "noParameter";

    static final String MY_PARAMETER = "anyParameter";

    private static final String MY_LABEL = "myLabel";

    private static final String MY_DESCRIPTION = "myDescription";

    @Mock
    private IProductCmptTypeMethod formulaSignature;

    @Mock
    private IParameter parameter;

    private ParameterParser parameterParser;

    @Mock
    private IType type;

    @Before
    public void createParameterParser() throws Exception {
        parameterParser = new ParameterParser(getParsingContext());
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
        getParsingContext().pushNode(new TestNode(mock(IProductCmptType.class)));

        IdentifierNode parameterNode = parameterParser.parse(new TextRegion(MY_PARAMETER, 0, MY_PARAMETER.length()));

        assertNull(parameterNode);
    }

    @Test
    public void testParse_noParameter() throws Exception {
        IdentifierNode parameterNode = parameterParser.parse(new TextRegion(ANY_PARAMETER, 0, ANY_PARAMETER.length()));

        assertNull(parameterNode);
    }

    @Test
    public void testParse_findParameter() throws Exception {
        ParameterNode parameterNode = (ParameterNode)parameterParser
                .parse(new TextRegion(MY_PARAMETER, 0, MY_PARAMETER.length()));

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

        InvalidIdentifierNode node = (InvalidIdentifierNode)parameterParser
                .parse(new TextRegion(MY_PARAMETER, 0, MY_PARAMETER.length()));

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

    @Test
    public void testGetProposals_noPrefix() throws Exception {
        parameterParser.parse(new TextRegion("any", 0, 0));

        List<IdentifierProposal> proposals = parameterParser.getProposals(IpsStringUtils.EMPTY);

        assertEquals(1, proposals.size());
        assertEquals(parameter.getName(), proposals.get(0).getText());
    }

    @Test
    public void testGetProposals_invalidPrefix() throws Exception {
        parameterParser.parse(new TextRegion("any", 0, 0));

        List<IdentifierProposal> proposals = parameterParser.getProposals("gfassddf");

        assertTrue(proposals.isEmpty());
    }

    @Test
    public void testGetProposals_withPrefix() throws Exception {
        parameterParser.parse(new TextRegion("any", 0, 0));

        List<IdentifierProposal> proposals = parameterParser.getProposals("any");

        assertEquals(1, proposals.size());
        assertEquals(parameter.getName(), (proposals.get(0)).getText());
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

        parameterParser.parse(new TextRegion("any", 0, 0));

        List<IdentifierProposal> proposals = parameterParser.getProposals("any");

        assertEquals(2, proposals.size());
        assertEquals(parameter.getName(), proposals.get(0).getText());
        assertEquals(param3.getName(), proposals.get(1).getText());
    }

    @Test
    public void testGetDescription_forTypes() throws Exception {
        when(parameter.findDatatype(getIpsProject())).thenReturn(type);
        when(getMultiLanguageSupport().getLocalizedLabel(type)).thenReturn(MY_LABEL);
        when(getMultiLanguageSupport().getLocalizedDescription(type)).thenReturn(MY_DESCRIPTION);

        String description = parameterParser.getDescription(parameter);

        assertEquals(MY_LABEL + " - " + MY_DESCRIPTION, description);
    }

}
