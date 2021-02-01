/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ChangesOverTimeNamingConventionTest {

    private ChangesOverTimeNamingConvention vaa;
    private ChangesOverTimeNamingConvention pm;

    @Before
    public void setUp() throws Exception {
        vaa = new ChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.VAA);
        pm = new ChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.PM);
    }

    @Test
    public void testGetName() {
        assertEquals("VAA", vaa.getName(Locale.ENGLISH));
        assertEquals("VAA", vaa.getName(Locale.GERMAN));
        assertEquals("Product-Manager", pm.getName(Locale.ENGLISH));
        assertEquals("Produkt-Manager", pm.getName(Locale.GERMAN));
    }

    @Test
    public void testGetGenerationConceptNameSingular() {
        assertNotNull(vaa.getGenerationConceptNameSingular(Locale.ENGLISH));
        assertNotNull(vaa.getGenerationConceptNameSingular(Locale.GERMAN));
        assertNotNull(pm.getGenerationConceptNameSingular(Locale.ENGLISH));
        assertNotNull(pm.getGenerationConceptNameSingular(Locale.GERMAN));
    }

    @Test
    public void testGetGenerationConceptNamePlural() {
        assertNotNull(vaa.getGenerationConceptNamePlural(Locale.ENGLISH));
        assertNotNull(vaa.getGenerationConceptNamePlural(Locale.GERMAN));
        assertNotNull(pm.getGenerationConceptNamePlural(Locale.ENGLISH));
        assertNotNull(pm.getGenerationConceptNamePlural(Locale.GERMAN));
    }

    @Test
    public void testGetGenerationConceptNameInsideSentence() {
        assertNotNull(vaa.getGenerationConceptNamePlural(true));
        assertNotNull(vaa.getGenerationConceptNamePlural(true));
        assertNotNull(pm.getGenerationConceptNamePlural(true));
        assertNotNull(pm.getGenerationConceptNamePlural(true));

        assertNotNull(vaa.getGenerationConceptNamePlural(false));
        assertNotNull(vaa.getGenerationConceptNamePlural(false));
        assertNotNull(pm.getGenerationConceptNamePlural(false));
        assertNotNull(pm.getGenerationConceptNamePlural(false));
    }

    @Test
    public void testGetGenerationConceptNameAbbreviation() {
        assertNotNull(vaa.getGenerationConceptNameAbbreviation(Locale.ENGLISH));
        assertNotNull(vaa.getGenerationConceptNameAbbreviation(Locale.GERMAN));
        assertNotNull(pm.getGenerationConceptNameAbbreviation(Locale.ENGLISH));
        assertNotNull(pm.getGenerationConceptNameAbbreviation(Locale.GERMAN));
    }

    @Test
    public void testGetEffectiveDateConceptName() {
        assertNotNull(vaa.getEffectiveDateConceptName(Locale.ENGLISH));
        assertNotNull(vaa.getEffectiveDateConceptName(Locale.GERMAN));
        assertNotNull(pm.getEffectiveDateConceptName(Locale.ENGLISH));
        assertNotNull(pm.getEffectiveDateConceptName(Locale.GERMAN));
    }
}
