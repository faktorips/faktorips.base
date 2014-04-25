/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.ast;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.internal.model.EnumDatatypePaymentMode;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.TextRegion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumClassNodeTest {

    @InjectMocks
    private EnumClassNode enumClassNode;

    @Mock
    private EnumClass enumClass;

    @Mock
    private EnumTypeDatatypeAdapter enumDatatype;

    @Mock
    private EnumDatatypePaymentMode enumDatatypePaymentMode;

    @Mock
    private MultiLanguageSupport multiLanguageSupport;

    @Mock
    private IEnumType enumType;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private TextRegion textregion;

    private static final String MY_LABEL = "myLabel";

    @Test
    public void testGetDescription_NoIpsElement() {
        doReturn(MY_LABEL).when(enumClass).getName();
        doReturn(enumDatatypePaymentMode).when(enumClass).getEnumDatatype();
        enumClassNode = new EnumClassNode(enumClass, textregion);

        String description = enumClassNode.getDescription(multiLanguageSupport);

        assertEquals(NLS.bind(Messages.EnumClassNode_description, MY_LABEL), description);
    }

    @Test
    public void testGetDescription_forTypes() {
        doReturn(enumType).when(enumDatatype).getEnumType();
        doReturn(MY_LABEL).when(enumType).getName();
        doReturn(enumDatatype).when(enumClass).getEnumDatatype();
        enumClassNode = new EnumClassNode(enumClass, textregion);

        String description = enumClassNode.getDescription(multiLanguageSupport);

        assertEquals(MY_LABEL, description);
    }
}
