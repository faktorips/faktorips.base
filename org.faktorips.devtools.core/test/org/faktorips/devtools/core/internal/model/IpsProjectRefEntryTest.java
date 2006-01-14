package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntryTest extends PluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath();
    }

    
    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject"), entry.getReferencedIpsProject());
    }

    public void testToXml() {
        IIpsProject refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        Element element = entry.toXml(newDocument());
        
        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
    }

}
