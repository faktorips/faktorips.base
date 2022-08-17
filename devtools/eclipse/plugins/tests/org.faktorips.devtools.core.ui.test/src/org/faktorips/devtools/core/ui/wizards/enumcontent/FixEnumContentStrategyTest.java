/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FixEnumContentStrategyTest extends AbstractIpsEnumPluginTest {
    @Mock
    private AssignContentAttributesPage<IEnumType, IEnumAttribute> assignEnumAttributesPage;

    private FixEnumContentStrategy enumStrategy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        enumStrategy = new FixEnumContentStrategy(genderEnumContent);
    }

    @Test
    public void testDeleteObsoleteContentAttributeValues() {
        Integer[] notAssignedColumnsList = { 1 };
        when(assignEnumAttributesPage.getCurrentlyNotAssignedColumns())
                .thenReturn(Arrays.asList(notAssignedColumnsList));
        enumStrategy.deleteObsoleteContentAttributeValues(assignEnumAttributesPage);
        // first EnumAttribute genderEnumAttributeId gets deleted (m | w). New first EnumAttribute
        // is now genderEnumAttributeName(male | female)
        assertEquals("male", genderEnumContent.getEnumValues().get(0).getEnumAttributeValues().get(0).getStringValue());
        assertEquals("female",
                genderEnumContent.getEnumValues().get(1).getEnumAttributeValues().get(0).getStringValue());
    }

    @Test
    public void testCreateNewContentAttributeValues() {
        IEnumAttribute newEnumAttribute;
        int[] notAssignedColumnsList = { 1, 2, 0 };
        when(assignEnumAttributesPage.getColumnOrder()).thenReturn(notAssignedColumnsList);

        newEnumAttribute = genderEnumType.newEnumAttribute();
        newEnumAttribute.setName("Deutsch");
        newEnumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        enumStrategy.createNewContentAttributeValues(assignEnumAttributesPage);
        assertNotNull(genderEnumContent.getEnumValues().get(0).getEnumAttributeValues().get(2));
        assertNotNull(genderEnumContent.getEnumValues().get(1).getEnumAttributeValues().get(2));
    }

    @Test
    public void testMoveAttributeValues() {
        enumStrategy.moveAttributeValues(new int[] { 2, 1 });
        assertEquals("male", genderEnumContent.getEnumValues().get(0).getEnumAttributeValues().get(0).getStringValue());
        assertEquals("m", genderEnumContent.getEnumValues().get(0).getEnumAttributeValues().get(1).getStringValue());
        assertEquals("female",
                genderEnumContent.getEnumValues().get(1).getEnumAttributeValues().get(0).getStringValue());
        assertEquals("w", genderEnumContent.getEnumValues().get(1).getEnumAttributeValues().get(1).getStringValue());
    }
}
