/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.faktorips.devtools.model.builder.xmodel.XAssociation;
import org.faktorips.devtools.model.builder.xmodel.XAttribute;
import org.faktorips.devtools.model.builder.xmodel.XType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This class is merely a String test. Functionality has to be additionally tested in integration
 * tests.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractTypeDeclClassAnnGenTest {

    private static final String ATTRIBUTE1 = "attribute1";
    private static final String ATTRIBUTE2 = "attribute2";
    private static final String ASSOCIATION1 = "association1";
    private static final String ASSOCIATION2 = "association2";

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractTypeDeclClassAnnGen annGen;

    // expected outcomes
    private String annAttribute_single = "@IpsAttributes({\"" + ATTRIBUTE1 + "\"})"
            + System.lineSeparator();
    private String annAttribute_mult = "@IpsAttributes({\"" + ATTRIBUTE1 + "\", \"" + ATTRIBUTE2 + "\"})"
            + System.lineSeparator();
    private String annAssociation_single = "@IpsAssociations({\"" + ASSOCIATION1 + "\"})"
            + System.lineSeparator();
    private String annAssociation_mult = "@IpsAssociations({\"" + ASSOCIATION1 + "\", \"" + ASSOCIATION2 + "\"})"
            + System.lineSeparator();

    @Test
    public void testCreateAnnAttributesNone() {
        XType type = mock(XType.class);
        assertEquals("", annGen.createAnnAttributes(type).getSourcecode());
    }

    @Test
    public void testCreateAnnAttributesSingle() {
        XType type = mock(XType.class);
        XAttribute attribute = mock(XAttribute.class);
        when(attribute.getName()).thenReturn(ATTRIBUTE1);
        doReturn(new LinkedHashSet<>(Arrays.asList(attribute))).when(type).getAllDeclaredAttributes();

        assertEquals(annAttribute_single, annGen.createAnnAttributes(type).getSourcecode());
    }

    @Test
    public void testCreateAnnAttributesMult() {
        XType type = mock(XType.class);
        XAttribute attribute1 = mock(XAttribute.class);
        when(attribute1.getName()).thenReturn(ATTRIBUTE1);
        XAttribute attribute2 = mock(XAttribute.class);
        when(attribute2.getName()).thenReturn(ATTRIBUTE2);
        doReturn(new LinkedHashSet<>(Arrays.asList(attribute1, attribute2))).when(type)
                .getAllDeclaredAttributes();

        assertEquals(annAttribute_mult, annGen.createAnnAttributes(type).getSourcecode());
    }

    @Test
    public void testCreateAnnAssociationsNone() {
        XType type = mock(XType.class);
        assertEquals("", annGen.createAnnAssociations(type).getSourcecode());
    }

    @Test
    public void testCreateAnnAssociationsSingle() {
        XType type = mock(XType.class);
        XAssociation association = mock(XAssociation.class);
        when(association.getName()).thenReturn(ASSOCIATION1);
        doReturn(new LinkedHashSet<>(Arrays.asList(association))).when(type).getAllDeclaredAssociations();

        assertEquals(annAssociation_single, annGen.createAnnAssociations(type).getSourcecode());
    }

    @Test
    public void testCreateAnnAssociationsMult() {
        XType type = mock(XType.class);
        XAssociation association1 = mock(XAssociation.class);
        when(association1.getName()).thenReturn(ASSOCIATION1);
        XAssociation association2 = mock(XAssociation.class);
        when(association2.getName()).thenReturn(ASSOCIATION2);
        doReturn(new LinkedHashSet<>(Arrays.asList(association1, association2))).when(type)
                .getAllDeclaredAssociations();

        assertEquals(annAssociation_mult, annGen.createAnnAssociations(type).getSourcecode());
    }
}
