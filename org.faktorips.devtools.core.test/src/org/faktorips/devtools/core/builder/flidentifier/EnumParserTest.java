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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.util.TextRegion;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumParserTest extends AbstractParserTest {

    private static final String MY_ENUM_CLASS = "myEnumClass";
    private static final String ANY_ENUM_CLASS = "anyEnumClass";

    private static final String MY_ENUM_VALUE = "myEnumValue";
    private static final String ANY_ENUM_VALUE = "anyEnumValue";

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

        List<IdentifierNode> proposals = enumParser.getProposals("my");

        assertEquals(1, proposals.size());

    }

    @Test
    public void testGetProposalEnumValue() {
        doReturn(new String[] { "notMyEnumValue", "myEnumValue" }).when(enumDatatype).getAllValueIds(false);
        doReturn(new String[] {}).when(enumDatatypeAdapter).getAllValueIds(false);
        enumParser.setContextType(enumClass);

        List<IdentifierNode> proposals = enumParser.getProposals("my");

        assertEquals("myEnumValue", proposals.get(0).getText());
        assertEquals(1, proposals.size());
    }
}
