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

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
    }

    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(IpsPlugin.getDefault().getIpsModel().getIpsProject(
                "CZ2_OMCWTG1_D~0~mc(2fbonus(2fkern(2fjava~as.de"), entry.getReferencedIpsProject());
        assertTrue(entry.isUseNWDITrackPrefix());
    }

    public void testToXml() {
        IIpsProject refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        Element element = entry.toXml(newDocument());

        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
    }
}
