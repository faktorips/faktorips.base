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

package org.faktorips.devtools.core;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Alexander Weickmann
 */
public class MultiLanguageSupportTest extends AbstractIpsPluginTest {

    private static final String TARGET_ROLE_SINGULAR = "target role singular";

    private static final String TARGET_ROLE_PLURAL = "target role plural";

    private static final String GERMAN_DESCRIPTION = "German Description";

    private static final String US_DESCRIPTION = "US Description";

    private static final String GERMAN_LABEL = "German Label";

    private static final String GERMAN_PLURAL_LABEL = "German Plural Label";

    private static final String US_LABEL = "US Label";

    private static final String US_PLURAL_LABEL = "US Plural Label";

    private MultiLanguageSupport support;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private IAssociation association;

    private IAttribute attribute;

    private IDescription germanDescription;

    private IDescription usDescription;

    private ILabel germanLabel;

    private ILabel usLabel;

    private CaptionedContainer captionedPart;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        support = IpsPlugin.getMultiLanguageSupport();
        ipsProject = newIpsProjectWithMultiLanguageSupport();

        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        association = policyCmptType.newAssociation();
        association.setTargetRoleSingular(TARGET_ROLE_SINGULAR);
        association.setTargetRolePlural(TARGET_ROLE_PLURAL);
        attribute = policyCmptType.newAttribute();

        germanDescription = policyCmptType.getDescription(Locale.GERMAN);
        germanDescription.setText(GERMAN_DESCRIPTION);
        usDescription = policyCmptType.getDescription(Locale.US);
        usDescription.setText(US_DESCRIPTION);

        germanLabel = association.getLabel(Locale.GERMAN);
        germanLabel.setValue(GERMAN_LABEL);
        germanLabel.setPluralValue(GERMAN_PLURAL_LABEL);
        usLabel = association.getLabel(Locale.US);
        usLabel.setValue(US_LABEL);
        usLabel.setPluralValue(US_PLURAL_LABEL);

        captionedPart = new CaptionedContainer(policyCmptType, "id");
    }

    public void testGetLocalizedCaption() {
        String localizedCaption = support.getLocalizedCaption(captionedPart);
        assertEquals("Caption for " + support.getLocalizationLocale().getLanguage(), localizedCaption);
    }

    public void testGetLocalizedCaptionLocalizedCaptionMissing() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        String localizedCaption = support.getLocalizedCaption(captionedPart);
        assertEquals("Caption for " + Locale.GERMAN.getLanguage(), localizedCaption);
    }

    public void testGetLocalizedCaptionLocalizedCaptionMissingEmptyString() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        captionedPart.emptyCaptionLocale1 = true;
        String localizedCaption = support.getLocalizedCaption(captionedPart);
        assertEquals("Caption for " + Locale.GERMAN.getLanguage(), localizedCaption);
    }

    public void testGetLocalizedCaptionEvenDefaultCaptionMissing() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        captionedPart.missingCaptionLocale2 = Locale.GERMAN;
        assertEquals(CaptionedContainer.LAST_RESORT_CAPTION, support.getLocalizedCaption(captionedPart));
    }

    public void testGetLocalizedCaptionEvenDefaultCaptionMissingEmptyString() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        captionedPart.missingCaptionLocale2 = Locale.GERMAN;
        captionedPart.emptyCaptionLocale2 = true;
        assertEquals(CaptionedContainer.LAST_RESORT_CAPTION, support.getLocalizedCaption(captionedPart));
    }

    public void testGetLocalizedCaptionNullPointer() {
        try {
            support.getLocalizedCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetLocalizedPluralCaption() {
        String localizedPluralCaption = support.getLocalizedPluralCaption(captionedPart);
        assertEquals("Plural Caption for " + support.getLocalizationLocale().getLanguage(), localizedPluralCaption);
    }

    public void testGetLocalizedPluralCaptionLocalizedCaptionMissing() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        String localizedPluralCaption = support.getLocalizedPluralCaption(captionedPart);
        assertEquals("Plural Caption for " + Locale.GERMAN.getLanguage(), localizedPluralCaption);
    }

    public void testGetLocalizedPluralCaptionLocalizedCaptionMissingEmptyString() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        captionedPart.emptyCaptionLocale1 = true;
        String localizedPluralCaption = support.getLocalizedPluralCaption(captionedPart);
        assertEquals("Plural Caption for " + Locale.GERMAN.getLanguage(), localizedPluralCaption);
    }

    public void testGetLocalizedPluralCaptionEvenDefaultCaptionMissing() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        captionedPart.missingCaptionLocale2 = Locale.GERMAN;
        assertEquals(CaptionedContainer.LAST_RESORT_PLURAL_CAPTION, support.getLocalizedPluralCaption(captionedPart));
    }

    public void testGetLocalizedPluralCaptionEvenDefaultCaptionMissingEmptyString() {
        captionedPart.missingCaptionLocale1 = support.getLocalizationLocale();
        captionedPart.missingCaptionLocale2 = Locale.GERMAN;
        captionedPart.emptyCaptionLocale2 = true;
        assertEquals(CaptionedContainer.LAST_RESORT_PLURAL_CAPTION, support.getLocalizedPluralCaption(captionedPart));
    }

    public void testGetLocalizedPluralCaptionNullPointer() {
        try {
            support.getLocalizedPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultCaption() {
        assertEquals("Caption for " + Locale.GERMAN.getLanguage(), support.getDefaultCaption(captionedPart));
    }

    public void testGetDefaultCaptionNotExisting() {
        captionedPart.missingCaptionLocale1 = Locale.GERMAN;
        assertEquals(CaptionedContainer.LAST_RESORT_CAPTION, support.getDefaultCaption(captionedPart));
    }

    public void testGetDefaultCaptionNotExistingEmptyString() {
        captionedPart.missingCaptionLocale1 = Locale.GERMAN;
        captionedPart.emptyCaptionLocale1 = true;
        assertEquals(CaptionedContainer.LAST_RESORT_CAPTION, support.getDefaultCaption(captionedPart));
    }

    public void testGetDefaultCaptionNullPointer() {
        try {
            support.getDefaultCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultCaptionNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        assertEquals(CaptionedContainer.LAST_RESORT_CAPTION, support.getDefaultCaption(captionedPart));
    }

    public void testGetDefaultPluralCaption() {
        assertEquals("Plural Caption for " + Locale.GERMAN.getLanguage(), support
                .getDefaultPluralCaption(captionedPart));
    }

    public void testGetDefaultPluralCaptionNotExisting() {
        captionedPart.missingCaptionLocale1 = Locale.GERMAN;
        assertEquals(CaptionedContainer.LAST_RESORT_PLURAL_CAPTION, support.getDefaultPluralCaption(captionedPart));
    }

    public void testGetDefaultPluralCaptionNotExistingEmptyString() {
        captionedPart.missingCaptionLocale1 = Locale.GERMAN;
        captionedPart.emptyCaptionLocale1 = true;
        assertEquals(CaptionedContainer.LAST_RESORT_PLURAL_CAPTION, support.getDefaultPluralCaption(captionedPart));
    }

    public void testGetDefaultPluralCaptionNullPointer() {
        try {
            support.getDefaultPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultPluralCaptionNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        assertEquals(CaptionedContainer.LAST_RESORT_PLURAL_CAPTION, support.getDefaultPluralCaption(captionedPart));
    }

    public void testGetLocalizedLabel() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setValue("foo");
        assertEquals("foo", support.getLocalizedLabel(association));
    }

    public void testGetLocalizedLabelLocalizedLabelMissing() {
        deleteLocalizedLabel();
        if (germanLabel.isDeleted()) {
            assertEquals(association.getName(), support.getLocalizedLabel(association));
        } else {
            assertEquals(GERMAN_LABEL, support.getLocalizedLabel(association));
        }
    }

    public void testGetLocalizedLabelLocalizedLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setValue("");
        if (localizedLabel.equals(germanLabel)) {
            assertEquals(association.getName(), support.getLocalizedLabel(association));
        } else {
            assertEquals(GERMAN_LABEL, support.getLocalizedLabel(association));
        }
    }

    public void testGetLocalizedLabelEvenDefaultLabelMissing() {
        deleteLocalizedLabel();
        if (!(germanLabel.isDeleted())) {
            germanLabel.delete();
        }
        assertEquals(association.getName(), support.getLocalizedLabel(association));
    }

    public void testGetLocalizedLabelEvenDefaultLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setValue("");
        germanLabel.setValue("");
        assertEquals(association.getName(), support.getLocalizedLabel(association));
    }

    public void testGetLocalizedLabelNullPointer() {
        try {
            support.getLocalizedLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetLocalizedPluralLabel() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setPluralValue("foos");
        assertEquals("foos", support.getLocalizedPluralLabel(association));
    }

    public void testGetLocalizedPluralLabelLocalizedLabelMissing() {
        deleteLocalizedLabel();
        if (germanLabel.isDeleted()) {
            assertEquals(association.getName(), support.getLocalizedPluralLabel(association));
        } else {
            assertEquals(GERMAN_PLURAL_LABEL, support.getLocalizedPluralLabel(association));
        }
    }

    public void testGetLocalizedPluralLabelLocalizedLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setPluralValue("");
        if (localizedLabel.equals(germanLabel)) {
            assertEquals(association.getName(), support.getLocalizedPluralLabel(association));
        } else {
            assertEquals(GERMAN_PLURAL_LABEL, support.getLocalizedPluralLabel(association));
        }
    }

    public void testGetLocalizedPluralLabelLocalizedEvenDefaultLabelMissing() {
        deleteLocalizedLabel();
        if (!(germanLabel.isDeleted())) {
            germanLabel.delete();
        }
        assertEquals(association.getName(), support.getLocalizedPluralLabel(association));
    }

    public void testGetLocalizedPluralLabelLocalizedEvenDefaultLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setPluralValue("");
        germanLabel.setPluralValue("");
        assertEquals(association.getName(), support.getLocalizedPluralLabel(association));
    }

    public void testGetLocalizedPluralLabelNullPointer() {
        try {
            support.getLocalizedPluralLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetLocalizedPluralLabelNotSupported() {
        try {
            support.getLocalizedPluralLabel(attribute);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetDefaultLabel() {
        assertEquals(GERMAN_LABEL, support.getDefaultLabel(association));
    }

    public void testGetDefaultLabelNotExistent() {
        germanLabel.delete();
        assertEquals(association.getName(), support.getDefaultLabel(association));
    }

    public void testGetDefaultLabelNotExistentEmptyString() {
        germanLabel.setValue("");
        assertEquals(association.getName(), support.getDefaultLabel(association));
    }

    public void testGetDefaultLabelNullPointer() {
        try {
            support.getDefaultLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultLabelNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        assertEquals(association.getName(), support.getDefaultLabel(association));
    }

    public void testGetDefaultPluralLabel() {
        assertEquals(GERMAN_PLURAL_LABEL, support.getDefaultPluralLabel(association));
    }

    public void testGetDefaultPluralLabelNotExistent() {
        germanLabel.delete();
        assertEquals(association.getName(), support.getDefaultPluralLabel(association));
    }

    public void testGetDefaultPluralLabelNotExistentEmptyString() {
        germanLabel.setPluralValue("");
        assertEquals(association.getName(), support.getDefaultPluralLabel(association));
    }

    public void testGetDefaultPluralLabelNullPointer() {
        try {
            support.getDefaultPluralLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultPluralLabelNotSupported() {
        try {
            support.getDefaultPluralLabel(attribute);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetDefaultPluralLabelNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        assertEquals(association.getName(), support.getDefaultPluralLabel(association));
    }

    public void testSetDefaultLabel() {
        support.setDefaultLabel(association, "foo");
        assertEquals("foo", germanLabel.getValue());
    }

    public void testSetDefaultLabelNullPointer1() {
        try {
            support.setDefaultLabel(null, "foo");
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultLabelNullPointer2() {
        support.setDefaultLabel(association, null);
        assertEquals(null, germanLabel.getValue());
    }

    public void testSetDefaultLabelNotExistent() {
        germanLabel.delete();
        support.setDefaultLabel(association, "foo");
        assertEquals(GERMAN_LABEL, germanLabel.getValue());
        assertEquals(US_LABEL, usLabel.getValue());
    }

    public void testSetDefaultLabelNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        support.setDefaultLabel(association, "foo");
        assertEquals(GERMAN_LABEL, germanLabel.getValue());
        assertEquals(US_LABEL, usLabel.getValue());
    }

    public void testSetDefaultPluralLabel() {
        support.setDefaultPluralLabel(association, "foos");
        assertEquals("foos", germanLabel.getPluralValue());
    }

    public void testSetDefaultPluralLabelNullPointer1() {
        try {
            support.setDefaultPluralLabel(null, "foos");
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultPluralLabelNullPointer2() {
        support.setDefaultPluralLabel(association, null);
        assertEquals(null, germanLabel.getPluralValue());
    }

    public void testSetDefaultPluralLabelNotSupported() {
        try {
            support.setDefaultPluralLabel(attribute, "foos");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetDefaultPluralLabelNotExistent() {
        germanLabel.delete();
        support.setDefaultPluralLabel(association, "foos");
        assertEquals(GERMAN_PLURAL_LABEL, germanLabel.getPluralValue());
        assertEquals(US_PLURAL_LABEL, usLabel.getPluralValue());
    }

    public void testSetDefaultPluralLabelNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        support.setDefaultLabel(association, "foos");
        assertEquals(GERMAN_PLURAL_LABEL, germanLabel.getPluralValue());
        assertEquals(US_PLURAL_LABEL, usLabel.getPluralValue());
    }

    public void testGetLocalizedDescription() {
        IDescription localizedDescription = getLocalizedDescription();
        localizedDescription.setText("foo");
        assertEquals("foo", support.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionLocalizedDescriptionMissing() {
        deleteLocalizedDescription();
        if (germanDescription.isDeleted()) {
            assertEquals("", support.getLocalizedDescription(policyCmptType));
        } else {
            assertEquals(GERMAN_DESCRIPTION, support.getLocalizedDescription(policyCmptType));
        }
    }

    public void testGetLocalizedDescriptionLocalizedDescriptionMissingEmptyString() {
        IDescription localizedDescription = getLocalizedDescription();
        localizedDescription.setText("");
        if (localizedDescription.equals(germanDescription)) {
            assertEquals("", support.getLocalizedDescription(policyCmptType));
        } else {
            assertEquals(GERMAN_DESCRIPTION, support.getLocalizedDescription(policyCmptType));
        }
    }

    public void testGetLocalizedDescriptionEvenDefaultDescriptionMissing() {
        deleteLocalizedDescription();
        if (!(germanDescription.isDeleted())) {
            germanDescription.delete();
        }
        assertEquals("", support.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionEvenDefaultDescriptionMissingEmptyString() {
        IDescription localizedDescription = getLocalizedDescription();
        localizedDescription.setText("");
        germanDescription.setText("");
        assertEquals("", support.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionNullPointer() {
        try {
            support.getLocalizedDescription(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultDescription() {
        assertEquals(GERMAN_DESCRIPTION, support.getDefaultDescription(policyCmptType));
    }

    public void testGetDefaultDescriptionNullPointer() {
        try {
            support.getDefaultDescription(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetDefaultDescriptionNotExsitent() {
        germanDescription.delete();
        assertEquals("", support.getDefaultDescription(policyCmptType));
    }

    public void testGetDefaultDescriptionNotExsitentEmptyString() {
        germanDescription.setText("");
        assertEquals("", support.getDefaultDescription(policyCmptType));
    }

    public void testGetDefaultDescriptionNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        assertEquals("", support.getDefaultDescription(policyCmptType));
    }

    public void testSetDefaultDescription() {
        support.setDefaultDescription(policyCmptType, "foo");
        assertEquals("foo", germanDescription.getText());
        assertEquals(US_DESCRIPTION, usDescription.getText());
    }

    public void testSetDefaultDescriptionNullPointer1() {
        try {
            support.setDefaultDescription(null, "foo");
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultDescriptionNullPointer2() {
        try {
            support.setDefaultDescription(policyCmptType, null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testSetDefaultDescriptionNotExistent() {
        germanDescription.delete();
        support.setDefaultDescription(policyCmptType, "foo");
        assertEquals(GERMAN_DESCRIPTION, germanDescription.getText());
        assertEquals(US_DESCRIPTION, usDescription.getText());
    }

    public void testSetDefaultDescriptionNoDefaultLanguage() throws CoreException {
        removeDefaultLanguage();
        support.setDefaultDescription(policyCmptType, "foo");
        assertEquals(GERMAN_DESCRIPTION, germanDescription.getText());
        assertEquals(US_DESCRIPTION, usDescription.getText());
    }

    public void testGetLocalizationLocale() {
        Locale localizationLocale = support.getLocalizationLocale();
        String nl = Platform.getNL();
        if (nl.length() > 2) {
            nl = nl.substring(0, 2);
        }
        assertEquals(nl, localizationLocale.getLanguage());
    }

    private void deleteLocalizedLabel() {
        Locale localizationLocale = support.getLocalizationLocale();
        ILabel localizedLabel = association.getLabel(localizationLocale);
        if (localizedLabel != null) {
            localizedLabel.delete();
        }
    }

    private void deleteLocalizedDescription() {
        Locale localizationLocale = support.getLocalizationLocale();
        IDescription localizedDescription = policyCmptType.getDescription(localizationLocale);
        if (localizedDescription != null) {
            localizedDescription.delete();
        }
    }

    private ILabel getLocalizedLabel() {
        Locale localizationLocale = support.getLocalizationLocale();
        ILabel localizedLabel = association.getLabel(localizationLocale);
        if (localizedLabel == null) {
            localizedLabel = association.newLabel();
            localizedLabel.setLocale(localizationLocale);
        }
        return localizedLabel;
    }

    private IDescription getLocalizedDescription() {
        Locale localizationLocale = support.getLocalizationLocale();
        IDescription localizedDescription = policyCmptType.getDescription(localizationLocale);
        if (localizedDescription == null) {
            localizedDescription = policyCmptType.newDescription();
            localizedDescription.setLocale(localizationLocale);
        }
        return localizedDescription;
    }

    private void removeDefaultLanguage() throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.removeSupportedLanguage(properties.getSupportedLanguage(Locale.GERMAN));
        ipsProject.setProperties(properties);
    }

    private static class CaptionedContainer extends AtomicIpsObjectPart {

        private static final String LAST_RESORT_CAPTION = "Last Resort Caption";

        private static final String LAST_RESORT_PLURAL_CAPTION = "Last Resort Plural Caption";

        private Locale missingCaptionLocale1;

        private Locale missingCaptionLocale2;

        private boolean emptyCaptionLocale1;

        private boolean emptyCaptionLocale2;

        public CaptionedContainer(IIpsObjectPartContainer parent, String id) {
            super(parent, id);
        }

        @Override
        protected Element createElement(Document doc) {
            return null;
        }

        @Override
        public String getCaption(Locale locale) {
            if (locale.equals(missingCaptionLocale1) && emptyCaptionLocale1) {
                return "";
            }
            if (locale.equals(missingCaptionLocale2) && emptyCaptionLocale2) {
                return "";
            }
            if (locale.equals(missingCaptionLocale1) || locale.equals(missingCaptionLocale2)) {
                return null;
            }
            return "Caption for " + locale.getLanguage();
        }

        @Override
        public String getPluralCaption(Locale locale) {
            if (locale.equals(missingCaptionLocale1) && emptyCaptionLocale1) {
                return "";
            }
            if (locale.equals(missingCaptionLocale2) && emptyCaptionLocale2) {
                return "";
            }
            if (locale.equals(missingCaptionLocale1) || locale.equals(missingCaptionLocale2)) {
                return null;
            }
            return "Plural Caption for " + locale.getLanguage();
        }

        @Override
        public String getLastResortCaption() {
            return LAST_RESORT_CAPTION;
        }

        @Override
        public String getLastResortPluralCaption() {
            return LAST_RESORT_PLURAL_CAPTION;
        }

    }

}
