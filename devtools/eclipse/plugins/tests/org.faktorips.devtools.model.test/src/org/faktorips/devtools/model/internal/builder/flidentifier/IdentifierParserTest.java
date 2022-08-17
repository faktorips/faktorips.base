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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.fl.IdentifierKind;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IndexNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.method.IFormulaMethod;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierParserTest {

    private static final String MY_PARAMETER = "anyParameter";

    private static final String MY_ATTRIBUTE = "myAttribute";

    private static final String MY_ASSOCIATION = "myAssociation";

    private static final String MY_QUALIFIER = "abc123";

    private static final String MY_ASSOCIATION_QUALIFIED = "myAssociation1[\"abc123\"]";

    private static final String MY_ASSOCIATION_INDEX = "myAssociation2[0]";

    private static final String MY_IDENTIFIER = MY_PARAMETER + '.' + MY_ASSOCIATION + '.' + MY_ASSOCIATION_QUALIFIED
            + '.' + MY_ASSOCIATION_INDEX + '.' + MY_ATTRIBUTE;

    private static final String MY_ENUMCLASS = "myEnumClas";

    private static final String MY_ENUMVALUE = "myEnumType";

    private IdentifierParser identifierParser;

    @Mock
    private IExpression expression;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IFormulaMethod formulaMethod;

    @Mock
    private IPolicyCmptType type;

    @Mock
    private IPolicyCmptType type1;

    @Mock
    private IPolicyCmptType type2;

    @Mock
    private IPolicyCmptType type3;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmpt productCmpt;

    @Mock
    private IIpsSrcFile productCmptIpsSrcFile;

    @Mock
    private IParameter parameter;

    @Mock
    private IAttribute attribute;

    @Mock
    private IAssociation association;

    @Mock
    private IAssociation associationQualified;

    @Mock
    private IPolicyCmptTypeAssociation associationIndexed;

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IdentifierFilter identifierFilter;

    @Mock
    private IIpsProjectProperties projectProperties;

    @Mock
    private ISupportedLanguage supportetLanguage;

    @Before
    public void createIdentifierParser() throws Exception {
        mockEnum();
        identifierParser = new IdentifierParser(expression, ipsProject, identifierFilter);
        when(type.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        when(type1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        when(type2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        when(type3.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        when(productCmptType.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);
    }

    private void mockEnum() throws Exception {
        when(expression.getEnumDatatypesAllowedInFormula()).thenReturn(new EnumDatatype[] { enumDatatype });
        when(enumDatatype.getName()).thenReturn(MY_ENUMCLASS);
        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { MY_ENUMVALUE });
    }

    @Before
    public void mockExpression() throws Exception {
        when(expression.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(expression.findFormulaSignature(ipsProject)).thenReturn(formulaMethod);
        when(formulaMethod.getParameters()).thenReturn(new IParameter[] { parameter });
        when(parameter.getName()).thenReturn(MY_PARAMETER);
        when(parameter.findDatatype(ipsProject)).thenReturn(type);
        when(parameter.getIpsProject()).thenReturn(ipsProject);
        when(type.findAssociation(MY_ASSOCIATION, ipsProject)).thenReturn(association);
        when(type.findAssociation(MY_ASSOCIATION + "1", ipsProject)).thenReturn(associationQualified);
        when(type.findAllAssociations(ipsProject)).thenReturn(Arrays.asList(association, associationQualified));
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(association.findTarget(ipsProject)).thenReturn(type1);
        when(association.is1ToMany()).thenReturn(true);
        when(association.getIpsProject()).thenReturn(ipsProject);
        when(association.getTarget()).thenReturn("target");
        when(association.getIpsProject()).thenReturn(ipsProject);
        when(association.getName()).thenReturn(MY_ASSOCIATION);
        when(associationQualified.getTarget()).thenReturn("target1");
        when(associationQualified.getIpsProject()).thenReturn(ipsProject);
        when(associationQualified.getName()).thenReturn(MY_ASSOCIATION + "1");
        when(type1.findAssociation(MY_ASSOCIATION + "1", ipsProject)).thenReturn(associationQualified);
        when(type1.findAllAssociations(ipsProject)).thenReturn(Arrays.asList(associationQualified));
        when(type1.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(type1.getIpsProject()).thenReturn(ipsProject);
        when(associationQualified.findTarget(ipsProject)).thenReturn(type2);
        when(type2.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(type2.getIpsProject()).thenReturn(ipsProject);
        IIpsSrcFile sourceFile = mock(IIpsSrcFile.class);
        IIpsSrcFile[] ipsSourceFiles = { sourceFile };
        when(ipsProject.findAllProductCmptSrcFiles(productCmptType, true)).thenReturn(ipsSourceFiles);
        when(sourceFile.getIpsObjectName()).thenReturn(MY_QUALIFIER);
        when(sourceFile.getIpsObject()).thenReturn(productCmpt);
        when(productCmpt.getRuntimeId()).thenReturn("runtimeId." + MY_QUALIFIER);
        when(productCmpt.findPolicyCmptType(ipsProject)).thenReturn(type2);
        when(productCmpt.getName()).thenReturn(MY_QUALIFIER);
        when(type2.findAssociation(MY_ASSOCIATION + "2", ipsProject)).thenReturn(associationIndexed);
        when(productCmptType.searchProductComponents(true)).thenReturn(Arrays.asList(sourceFile));
        when(associationIndexed.findTarget(ipsProject)).thenReturn(type3);
        when(type3.findAllAttributes(ipsProject)).thenReturn(Arrays.asList(attribute));
        when(type3.getIpsProject()).thenReturn(ipsProject);
        when(attribute.getName()).thenReturn(MY_ATTRIBUTE);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.GREGORIAN_CALENDAR);
        when(identifierFilter.isIdentifierAllowed(any(IIpsObjectPartContainer.class), any(IdentifierKind.class)))
                .thenReturn(true);
        when(ipsProject.getReadOnlyProperties()).thenReturn(projectProperties);
        when(projectProperties.getDefaultLanguage()).thenReturn(supportetLanguage);
        when(supportetLanguage.getLocale()).thenReturn(Locale.GERMAN);
    }

    @Test
    public void testParse_illegalIdentifier() throws Exception {
        IdentifierNode identifierNode = identifierParser.parse("no" + MY_IDENTIFIER);

        assertTrue(identifierNode instanceof InvalidIdentifierNode);
    }

    @Test
    public void testParse_multiParametersAssociationsAttributes() throws Exception {
        initProdCmptAndType();
        ParameterNode parameterNode = (ParameterNode)identifierParser.parse(MY_IDENTIFIER);

        assertEquals(parameter, parameterNode.getParameter());
        AssociationNode associationNode = (AssociationNode)parameterNode.getSuccessor();
        assertEquals(association, associationNode.getAssociation());
        assertTrue(associationNode.getDatatype() instanceof ListOfTypeDatatype);
        AssociationNode qualifiedAssociationNode = (AssociationNode)associationNode.getSuccessor();
        assertEquals(associationQualified, qualifiedAssociationNode.getAssociation());
        QualifierNode qualifiedNode = (QualifierNode)qualifiedAssociationNode.getSuccessor();
        assertEquals("runtimeId.abc123", qualifiedNode.getRuntimeId());
        assertTrue(qualifiedNode.getDatatype() instanceof ListOfTypeDatatype);
        AssociationNode indexedAssociationNode = (AssociationNode)qualifiedNode.getSuccessor();
        assertEquals(associationIndexed, indexedAssociationNode.getAssociation());
        IndexNode indexNode = (IndexNode)indexedAssociationNode.getSuccessor();
        assertEquals(0, indexNode.getIndex());
        assertEquals(type3, indexNode.getDatatype());
        AttributeNode attributeNode = (AttributeNode)indexNode.getSuccessor();
        assertEquals(attribute, attributeNode.getAttribute());
        assertEquals(Datatype.GREGORIAN_CALENDAR, attributeNode.getDatatype());
    }

    private void initProdCmptAndType() {
        ArrayList<IIpsSrcFile> list = new ArrayList<>();
        list.add(productCmptIpsSrcFile);

        when(ipsProject.findProductCmptByUnqualifiedName(MY_QUALIFIER)).thenReturn(list);
        when(productCmptIpsSrcFile.getIpsObject()).thenReturn(productCmpt);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(productCmptType.isSubtypeOrSameType(productCmptType, ipsProject)).thenReturn(true);
    }

    @Test
    public void testParse_ListTypeForAttributes() throws Exception {
        when(type1.findAllAttributes(ipsProject)).thenReturn(Arrays.asList(attribute));

        ParameterNode parameterNode = (ParameterNode)identifierParser.parse(MY_PARAMETER + '.' + MY_ASSOCIATION + '.'
                + MY_ATTRIBUTE);

        assertEquals(parameter, parameterNode.getParameter());
        AssociationNode associationNode = (AssociationNode)parameterNode.getSuccessor();
        assertEquals(association, associationNode.getAssociation());
        assertTrue(associationNode.getDatatype() instanceof ListOfTypeDatatype);
        AttributeNode attributeNode = (AttributeNode)associationNode.getSuccessor();
        assertEquals(attribute, attributeNode.getAttribute());
        assertEquals(new ListOfTypeDatatype(Datatype.GREGORIAN_CALENDAR), attributeNode.getDatatype());
    }

    @Test
    public void testParse_multiEnum() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)identifierParser.parse(MY_ENUMCLASS + '.' + MY_ENUMVALUE);

        assertEquals(enumDatatype, enumClassNode.getDatatype().getEnumDatatype());
        EnumValueNode enumDatatypeNode = (EnumValueNode)enumClassNode.getSuccessor();
        assertEquals(MY_ENUMVALUE, enumDatatypeNode.getEnumValueName());
        assertEquals(enumDatatype, enumDatatypeNode.getDatatype());
    }

    @Test
    public void testValidTextRegionInIdentifierNodeLongString() {
        String input = MY_PARAMETER + '.' + MY_ASSOCIATION;
        IdentifierNode node = identifierParser.parse(input);
        IdentifierNode successor = node.getSuccessor();

        assertEquals(0, node.getTextRegion().getStart());
        assertEquals(MY_PARAMETER.length(), node.getTextRegion().getEnd());

        assertEquals(MY_PARAMETER.length() + 1, successor.getTextRegion().getStart());
        assertEquals(input.length(), successor.getTextRegion().getEnd());
    }

    @Test
    public void testValidTextRegionInIdentifierNodeEmptyString() {
        IdentifierNode node = identifierParser.parse(StringUtils.EMPTY);

        assertEquals(node.getTextRegion().getStart(), node.getTextRegion().getEnd());
    }

    /**
     * Simulate the activation with dot separator. <br>
     * No proposals will be found because the dot is the last character.
     * <p>
     * More information can be found on FIPS-3514
     */
    @Test
    public void testGetProposals_hitEndSeparator() throws Exception {
        assertTrue(identifierParser.getProposals("anyParam.").isEmpty());
        assertTrue(identifierParser.getProposals("anyParam..").isEmpty());

        String input = MY_PARAMETER + '.' + MY_ASSOCIATION + "1[\".";
        List<IdentifierProposal> proposals = identifierParser.getProposals(input);
        assertTrue(String.valueOf(proposals.size()), proposals.isEmpty());
    }

    /**
     * Simulate the activation with STRG + Space. <br>
     * The correct proposal will be found.
     */
    @Test
    public void testGetProposals_STRG_SPACE() throws Exception {
        assertFalse(identifierParser.getProposals("anyParam").isEmpty());
        assertTrue(identifierParser.getProposals("anyParamm").isEmpty());

        String input = MY_PARAMETER + '.' + MY_ASSOCIATION + "1[\"";
        List<IdentifierProposal> proposals = identifierParser.getProposals(input);
        assertEquals(1, proposals.size());
        assertEquals("\"" + MY_QUALIFIER + "\"]", proposals.get(0).getText());
    }
}
