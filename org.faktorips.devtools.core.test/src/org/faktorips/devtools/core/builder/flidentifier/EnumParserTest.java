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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
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
    private EnumTypeDatatypeAdapter enumDatatypeAdapter;

    @Mock
    private IEnumType enumtype;

    @Mock
    private IEnumValue enumValue1;

    @Mock
    private IEnumValue enumValue2;

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
        EnumClassNode enumClassNode = (EnumClassNode)enumParser.parse(MY_ENUM_CLASS, null);

        assertEquals(new EnumClassNode.EnumClass(enumDatatype), enumClassNode.getDatatype());
    }

    @Test
    public void testParse_parseEnumClassNotFound() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)enumParser.parse(ANY_ENUM_CLASS, null);

        assertNull(enumClassNode);
    }

    @Test
    public void testParse_parseEnumDatatype() throws Exception {
        EnumClassNode enumClassNode = new IdentifierNodeFactory("", null, getIpsProject())
                .createEnumClassNode(new EnumClassNode.EnumClass(enumDatatype));
        getParsingContext().pushNode(enumClassNode);

        EnumValueNode enumDatatypeNode = (EnumValueNode)enumParser.parse(MY_ENUM_VALUE, null);

        assertEquals(enumDatatype, enumDatatypeNode.getDatatype());
    }

    @Test
    public void testParse_parseEnumDatatypeNotFount() throws Exception {
        EnumClassNode enumClassNode = new IdentifierNodeFactory("", null, getIpsProject())
                .createEnumClassNode(new EnumClassNode.EnumClass(enumDatatype));
        getParsingContext().pushNode(enumClassNode);

        InvalidIdentifierNode node = (InvalidIdentifierNode)enumParser.parse(ANY_ENUM_VALUE, null);

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
        List<IEnumValue> enumValues = new ArrayList<IEnumValue>();
        enumValues.add(enumValue1);
        enumValues.add(enumValue2);

        doReturn(enumtype).when(enumDatatypeAdapter).getEnumType();
        doReturn("otherName").when(enumtype).getName();
        doReturn("notMyEnumValue").when(enumValue1).getName();
        doReturn("myEnumValue").when(enumValue2).getName();

        doReturn(enumValues).when(enumtype).getEnumValues();

        List<IdentifierNode> proposals = enumParser.getProposals("my");

        assertEquals(enumValue2.getName(), proposals.get(0).getText());
        assertEquals(1, proposals.size());
    }
}
