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
import static org.mockito.Mockito.when;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
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

    private EnumParser enumParser;

    @Before
    public void createEnumParser() throws Exception {
        enumParser = new EnumParser(getExpression(), getIpsProject());
    }

    @Before
    public void mockEnumDatatype() {
        when(getExpression().getEnumDatatypesAllowedInFormula()).thenReturn(new EnumDatatype[] { enumDatatype });
        when(enumDatatype.getName()).thenReturn(MY_ENUM_CLASS);
        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { MY_ENUM_VALUE });
    }

    @Test
    public void testParse_parseEnumClass() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)enumParser.parse(MY_ENUM_CLASS, null, null);

        assertEquals(new EnumClassNode.EnumClass(enumDatatype), enumClassNode.getDatatype());
    }

    @Test
    public void testParse_parseEnumClassNotFound() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)enumParser.parse(ANY_ENUM_CLASS, null, null);

        assertNull(enumClassNode);
    }

    @Test
    public void testParse_parseEnumDatatype() throws Exception {
        EnumClassNode enumClassNode = new IdentifierNodeFactory("", getIpsProject(), null)
                .createEnumClassNode(new EnumClassNode.EnumClass(enumDatatype));

        EnumValueNode enumDatatypeNode = (EnumValueNode)enumParser.parse(MY_ENUM_VALUE, enumClassNode, null);

        assertEquals(enumDatatype, enumDatatypeNode.getDatatype());
    }

    @Test
    public void testParse_parseEnumDatatypeNotFount() throws Exception {
        EnumClassNode enumClassNode = new IdentifierNodeFactory("", getIpsProject(), null)
                .createEnumClassNode(new EnumClassNode.EnumClass(enumDatatype));

        InvalidIdentifierNode node = (InvalidIdentifierNode)enumParser.parse(ANY_ENUM_VALUE, enumClassNode, null);

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

}
