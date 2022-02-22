/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Alexander Weickmann
 */
public class MultiLanguageSupportTest extends AbstractIpsPluginTest {

    private static final String GERMAN_DESCRIPTION = "German Description";

    private static final String US_DESCRIPTION = "US Description";

    private static final String GERMAN_LABEL = "German Label";

    private static final String GERMAN_PLURAL_LABEL = "German Plural Label";

    private static final String US_LABEL = "US Label";

    private static final String US_PLURAL_LABEL = "US Plural Label";

    private MultiLanguageSupport support;

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IDescription germanDescription;

    private IDescription usDescription;

    private ILabel germanLabel;

    private ILabel usLabel;

    private TestContainer testContainer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        support = new MultiLanguageSupport(Locale.US);
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "TestPolicy");
        testContainer = new TestContainer(productCmptType, "id");

        germanDescription = testContainer.getDescription(Locale.GERMAN);
        germanDescription.setText(GERMAN_DESCRIPTION);
        usDescription = testContainer.getDescription(Locale.US);
        usDescription.setText(US_DESCRIPTION);

        germanLabel = testContainer.getLabel(Locale.GERMAN);
        germanLabel.setValue(GERMAN_LABEL);
        germanLabel.setPluralValue(GERMAN_PLURAL_LABEL);
        usLabel = testContainer.getLabel(Locale.US);
        usLabel.setValue(US_LABEL);
        usLabel.setPluralValue(US_PLURAL_LABEL);
    }

    @Test
    public void testGetLocalizedCaption() {
        String localizedCaption = support.getLocalizedCaption(testContainer);
        assertEquals("Caption for " + support.getLocalizationLocale().getLanguage(), localizedCaption);
    }

    @Test
    public void testGetLocalizedCaptionLocalizedCaptionMissing() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        String localizedCaption = support.getLocalizedCaption(testContainer);
        assertEquals("Caption for " + Locale.GERMAN.getLanguage(), localizedCaption);
    }

    @Test
    public void testGetLocalizedCaptionLocalizedCaptionMissingEmptyString() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        testContainer.emptyCaptionLocale1 = true;
        String localizedCaption = support.getLocalizedCaption(testContainer);
        assertEquals("Caption for " + Locale.GERMAN.getLanguage(), localizedCaption);
    }

    @Test
    public void testGetLocalizedCaptionEvenDefaultCaptionMissing() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        testContainer.missingCaptionLocale2 = Locale.GERMAN;
        assertEquals(TestContainer.LAST_RESORT_CAPTION, support.getLocalizedCaption(testContainer));
    }

    @Test
    public void testGetLocalizedCaptionEvenDefaultCaptionMissingEmptyString() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        testContainer.missingCaptionLocale2 = Locale.GERMAN;
        testContainer.emptyCaptionLocale2 = true;
        assertEquals(TestContainer.LAST_RESORT_CAPTION, support.getLocalizedCaption(testContainer));
    }

    @Test
    public void testGetLocalizedCaptionNullPointer() {
        try {
            support.getLocalizedCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLocalizedPluralCaption() {
        String localizedPluralCaption = support.getLocalizedPluralCaption(testContainer);
        assertEquals("Plural Caption for " + support.getLocalizationLocale().getLanguage(), localizedPluralCaption);
    }

    @Test
    public void testGetLocalizedPluralCaptionLocalizedCaptionMissing() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        String localizedPluralCaption = support.getLocalizedPluralCaption(testContainer);
        assertEquals("Plural Caption for " + Locale.GERMAN.getLanguage(), localizedPluralCaption);
    }

    @Test
    public void testGetLocalizedPluralCaptionLocalizedCaptionMissingEmptyString() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        testContainer.emptyCaptionLocale1 = true;
        String localizedPluralCaption = support.getLocalizedPluralCaption(testContainer);
        assertEquals("Plural Caption for " + Locale.GERMAN.getLanguage(), localizedPluralCaption);
    }

    @Test
    public void testGetLocalizedPluralCaptionEvenDefaultCaptionMissing() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        testContainer.missingCaptionLocale2 = Locale.GERMAN;
        assertEquals(TestContainer.LAST_RESORT_PLURAL_CAPTION, support.getLocalizedPluralCaption(testContainer));
    }

    @Test
    public void testGetLocalizedPluralCaptionEvenDefaultCaptionMissingEmptyString() {
        testContainer.missingCaptionLocale1 = support.getLocalizationLocale();
        testContainer.missingCaptionLocale2 = Locale.GERMAN;
        testContainer.emptyCaptionLocale2 = true;
        assertEquals(TestContainer.LAST_RESORT_PLURAL_CAPTION, support.getLocalizedPluralCaption(testContainer));
    }

    @Test
    public void testGetLocalizedPluralCaptionNullPointer() {
        try {
            support.getLocalizedPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultCaption() {
        assertEquals("Caption for " + Locale.GERMAN.getLanguage(), support.getDefaultCaption(testContainer));
    }

    @Test
    public void testGetDefaultCaptionNotExisting() {
        testContainer.missingCaptionLocale1 = Locale.GERMAN;
        assertEquals(TestContainer.LAST_RESORT_CAPTION, support.getDefaultCaption(testContainer));
    }

    @Test
    public void testGetDefaultCaptionNotExistingEmptyString() {
        testContainer.missingCaptionLocale1 = Locale.GERMAN;
        testContainer.emptyCaptionLocale1 = true;
        assertEquals(TestContainer.LAST_RESORT_CAPTION, support.getDefaultCaption(testContainer));
    }

    @Test
    public void testGetDefaultCaptionNullPointer() {
        try {
            support.getDefaultCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultCaptionNoDefaultLanguage() {
        removeDefaultLanguage();
        assertEquals("Caption for " + Locale.ENGLISH.getLanguage(), support.getDefaultCaption(testContainer));

    }

    @Test
    public void testGetDefaultPluralCaption() {
        assertEquals("Plural Caption for " + Locale.GERMAN.getLanguage(),
                support.getDefaultPluralCaption(testContainer));
    }

    @Test
    public void testGetDefaultPluralCaptionNotExisting() {
        testContainer.missingCaptionLocale1 = Locale.GERMAN;
        assertEquals(TestContainer.LAST_RESORT_PLURAL_CAPTION, support.getDefaultPluralCaption(testContainer));
    }

    @Test
    public void testGetDefaultPluralCaptionNotExistingEmptyString() {
        testContainer.missingCaptionLocale1 = Locale.GERMAN;
        testContainer.emptyCaptionLocale1 = true;
        assertEquals(TestContainer.LAST_RESORT_PLURAL_CAPTION, support.getDefaultPluralCaption(testContainer));
    }

    @Test
    public void testGetDefaultPluralCaptionNullPointer() {
        try {
            support.getDefaultPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultPluralCaptionNoDefaultLanguage() {
        removeDefaultLanguage();
        assertEquals("Plural Caption for " + Locale.ENGLISH.getLanguage(),
                support.getDefaultPluralCaption(testContainer));
    }

    @Test
    public void testGetLocalizedLabel() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setValue("foo");
        assertEquals("foo", support.getLocalizedLabel(testContainer));
    }

    @Test
    public void testGetLocalizedLabelLocalizedLabelMissing() {
        deleteLocalizedLabel();
        if (germanLabel.isDeleted()) {
            assertEquals(testContainer.getName(), support.getLocalizedLabel(testContainer));
        } else {
            assertEquals(GERMAN_LABEL, support.getLocalizedLabel(testContainer));
        }
    }

    @Test
    public void testGetLocalizedLabelLocalizedLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setValue("");
        if (localizedLabel.equals(germanLabel)) {
            assertEquals(StringUtils.capitalize(testContainer.getName()), support.getLocalizedLabel(testContainer));
        } else {
            assertEquals(GERMAN_LABEL, support.getLocalizedLabel(testContainer));
        }
    }

    @Test
    public void testGetLocalizedLabelEvenDefaultLabelMissing() {
        deleteLocalizedLabel();
        if (!(germanLabel.isDeleted())) {
            germanLabel.delete();
        }
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getLocalizedLabel(testContainer));
    }

    @Test
    public void testGetLocalizedLabelEvenDefaultLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setValue("");
        germanLabel.setValue("");
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getLocalizedLabel(testContainer));
    }

    @Test
    public void testGetLocalizedLabelNullPointer() {
        try {
            support.getLocalizedLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLocalizedPluralLabel() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setPluralValue("foos");
        assertEquals("foos", support.getLocalizedPluralLabel(testContainer));
    }

    @Test
    public void testGetLocalizedPluralLabelLocalizedLabelMissing() {
        deleteLocalizedLabel();
        if (germanLabel.isDeleted()) {
            assertEquals(StringUtils.capitalize(testContainer.getName()),
                    support.getLocalizedPluralLabel(testContainer));
        } else {
            assertEquals(GERMAN_PLURAL_LABEL, support.getLocalizedPluralLabel(testContainer));
        }
    }

    @Test
    public void testGetLocalizedPluralLabelLocalizedLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setPluralValue("");
        if (localizedLabel.equals(germanLabel)) {
            assertEquals(StringUtils.capitalize(testContainer.getName()),
                    support.getLocalizedPluralLabel(testContainer));
        } else {
            assertEquals(GERMAN_PLURAL_LABEL, support.getLocalizedPluralLabel(testContainer));
        }
    }

    @Test
    public void testGetLocalizedPluralLabelEvenDefaultLabelMissing() {
        deleteLocalizedLabel();
        if (!(germanLabel.isDeleted())) {
            germanLabel.delete();
        }
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getLocalizedPluralLabel(testContainer));
    }

    @Test
    public void testGetLocalizedPluralLabelEvenDefaultLabelMissingEmptyString() {
        ILabel localizedLabel = getLocalizedLabel();
        localizedLabel.setPluralValue("");
        germanLabel.setPluralValue("");
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getLocalizedPluralLabel(testContainer));
    }

    @Test
    public void testGetLocalizedPluralLabelNullPointer() {
        try {
            support.getLocalizedPluralLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLocalizedPluralLabelNotSupported() {
        try {
            support.getLocalizedPluralLabel(productCmptType.newAttribute());
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetDefaultLabel() {
        assertEquals(GERMAN_LABEL, support.getDefaultLabel(testContainer));
    }

    @Test
    public void testGetDefaultLabelNotExistent() {
        germanLabel.delete();
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getDefaultLabel(testContainer));
    }

    @Test
    public void testGetDefaultLabelNotExistentEmptyString() {
        germanLabel.setValue("");
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getDefaultLabel(testContainer));
    }

    @Test
    public void testGetDefaultLabelNullPointer() {
        try {
            support.getDefaultLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultLabelNoDefaultLanguage() {
        removeDefaultLanguage();
        assertEquals(US_LABEL, support.getDefaultLabel(testContainer));
    }

    @Test
    public void testGetDefaultPluralLabel() {
        assertEquals(GERMAN_PLURAL_LABEL, support.getDefaultPluralLabel(testContainer));
    }

    @Test
    public void testGetDefaultPluralLabelNotExistent() {
        germanLabel.delete();
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getDefaultPluralLabel(testContainer));
    }

    @Test
    public void testGetDefaultPluralLabelNotExistentEmptyString() {
        germanLabel.setPluralValue("");
        assertEquals(StringUtils.capitalize(testContainer.getName()), support.getDefaultPluralLabel(testContainer));
    }

    @Test
    public void testGetDefaultPluralLabelNullPointer() {
        try {
            support.getDefaultPluralLabel(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultPluralLabelNotSupported() {
        try {
            support.getDefaultPluralLabel(productCmptType.newAttribute());
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetDefaultPluralLabelNoDefaultLanguage() {
        removeDefaultLanguage();
        assertEquals(US_PLURAL_LABEL, support.getDefaultPluralLabel(testContainer));
    }

    @Test
    public void testSetDefaultLabel() {
        support.setDefaultLabel(testContainer, "foo");
        assertEquals("foo", germanLabel.getValue());
    }

    @Test
    public void testSetDefaultLabelNullPointer1() {
        try {
            support.setDefaultLabel(null, "foo");
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultLabelNullPointer2() {
        support.setDefaultLabel(testContainer, null);
        assertEquals("", germanLabel.getValue());
    }

    @Test
    public void testSetDefaultLabelNotExistent() {
        germanLabel.delete();
        support.setDefaultLabel(testContainer, "foo");
        assertEquals(GERMAN_LABEL, germanLabel.getValue());
        assertEquals(US_LABEL, usLabel.getValue());
    }

    @Test
    public void testSetDefaultLabelNoDefaultLanguage() {
        removeDefaultLanguage();
        support.setDefaultLabel(testContainer, "foo");
        assertEquals(GERMAN_LABEL, germanLabel.getValue());
        assertEquals("foo", usLabel.getValue());
    }

    @Test
    public void testSetDefaultPluralLabel() {
        support.setDefaultPluralLabel(testContainer, "foos");
        assertEquals("foos", germanLabel.getPluralValue());
    }

    @Test
    public void testSetDefaultPluralLabelNullPointer1() {
        try {
            support.setDefaultPluralLabel(null, "foos");
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultPluralLabelNullPointer2() {
        support.setDefaultPluralLabel(testContainer, null);
        assertEquals("", germanLabel.getPluralValue());
    }

    @Test
    public void testSetDefaultPluralLabelNotSupported() {
        try {
            support.setDefaultPluralLabel(productCmptType.newAttribute(), "foos");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetDefaultPluralLabelNotExistent() {
        germanLabel.delete();
        support.setDefaultPluralLabel(testContainer, "foos");
        assertEquals(GERMAN_PLURAL_LABEL, germanLabel.getPluralValue());
        assertEquals(US_PLURAL_LABEL, usLabel.getPluralValue());
    }

    @Test
    public void testSetDefaultPluralLabelNoDefaultLanguage() {
        removeDefaultLanguage();
        support.setDefaultLabel(testContainer, "foos");
        assertEquals(GERMAN_PLURAL_LABEL, germanLabel.getPluralValue());
        assertEquals(US_PLURAL_LABEL, usLabel.getPluralValue());
    }

    @Test
    public void testGetLocalizedDescription() {
        IDescription localizedDescription = getLocalizedDescription();
        localizedDescription.setText("foo");
        assertEquals("foo", support.getLocalizedDescription(testContainer));
    }

    @Test
    public void testGetLocalizedDescriptionLocalizedDescriptionMissing() {
        deleteLocalizedDescription();
        if (germanDescription.isDeleted()) {
            assertEquals("", support.getLocalizedDescription(testContainer));
        } else {
            assertEquals(GERMAN_DESCRIPTION, support.getLocalizedDescription(testContainer));
        }
    }

    @Test
    public void testGetLocalizedDescriptionLocalizedDescriptionMissingEmptyString() {
        IDescription localizedDescription = getLocalizedDescription();
        localizedDescription.setText("");
        if (localizedDescription.equals(germanDescription)) {
            assertEquals("", support.getLocalizedDescription(testContainer));
        } else {
            assertEquals(GERMAN_DESCRIPTION, support.getLocalizedDescription(testContainer));
        }
    }

    @Test
    public void testGetLocalizedDescriptionEvenDefaultDescriptionMissing() {
        deleteLocalizedDescription();
        if (!(germanDescription.isDeleted())) {
            germanDescription.delete();
        }
        assertEquals("", support.getLocalizedDescription(testContainer));
    }

    @Test
    public void testGetLocalizedDescriptionEvenDefaultDescriptionMissingEmptyString() {
        IDescription localizedDescription = getLocalizedDescription();
        localizedDescription.setText("");
        germanDescription.setText("");
        assertEquals("", support.getLocalizedDescription(testContainer));
    }

    @Test
    public void testGetLocalizedDescriptionNullPointer() {
        try {
            support.getLocalizedDescription(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultDescription() {
        assertEquals(GERMAN_DESCRIPTION, support.getDefaultDescription(testContainer));
    }

    @Test
    public void testGetDefaultDescriptionNullPointer() {
        try {
            support.getDefaultDescription(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetDefaultDescriptionNotExsitent() {
        germanDescription.delete();
        assertEquals("", support.getDefaultDescription(testContainer));
    }

    @Test
    public void testGetDefaultDescriptionNotExsitentEmptyString() {
        germanDescription.setText("");
        assertEquals("", support.getDefaultDescription(testContainer));
    }

    @Test
    public void testGetDefaultDescriptionNoDefaultLanguage() {
        removeDefaultLanguage();
        assertEquals(US_DESCRIPTION, support.getDefaultDescription(testContainer));
    }

    @Test
    public void testSetDefaultDescription() {
        support.setDefaultDescription(testContainer, "foo");
        assertEquals("foo", germanDescription.getText());
        assertEquals(US_DESCRIPTION, usDescription.getText());
    }

    @Test
    public void testSetDefaultDescriptionNullPointer1() {
        try {
            support.setDefaultDescription(null, "foo");
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultDescriptionNullPointer2() {
        try {
            support.setDefaultDescription(testContainer, null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSetDefaultDescriptionNotExistent() {
        germanDescription.delete();
        support.setDefaultDescription(testContainer, "foo");
        assertEquals(GERMAN_DESCRIPTION, germanDescription.getText());
        assertEquals(US_DESCRIPTION, usDescription.getText());
    }

    @Test
    public void testSetDefaultDescriptionNoDefaultLanguage() {
        removeDefaultLanguage();
        support.setDefaultDescription(testContainer, "foo");
        assertEquals(GERMAN_DESCRIPTION, germanDescription.getText());
        assertEquals("foo", usDescription.getText());
    }

    @Test
    public void testDefaultConstructor() {
        support = new MultiLanguageSupport();
        Locale localizationLocale = support.getLocalizationLocale();
        String nl = Platform.getNL();
        if (nl.length() > 2) {
            nl = nl.substring(0, 2);
        }
        assertEquals(nl, localizationLocale.getLanguage());
    }

    @Test
    public void testGetLocalizedContentForEnvironmentLocale() {
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, "foo");
        LocalizedString expectedUs = new LocalizedString(Locale.US, "jee"); // environment locale
        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, StringUtils.EMPTY);

        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(expectedEn);
        internationalStringValue.getContent().add(expectedUs);
        internationalStringValue.getContent().add(expectedDe);

        assertEquals("jee", support.getLocalizedContent(internationalStringValue, ipsProject));
    }

    @Test
    public void testGetLocalizedContentForProjectDefaultLocale() {
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, "foo");
        // project default locale
        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, "bar");

        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(expectedEn);
        internationalStringValue.getContent().add(expectedDe);

        assertEquals("bar", support.getLocalizedContent(internationalStringValue, ipsProject));
    }

    @Test
    public void testGetLocalizedContentFirstNonEmptyValue() {
        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, StringUtils.EMPTY);
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, "foo");

        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);

        assertEquals("foo", support.getLocalizedContent(internationalStringValue, ipsProject));
    }

    @Test
    public void testGetLocalizedContentNoValueDefined() {
        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, StringUtils.EMPTY);
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, StringUtils.EMPTY);

        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);

        assertEquals(StringUtils.EMPTY, support.getLocalizedContent(internationalStringValue, ipsProject));
    }

    private void deleteLocalizedLabel() {
        Locale localizationLocale = support.getLocalizationLocale();
        ILabel localizedLabel = testContainer.getLabel(localizationLocale);
        if (localizedLabel != null) {
            localizedLabel.delete();
        }
    }

    private void deleteLocalizedDescription() {
        Locale localizationLocale = support.getLocalizationLocale();
        IDescription localizedDescription = testContainer.getDescription(localizationLocale);
        if (localizedDescription != null) {
            localizedDescription.delete();
        }
    }

    private ILabel getLocalizedLabel() {
        Locale localizationLocale = support.getLocalizationLocale();
        ILabel localizedLabel = testContainer.getLabel(localizationLocale);
        if (localizedLabel == null) {
            localizedLabel = testContainer.newLabel();
            localizedLabel.setLocale(localizationLocale);
        }
        return localizedLabel;
    }

    private IDescription getLocalizedDescription() {
        Locale localizationLocale = support.getLocalizationLocale();
        IDescription localizedDescription = testContainer.getDescription(localizationLocale);
        if (localizedDescription == null) {
            localizedDescription = testContainer.newDescription();
            localizedDescription.setLocale(localizationLocale);
        }
        return localizedDescription;
    }

    private void removeDefaultLanguage() {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.removeSupportedLanguage(properties.getSupportedLanguage(Locale.GERMAN));
        ipsProject.setProperties(properties);
    }

    private static class TestContainer extends AtomicIpsObjectPart implements IDescribedElement, ILabeledElement {

        private static final String LAST_RESORT_CAPTION = "Last Resort Caption";

        private static final String LAST_RESORT_PLURAL_CAPTION = "Last Resort Plural Caption";

        private Locale missingCaptionLocale1;

        private Locale missingCaptionLocale2;

        private boolean emptyCaptionLocale1;

        private boolean emptyCaptionLocale2;

        public TestContainer(IIpsObjectPartContainer parent, String id) {
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

        @Override
        public String getName() {
            return "TestContainer";
        }

        @Override
        public boolean isPluralLabelSupported() {
            return true;
        }

    }

}
