/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.enums;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class EnumAttributeAnnGenTest {


    @Mock
    private XEnumAttribute attribute;

    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = createMocks(this);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testCreateAnnotation() throws Exception {
        when(attribute.getName()).thenReturn("foo");

        JavaCodeFragment annotation = new EnumAttributeAnnGen().createAnnotation(attribute);

        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsEnumAttribute(name = \"foo\")" + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_id() throws Exception {
        when(attribute.getName()).thenReturn("bar");
        when(attribute.isIdentifier()).thenReturn(true);

        JavaCodeFragment annotation = new EnumAttributeAnnGen().createAnnotation(attribute);

        assertThat(annotation.getSourcecode(), is(equalTo("@IpsEnumAttribute(name = \"bar\", identifier = true)"
                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_unique() throws Exception {
        when(attribute.getName()).thenReturn("baz");
        when(attribute.isUnique()).thenReturn(true);

        JavaCodeFragment annotation = new EnumAttributeAnnGen().createAnnotation(attribute);

        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsEnumAttribute(name = \"baz\", unique = true)" + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_displayName() throws Exception {
        when(attribute.getName()).thenReturn("bat");
        when(attribute.isDisplayName()).thenReturn(true);

        JavaCodeFragment annotation = new EnumAttributeAnnGen().createAnnotation(attribute);

        assertThat(annotation.getSourcecode(), is(equalTo("@IpsEnumAttribute(name = \"bat\", displayName = true)"
                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_mandatory() throws Exception {
        when(attribute.getName()).thenReturn("baz");
        when(attribute.isMandatory()).thenReturn(true);

        JavaCodeFragment annotation = new EnumAttributeAnnGen().createAnnotation(attribute);

        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsEnumAttribute(name = \"baz\", mandatory = true)" + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_full() throws Exception {
        when(attribute.getName()).thenReturn("foobar");
        when(attribute.isIdentifier()).thenReturn(true);
        when(attribute.isUnique()).thenReturn(true);
        when(attribute.isDisplayName()).thenReturn(true);
        when(attribute.isMandatory()).thenReturn(true);

        JavaCodeFragment annotation = new EnumAttributeAnnGen().createAnnotation(attribute);

        assertThat(annotation.getSourcecode(),
                is(equalTo(
                        "@IpsEnumAttribute(name = \"foobar\", identifier = true, unique = true, displayName = true, mandatory = true)"
                                + System.lineSeparator())));
    }

}
