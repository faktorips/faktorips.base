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
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumParserTest extends AbstractParserTest {

    private static final String MY_LABEL = "myLabel";

    private static final String MY_ENUM_CLASS = "myEnumClass";
    private static final String ANY_ENUM_CLASS = "anyEnumClass";

    private static final String MY_ENUM_VALUE = "myEnumValue";
    private static final String ANY_ENUM_VALUE = "anyEnumValue";

    private static final Object NAME = "myEnumName";

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private EnumClass enumClass;

    @Mock
    private EnumTypeDatatypeAdapter enumDatatypeAdapter;

    @Mock
    private IEnumType enumtype;

    private EnumParser enumParser;

    @Before
    public void createEnumParser() throws Exception {
        when(getExpression().getEnumDatatypesAllowedInFormula()).thenReturn(
                new EnumDatatype[] { enumDatatype, enumDatatypeAdapter });
        when(enumDatatype.getName()).thenReturn(MY_ENUM_CLASS);
        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { MY_ENUM_VALUE });
        enumParser = new EnumParser(getParsingContext());
    }

    @Test
    public void testParse_parseEnumClass() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)enumParser.parse(new TextRegion(MY_ENUM_CLASS, 0, MY_ENUM_CLASS
                .length()));

        assertEquals(new EnumClassNode.EnumClass(enumDatatype), enumClassNode.getDatatype());
    }

    @Test
    public void testParse_parseEnumClassNotFound() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)enumParser.parse(new TextRegion(ANY_ENUM_CLASS, 0, ANY_ENUM_CLASS
                .length()));

        assertNull(enumClassNode);
    }

    @Test
    public void testParse_parseEnumDatatype() throws Exception {
        EnumClassNode enumClassNode = new IdentifierNodeFactory(null, getIpsProject())
                .createEnumClassNode(new EnumClassNode.EnumClass(enumDatatype));
        getParsingContext().pushNode(enumClassNode);

        EnumValueNode enumDatatypeNode = (EnumValueNode)enumParser.parse(new TextRegion(MY_ENUM_VALUE, 0, MY_ENUM_VALUE
                .length()));

        assertEquals(enumDatatype, enumDatatypeNode.getDatatype());
    }

    @Test
    public void testParse_parseEnumDatatypeNotFount() throws Exception {
        EnumClassNode enumClassNode = new IdentifierNodeFactory(null, getIpsProject())
                .createEnumClassNode(new EnumClassNode.EnumClass(enumDatatype));
        getParsingContext().pushNode(enumClassNode);

        InvalidIdentifierNode node = (InvalidIdentifierNode)enumParser.parse(new TextRegion(ANY_ENUM_VALUE, 0,
                ANY_ENUM_VALUE.length()));

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

    @Test
    public void testGetProposalEnumType() {
        doReturn(enumtype).when(enumDatatypeAdapter).getEnumType();
        doReturn("myEnum").when(enumtype).getName();

        List<IdentifierProposal> proposals = enumParser.getProposals("my");

        assertEquals(1, proposals.size());

    }

    @Test
    public void testGetProposalEnumValue() {
        doReturn(new String[] { "notMyEnumValue", "myEnumValue" }).when(enumDatatype).getAllValueIds(false);
        enumParser.setContextType(enumClass);
        when(enumClass.getEnumDatatype()).thenReturn(enumDatatype);
        when(enumDatatype.getValueName(eq(MY_ENUM_VALUE), any(Locale.class))).thenReturn(MY_LABEL);

        List<IdentifierProposal> proposals = enumParser.getProposals("my");

        assertEquals("myEnumValue", proposals.get(0).getText());
        assertEquals("myEnumValue(" + MY_LABEL + ")", proposals.get(0).getLabel());
        assertEquals(1, proposals.size());
    }

    @Test
    public void testGetDescription_NoneEnumTypes() {
        doReturn(NAME).when(enumDatatype).getName();

        String description = enumParser.getDescription(enumDatatype);

        assertEquals(NLS.bind(Messages.EnumParser_description, NAME), description);
    }

    @Test
    public void testGetDescription_forEnumTypes() {
        doReturn(enumtype).when(enumDatatypeAdapter).getEnumType();
        doReturn(MY_LABEL).when(enumtype).getName();

        String description = enumParser.getDescription(enumDatatypeAdapter);

        assertEquals(MY_LABEL, description);
    }

}
