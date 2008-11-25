/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.bf.BusinessFunction;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParameterBFETest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private TestContentsChangeListener listener;
    private BusinessFunction bf;

    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        listener = new TestContentsChangeListener();
        ipsProject.getIpsModel().addChangeListener(listener);
        bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    public void testInitPropertiesFromXml() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setDatatype(Datatype.INTEGER.getName());
        Document doc = getDocumentBuilder().newDocument();
        Element el = p.toXml(doc);

        IParameterBFE p2 = bf.newParameter();
        p2.initFromXml(el);
        assertEquals(Datatype.INTEGER.getName(), p2.getDatatype());
    }

    public void testPropertiesToXml() {
        Document doc = getTestDocument();
        IParameterBFE p = bf.newParameter();
        p.initFromXml((Element)doc.getDocumentElement().getElementsByTagName(IParameterBFE.XML_TAG).item(0));
        assertEquals("String", p.getDatatype());
    }

    public void testGetDisplayString() {
        IParameterBFE p = bf.newParameter();
        p.setDatatype("String");
        p.setName("p1");
        assertEquals("String:p1", p.getDisplayString());
    }

    public void testSetDatatype() {
        IParameterBFE p = bf.newParameter();
        listener.clear();
        p.setDatatype("String");
        assertEquals("String", p.getDatatype());
        assertTrue(listener.getIpsObjectParts().contains(p));

        p.setDatatype("");
        assertEquals("", p.getDatatype());

        try {
            p.setDatatype(null);
            fail();
        } catch (NullPointerException e) {

        }
    }

    public void testFindDatatype() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setDatatype(Datatype.STRING.getName());
        Datatype d = p.findDatatype();
        assertEquals(Datatype.STRING, d);

        p.setDatatype("");
        d = p.findDatatype();
        assertNull(d);

    }

}
