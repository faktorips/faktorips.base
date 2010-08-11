/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LabeledElementHelperTest extends AbstractIpsPluginTest {

    private MockHelper helper;

    private ILabeledElement labeledObjectPart;

    private ILabel usLabel;

    private ILabel germanLabel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.US);
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.setDefaultLanguage(properties.getSupportedLanguage(Locale.GERMAN));
        ipsProject.setProperties(properties);

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        labeledObjectPart = policyCmptType.newAttribute();
        helper = new MockHelper((IpsObjectPartContainer)labeledObjectPart);

        usLabel = helper.newLabel();
        usLabel.setLocale(Locale.US);
        germanLabel = helper.newLabel();
        germanLabel.setLocale(Locale.GERMAN);
    }

    public void testGetChildren() {
        IIpsElement[] children = helper.getChildren();
        assertEquals(2, children.length);

        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(usLabel));
        assertTrue(childrenList.contains(germanLabel));
    }

    public void testReinitPartCollections() {
        assertEquals(2, helper.getLabels().size());
        helper.reinitPartCollections();
        assertEquals(0, helper.getLabels().size());
    }

    public void testAddRemovePart() {
        ILabel labelToAdd = helper.newLabel();
        assertEquals(3, helper.getLabels().size());
        helper.removePart(labelToAdd);
        assertEquals(2, helper.getLabels().size());
        helper.addPart(labelToAdd);
        assertEquals(3, helper.getLabels().size());
    }

    public void testNewPart() throws DOMException, ParserConfigurationException {
        Document xmlDoc = createXmlDocument("Blub");
        Element element = xmlDoc.createElement(ILabel.XML_TAG_NAME);
        assertTrue(helper.newPart(element, "blub") instanceof ILabel);
        assertEquals(3, helper.getLabels().size());

        element = xmlDoc.createElement("foobar");
        assertNull(helper.newPart(element, "xyz"));
    }

    public void testNewLabel() {
        assertEquals(2, helper.getLabels().size());
        assertNotNull(helper.newLabel());
        assertEquals(3, helper.getLabels().size());
    }

    public void testGetLabel() {
        assertEquals(usLabel, helper.getLabel(Locale.US));
        assertEquals(germanLabel, helper.getLabel(Locale.GERMAN));
        assertNull(helper.getLabel(Locale.KOREAN));
    }

    public void testGetLabels() {
        Set<ILabel> labelSet = helper.getLabels();
        assertEquals(2, labelSet.size());
        assertTrue(labelSet.contains(usLabel));
        assertTrue(labelSet.contains(germanLabel));
        try {
            labelSet.remove(germanLabel);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testGetLabelForCurrentLocale() {
        Locale currentLocale = IpsPlugin.getDefault().getIpsModelLocale();
        assertEquals(currentLocale.getLanguage(), helper.getLabelForCurrentLocale().getLocale().getLanguage());
    }

    public void testGetLabelForDefaultLocale() {
        Locale defaultLocale = Locale.GERMAN;
        assertEquals(defaultLocale, helper.getLabelForDefaultLocale().getLocale());
    }

    // This class is necessary to be able to access protected methods.
    private static class MockHelper extends LabeledElementHelper {

        public MockHelper(IpsObjectPartContainer ipsObjectPartContainer) {
            super(ipsObjectPartContainer);
        }

        @Override
        public void removePart(IIpsObjectPart part) {
            super.removePart(part);
        }

        @Override
        public void reinitPartCollections() {
            super.reinitPartCollections();
        }

        @Override
        protected IIpsObjectPart newPart(Element xmlTag, String id) {
            return super.newPart(xmlTag, id);
        }

        @Override
        protected void addPart(IIpsObjectPart part) {
            super.addPart(part);
        }

    }

}
