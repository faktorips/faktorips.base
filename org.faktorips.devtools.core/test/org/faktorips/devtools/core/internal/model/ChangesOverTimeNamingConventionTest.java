package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import junit.framework.TestCase;

import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;

/**
 * 
 * @author Jan Ortmann
 */
public class ChangesOverTimeNamingConventionTest extends TestCase {

	private ChangesOverTimeNamingConvention vaa;
	private ChangesOverTimeNamingConvention pm;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		vaa = new ChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.VAA);
		pm = new ChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.PM);
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.ChangesInTimeNamingConvention.getName()'
	 */
	public void testGetName() {
		assertEquals("VAA", vaa.getName(Locale.ENGLISH));
		assertEquals("VAA", vaa.getName(Locale.GERMAN));
		assertEquals("Product-Manager", pm.getName(Locale.ENGLISH));
		assertEquals("Produkt-Manager", pm.getName(Locale.GERMAN));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.ChangesInTimeNamingConvention.getGenerationConceptNameSingular(Locale)'
	 */
	public void testGetGenerationConceptNameSingular() {
		assertNotNull(vaa.getGenerationConceptNameSingular(Locale.ENGLISH));
		assertNotNull(vaa.getGenerationConceptNameSingular(Locale.GERMAN));
		assertNotNull(pm.getGenerationConceptNameSingular(Locale.ENGLISH));
		assertNotNull(pm.getGenerationConceptNameSingular(Locale.GERMAN));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.ChangesInTimeNamingConvention.getGenerationConceptNamePlural(Locale)'
	 */
	public void testGetGenerationConceptNamePlural() {
		assertNotNull(vaa.getGenerationConceptNamePlural(Locale.ENGLISH));
		assertNotNull(vaa.getGenerationConceptNamePlural(Locale.GERMAN));
		assertNotNull(pm.getGenerationConceptNamePlural(Locale.ENGLISH));
		assertNotNull(pm.getGenerationConceptNamePlural(Locale.GERMAN));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.ChangesInTimeNamingConvention.getGenerationConceptNameAbbreviation(Locale)'
	 */
	public void testGetGenerationConceptNameAbbreviation() {
		assertNotNull(vaa.getGenerationConceptNameAbbreviation(Locale.ENGLISH));
		assertNotNull(vaa.getGenerationConceptNameAbbreviation(Locale.GERMAN));
		assertNotNull(pm.getGenerationConceptNameAbbreviation(Locale.ENGLISH));
		assertNotNull(pm.getGenerationConceptNameAbbreviation(Locale.GERMAN));
	}

	public void testGetEffectiveDateConceptName() {
		assertNotNull(vaa.getEffectiveDateConceptName(Locale.ENGLISH));
		assertNotNull(vaa.getEffectiveDateConceptName(Locale.GERMAN));
		assertNotNull(pm.getEffectiveDateConceptName(Locale.ENGLISH));
		assertNotNull(pm.getEffectiveDateConceptName(Locale.GERMAN));
	}
}
