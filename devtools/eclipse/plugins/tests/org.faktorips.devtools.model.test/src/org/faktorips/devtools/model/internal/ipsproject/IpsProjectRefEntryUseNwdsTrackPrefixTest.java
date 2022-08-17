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
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class IpsProjectRefEntryUseNwdsTrackPrefixTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(
                IIpsModel.get().getIpsProject("CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de"),
                entry.getReferencedIpsProject());
        assertTrue(entry.isUseNWDITrackPrefix());
    }

    @Test
    public void testToXml() {
        IIpsProject refProject = IIpsModel.get().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        Element element = entry.toXml(newDocument());

        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
    }
}
