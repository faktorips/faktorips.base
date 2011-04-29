/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * @author Joerg Ortmann
 */
public class IpsProjectRefEntryJunitTest {

    @Test
    public void testConvertProjNameUsingSapConvention() {
        // no sap format
        assertEquals("x", IpsProjectRefEntry.createNWDIProjectName("x", "y"));

        // no sap format current project name
        assertEquals("CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de",
                IpsProjectRefEntry.createNWDIProjectName("CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "y"));

        // no sap format referenced project name, use name as relative name
        assertEquals("CZ2_OMCWTG101_D~11~x",
                IpsProjectRefEntry.createNWDIProjectName("x", "CZ2_OMCWTG101_D~11~mc(2bonus(2fdomain(2fjava~as.de"));

        // different instance
        assertEquals("CZ2_OMCWTG1_D~1~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "CZ2_OMCWTG1_D~1~mc(2bonus(2fdomain(2fjava~as.de"));

        // different track
        assertEquals("CZ2_OMCWTG2_D~0~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "CZ2_OMCWTG2_D~0~mc(2bonus(2fdomain(2fjava~as.de"));

        // different track and instance
        assertEquals("CZ2_OMCWTG101_D~11~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "CZ2_OMCWTG101_D~11~mc(2bonus(2fdomain(2fjava~as.de"));
    }
}
