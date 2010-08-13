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
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DescribedAndLabeledHelperTest extends AbstractIpsPluginTest {

    private MockHelper helper;

    private IDescription usDescription;

    private IDescription germanDescription;

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
        IIpsObjectPartContainer objectPartContainer = policyCmptType.newAttribute();
        helper = new MockHelper(objectPartContainer);

        usDescription = helper.newDescription();
        usDescription.setLocale(Locale.US);
        germanDescription = helper.newDescription();
        germanDescription.setLocale(Locale.GERMAN);

        usLabel = helper.newLabel();
        usLabel.setLocale(Locale.US);
        germanLabel = helper.newLabel();
        germanLabel.setLocale(Locale.GERMAN);
    }

    public void testGetChildren() {
        IIpsElement[] children = helper.getChildren();
        assertEquals(4, children.length);

        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(usDescription));
        assertTrue(childrenList.contains(germanDescription));
        assertTrue(childrenList.contains(usLabel));
        assertTrue(childrenList.contains(germanLabel));
    }

    public void testReinitPartCollections() {
        assertEquals(2, helper.getLabels().size());
        assertEquals(2, helper.getDescriptions().size());
        helper.reinitPartCollections();
        assertEquals(0, helper.getLabels().size());
        assertEquals(0, helper.getDescriptions().size());
    }

    public void testAddRemovePart() {
        ILabel labelToAdd = helper.newLabel();
        assertEquals(3, helper.getLabels().size());
        helper.removePart(labelToAdd);
        assertEquals(2, helper.getLabels().size());
        helper.addPart(labelToAdd);
        assertEquals(3, helper.getLabels().size());

        IDescription descriptionToAdd = helper.newDescription();
        assertEquals(3, helper.getDescriptions().size());
        helper.removePart(descriptionToAdd);
        assertEquals(2, helper.getDescriptions().size());
        helper.addPart(descriptionToAdd);
        assertEquals(3, helper.getDescriptions().size());
    }

    public void testNewPart() throws DOMException, ParserConfigurationException {
        Document xmlDoc = createXmlDocument("Blub");
        Element element = xmlDoc.createElement(ILabel.XML_TAG_NAME);
        assertTrue(helper.newPart(element, "blub") instanceof ILabel);
        assertEquals(3, helper.getLabels().size());

        element = xmlDoc.createElement(IDescription.XML_TAG_NAME);
        assertTrue(helper.newPart(element, "blub") instanceof IDescription);
        assertEquals(3, helper.getDescriptions().size());

        element = xmlDoc.createElement("foobar");
        assertNull(helper.newPart(element, "xyz"));
    }

    public void testNewDescription() {
        assertEquals(2, helper.getDescriptions().size());
        assertNotNull(helper.newDescription());
        assertEquals(3, helper.getDescriptions().size());
    }

    public void testNewLabel() {
        assertEquals(2, helper.getLabels().size());
        assertNotNull(helper.newLabel());
        assertEquals(3, helper.getLabels().size());
    }

    public void testGetDescription() {
        assertEquals(usDescription, helper.getDescription(Locale.US));
        assertEquals(germanDescription, helper.getDescription(Locale.GERMAN));
        assertNull(helper.getDescription(Locale.KOREAN));
    }

    public void testGetLabel() {
        assertEquals(usLabel, helper.getLabel(Locale.US));
        assertEquals(germanLabel, helper.getLabel(Locale.GERMAN));
        assertNull(helper.getLabel(Locale.KOREAN));
    }

    public void testGetDescriptions() {
        Set<IDescription> descriptionSet = helper.getDescriptions();
        assertEquals(2, descriptionSet.size());
        assertTrue(descriptionSet.contains(usDescription));
        assertTrue(descriptionSet.contains(germanDescription));
        try {
            descriptionSet.remove(germanDescription);
            fail();
        } catch (UnsupportedOperationException e) {
        }
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

    public void testGetDescriptionForIpsModelLocale() {
        Locale ipsModelLocale = IpsPlugin.getDefault().getIpsModelLocale();
        assertEquals(ipsModelLocale.getLanguage(), helper.getDescriptionForIpsModelLocale().getLocale().getLanguage());
    }

    public void testGetLabelForIpsModelLocale() {
        Locale ipsModelLocale = IpsPlugin.getDefault().getIpsModelLocale();
        assertEquals(ipsModelLocale.getLanguage(), helper.getLabelForIpsModelLocale().getLocale().getLanguage());
    }

    public void testGetDescriptionForDefaultLocale() {
        Locale defaultLocale = Locale.GERMAN;
        assertEquals(defaultLocale, helper.getDescriptionForDefaultLocale().getLocale());
    }

    public void testGetLabelForDefaultLocale() {
        Locale defaultLocale = Locale.GERMAN;
        assertEquals(defaultLocale, helper.getLabelForDefaultLocale().getLocale());
    }

    // This class is necessary to be able to access protected methods.
    private static class MockHelper extends DescribedAndLabeledHelper {

        public MockHelper(IIpsObjectPartContainer ipsObjectPartContainer) {
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
