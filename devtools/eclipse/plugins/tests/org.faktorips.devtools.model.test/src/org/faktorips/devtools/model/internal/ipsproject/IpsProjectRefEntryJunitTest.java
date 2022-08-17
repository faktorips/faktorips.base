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
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 * @author Joerg Ortmann
 */
public class IpsProjectRefEntryJunitTest {

    @Test
    public void testConvertProjNameUsingSapConventionNW72() {
        // no sap format current project name and referenced project name
        assertEquals("x", IpsProjectRefEntry.createNWDIProjectName("x", "y"));

        // no sap format current project name
        assertEquals("CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "y"));

        // no sap format referenced project name, use name as relative name
        assertEquals("CZ2_OMCWTG101_D~11~x", IpsProjectRefEntry.createNWDIProjectName("x",
                "CZ2_OMCWTG101_D~11~mc(2bonus(2fdomain(2fjava~as.de"));

        // different instance
        assertEquals("CZ2_OMCWTG1_D~1~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "CZ2_OMCWTG1_D~1~mc(2bonus(2fdomain(2fjava~as.de"));

        // different track
        assertEquals("CZ2_OMCWTG2_D~0~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de", "CZ2_OMCWTG2_D~0~mc(2bonus(2fdomain(2fjava~as.de"));

        // different track and instance
        assertEquals("CZ2_OMCWTG101_D~11~mc(2fbonus(2fkern(2fjava~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de",
                "CZ2_OMCWTG101_D~11~mc(2bonus(2fdomain(2fjava~as.de"));
    }

    @Test
    public void testConvertProjNameUsingSapConventionNW73() {
        // different track
        assertEquals("CZ2_OMCWTG1_D~mc~bonus~kern~java~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG2_D~mc~bonus~kern~java~as.de", "CZ2_OMCWTG1_D~mc~bonus~kern~java~as.de"));

        // negative test
        assertFalse("CZ2_OMCWTG1_D~mc~bonus~kern~java~as.de".equals(IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG2_D~mc~x~kern~java~as.de", "CZ2_OMCWTG1_D~mc~bonus~kern~java~as.de")));
        assertEquals("CZ2_OMCWTG1_D~mc~x~kern~java~as.de", IpsProjectRefEntry.createNWDIProjectName(
                "CZ2_OMCWTG2_D~mc~x~kern~java~as.de", "CZ2_OMCWTG1_D~mc~bonus~kern~java~as.de"));
    }
}
