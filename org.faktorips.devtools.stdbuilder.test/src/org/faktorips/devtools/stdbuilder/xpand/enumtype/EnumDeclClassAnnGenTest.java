/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.enumtype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumAttribute;
import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumType;
import org.junit.Test;

public class EnumDeclClassAnnGenTest {

    private final EnumDeclClassAnnGen enumDeclClassAnnGen = new EnumDeclClassAnnGen();

    @Test
    public void testCreateAnnotation() {
        XEnumType enumtype = mockEnumtype();

        assertThat(
                enumDeclClassAnnGen.createAnnotation(enumtype).getSourcecode(),
                is(equalTo("@IpsEnum(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.getProperty("line.separator"))));
    }

    @Test
    public void testCreateAnnotationExtensible() {
        XEnumType enumtype = mockEnumtype();
        when(enumtype.isExtensible()).thenReturn(true);
        when(enumtype.getEnumContentQualifiedName()).thenReturn("foo.EnumName");

        assertThat(
                enumDeclClassAnnGen.createAnnotation(enumtype).getSourcecode(),
                is(equalTo("@IpsEnum(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.getProperty("line.separator") + "@IpsExtensibleEnum(enumContentName=\"foo.EnumName\")"
                        + System.getProperty("line.separator"))));
    }

    private XEnumType mockEnumtype() {
        XEnumType enumtype = mock(XEnumType.class);
        when(enumtype.getQualifiedIpsObjectName()).thenReturn("test.EnumType");
        XEnumAttribute attribute1 = mock(XEnumAttribute.class);
        when(attribute1.getName()).thenReturn("A1");
        XEnumAttribute attribute2 = mock(XEnumAttribute.class);
        when(attribute2.getName()).thenReturn("A2");
        XEnumAttribute attribute3 = mock(XEnumAttribute.class);
        when(attribute3.getName()).thenReturn("A3");
        List<XEnumAttribute> attributes = Arrays.asList(attribute2, attribute1, attribute3);
        when(enumtype.getAllAttributes()).thenReturn(attributes);
        return enumtype;
    }

}