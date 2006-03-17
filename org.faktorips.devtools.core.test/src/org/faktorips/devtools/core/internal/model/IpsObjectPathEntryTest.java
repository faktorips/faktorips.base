/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathEntryTest extends IpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath();
    }

    public void testCreateFromXml() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(0), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_SRC_FOLDER, entry.getType());
        entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(1), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE, entry.getType());
    }

}
