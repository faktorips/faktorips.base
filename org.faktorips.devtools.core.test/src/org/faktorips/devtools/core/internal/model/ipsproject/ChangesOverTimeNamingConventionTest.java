/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
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
