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

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IOverridableElement;
import org.faktorips.devtools.model.type.IOverridableLabeledElement;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * This class provides several methods related to multi-language support.
 */
public class MultiLanguageSupport implements IMultiLanguageSupport {

    private final Locale localizationLocale;
    private final Locale usedLanguagePackLocale;

    /**
     * Creates the MultiLanguageSupport and uses the Locale of the Platform.
     */
    public MultiLanguageSupport() {
        this(getPlatformLocale());
    }

    /**
     * Creates a MultiLanguageSupport with the given Locale. Used by the HtmlExport
     * 
     * @param localizationLocale the Locale for the MultiLanguageSupport
     * 
     * @throws NullPointerException if localizationLocale is null
     */
    public MultiLanguageSupport(Locale localizationLocale) {
        ArgumentCheck.notNull(localizationLocale);

        this.localizationLocale = localizationLocale;
        usedLanguagePackLocale = new Locale(Messages.IpsPlugin_languagePackLanguage,
                Messages.IpsPlugin_languagePackCountry,
                Messages.IpsPlugin_languagePackVariant);
    }

    private static Locale getPlatformLocale() {
        return Abstractions.getLocale();
    }

    @Override
    public Locale getUsedLanguagePackLocale() {
        return usedLanguagePackLocale;
    }

    @Override
    public String getLocalizedCaption(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String localizedCaption = null;
        try {
            localizedCaption = ipsObjectPartContainer.getCaption(getLocalizationLocale());
        } catch (IpsException e) {
            // Exception not too critical, just log it and use the next possible caption.
            IpsLog.log(e);
        }
        if (IpsStringUtils.isEmpty(localizedCaption)) {
            localizedCaption = getDefaultCaption(ipsObjectPartContainer);
        }
        return localizedCaption;
    }

    @Override
    public String getLocalizedPluralCaption(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String localizedPluralCaption = null;
        try {
            localizedPluralCaption = ipsObjectPartContainer.getPluralCaption(getLocalizationLocale());
        } catch (IpsException e) {
            // Exception not too critical, just log it and use the next possible caption.
            IpsLog.log(e);
        }
        if (IpsStringUtils.isEmpty(localizedPluralCaption)) {
            localizedPluralCaption = getDefaultPluralCaption(ipsObjectPartContainer);
        }
        return localizedPluralCaption;
    }

    @Override
    public String getDefaultCaption(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String defaultCaption = null;
        Locale defaultLocale = getDefaultLocale(ipsObjectPartContainer.getIpsProject());
        if (defaultLocale != null) {
            try {
                defaultCaption = ipsObjectPartContainer.getCaption(defaultLocale);
            } catch (IpsException e) {
                // Exception not too critical, just log it and use the next possible caption.
                IpsLog.log(e);
            }
        }
        if (IpsStringUtils.isEmpty(defaultCaption)) {
            defaultCaption = ipsObjectPartContainer.getLastResortCaption();
        }
        return defaultCaption;
    }

    @Override
    public String getDefaultPluralCaption(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String defaultPluralCaption = null;
        Locale defaultLocale = getDefaultLocale(ipsObjectPartContainer.getIpsProject());
        if (defaultLocale != null) {
            try {
                defaultPluralCaption = ipsObjectPartContainer.getPluralCaption(defaultLocale);
            } catch (IpsException e) {
                // Exception not too critical, just log it and use the next possible caption.
                IpsLog.log(e);
            }
        }
        if (IpsStringUtils.isEmpty(defaultPluralCaption)) {
            defaultPluralCaption = ipsObjectPartContainer.getLastResortPluralCaption();
        }
        return defaultPluralCaption;
    }

    @Override
    public String getLocalizedLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);

        String label;
        if (labeledElement instanceof IOverridableLabeledElement) {
            label = ((IOverridableLabeledElement)labeledElement).getLabelValueFromThisOrSuper(getLocalizationLocale());
        } else {
            label = labeledElement.getLabelValue(getLocalizationLocale());
        }
        return (IpsStringUtils.isEmpty(label)) ? getDefaultLabel(labeledElement) : label;
    }

    @Override
    public String getLocalizedPluralLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);
        ArgumentCheck.isTrue(labeledElement.isPluralLabelSupported());

        String pluralLabel;
        if (labeledElement instanceof IAssociation) {
            pluralLabel = ((IAssociation)labeledElement)
                    .getPluralLabelValueFromThisOrSuper(getLocalizationLocale());
        } else {
            pluralLabel = labeledElement.getPluralLabelValue(getLocalizationLocale());
        }
        return (IpsStringUtils.isEmpty(pluralLabel)) ? getDefaultPluralLabel(labeledElement) : pluralLabel;
    }

    @Override
    public String getDefaultLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);

        String label = StringUtils.capitalize(labeledElement.getName());

        // TODO AW: Awkward hard-coded solution but so far we haven't agreed upon another approach
        if (labeledElement instanceof IProductCmptTypeMethod method) {
            if (method.isFormulaSignatureDefinition()) {
                label = StringUtils.capitalize(method.getFormulaName());
            }
        }

        String value = getDefaultLabelValue(labeledElement);

        return (IpsStringUtils.isEmpty(value)) ? label : value;
    }

    private String getDefaultLabelValue(ILabeledElement labeledElement) {
        Locale defaultLocale = getDefaultLocale(labeledElement.getIpsProject());
        if (defaultLocale != null) {
            if (labeledElement instanceof IOverridableLabeledElement) {
                return ((IOverridableLabeledElement)labeledElement).getLabelValueFromThisOrSuper(defaultLocale);
            } else {
                return labeledElement.getLabelValue(defaultLocale);
            }
        }
        return "";
    }

    @Override
    public String getDefaultPluralLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);
        ArgumentCheck.isTrue(labeledElement.isPluralLabelSupported());

        String pluralLabel = StringUtils.capitalize(labeledElement.getName());

        // TODO AW: Awkward hard-coded solution but so far we havn't agreed upon another approach
        if (labeledElement instanceof IAssociation) {
            pluralLabel = StringUtils.capitalize(((IAssociation)labeledElement).getTargetRolePlural());
        }

        String value = getDefaultPluralLabelValue(labeledElement);

        return (IpsStringUtils.isEmpty(value)) ? pluralLabel : value;
    }

    private String getDefaultPluralLabelValue(ILabeledElement labeledElement) {
        Locale defaultLocale = getDefaultLocale(labeledElement.getIpsProject());
        if (defaultLocale != null) {
            if (labeledElement instanceof IAssociation) {
                return ((IAssociation)labeledElement).getPluralLabelValueFromThisOrSuper(defaultLocale);
            } else {
                return labeledElement.getPluralLabelValue(defaultLocale);
            }
        }
        return "";
    }

    /**
     * Sets the label of the given {@link ILabeledElement} for the default language. The default
     * language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no label for that locale or no default language is specified, nothing will
     * happen.
     * 
     * @param labeledElement The {@link ILabeledElement} to set the default label for.
     * @param value The value to set the label to.
     * 
     * @throws NullPointerException If <code>labeledElement</code> is <code>null</code>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public void setDefaultLabel(ILabeledElement labeledElement, String value) {
        ArgumentCheck.notNull(labeledElement);

        ILabel defaultLabel = getDefaultLabelPart(labeledElement);
        if (defaultLabel != null) {
            defaultLabel.setValue(value);
        }
    }

    /**
     * Sets the plural label of the given {@link ILabeledElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no plural label for that locale or no default language is specified, nothing will
     * happen.
     * 
     * @param labeledElement The {@link ILabeledElement} to set the default plural label for.
     * @param pluralValue The value to set the plural label to.
     * 
     * @throws NullPointerException If <code>labeledElement</code> is <code>null</code>.
     * @throws IllegalArgumentException If the given {@link ILabeledElement} does not support plural
     *             labels.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     * @see ILabeledElement#isPluralLabelSupported()
     */
    public void setDefaultPluralLabel(ILabeledElement labeledElement, String pluralValue) {
        ArgumentCheck.notNull(labeledElement);
        ArgumentCheck.isTrue(labeledElement.isPluralLabelSupported());

        ILabel defaultLabel = getDefaultLabelPart(labeledElement);
        if (defaultLabel != null) {
            defaultLabel.setPluralValue(pluralValue);
        }
    }

    @Override
    public String getLocalizedDescription(IDescribedElement describedElement) {
        ArgumentCheck.notNull(describedElement);

        String description = ""; //$NON-NLS-1$
        if (describedElement instanceof IOverridableElement) {
            description = ((IOverridableElement)describedElement)
                    .getDescriptionTextFromThisOrSuper(getLocalizationLocale());
        } else {
            description = describedElement.getDescriptionText(getLocalizationLocale());
        }
        return (IpsStringUtils.isEmpty(description)) ? getDefaultDescription(describedElement) : description;
    }

    @Override
    public String getDefaultDescription(IDescribedElement describedElement) {
        ArgumentCheck.notNull(describedElement);

        String text = getDefaultDescriptionText(describedElement);

        return (IpsStringUtils.isEmpty(text)) ? "" : text;

    }

    private String getDefaultDescriptionText(IDescribedElement describedElement) {
        Locale defaultLocale = getDefaultLocale(describedElement.getIpsProject());
        if (defaultLocale != null) {
            if (describedElement instanceof IOverridableElement) {
                return ((IOverridableElement)describedElement)
                        .getDescriptionTextFromThisOrSuper(defaultLocale);
            } else {
                return describedElement.getDescriptionText(defaultLocale);
            }
        }
        return null;
    }

    /**
     * Sets the description of the given {@link IDescribedElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no description for that locale or no default language is specified, nothing will
     * happen.
     * 
     * @param describedElement The {@link IDescribedElement} to set the default description for.
     * @param text The description text to set.
     * 
     * @throws NullPointerException If <code>describedElement</code> or <code>text</code> is
     *             <code>null</code>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public void setDefaultDescription(IDescribedElement describedElement, String text) {
        ArgumentCheck.notNull(new Object[] { describedElement, text });

        IDescription defaultDescription = getDefaultDescriptionPart(describedElement);
        if (defaultDescription != null) {
            defaultDescription.setText(text);
        }
    }

    private ILabel getDefaultLabelPart(ILabeledElement labeledElement) {
        ILabel labelPart = null;
        Locale defaultLocale = getDefaultLocale(labeledElement.getIpsProject());
        if (defaultLocale != null) {
            labelPart = labeledElement.getLabel(defaultLocale);
        }
        return labelPart;
    }

    private IDescription getDefaultDescriptionPart(IDescribedElement describedElement) {
        IDescription descriptionPart = null;
        Locale defaultLocale = getDefaultLocale(describedElement.getIpsProject());
        if (defaultLocale != null) {
            descriptionPart = describedElement.getDescription(defaultLocale);
        }
        return descriptionPart;
    }

    /**
     * Returns the locale of the {@link ISupportedLanguage} that is set as the default language for
     * the given {@link IIpsProject}.
     */
    private Locale getDefaultLocale(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
    }

    @Override
    public Locale getLocalizationLocale() {
        return localizationLocale;
    }

    @Override
    public Locale getLocalizationLocaleOrDefault(IIpsProject ipsProject) {
        IIpsProjectProperties readOnlyProperties = ipsProject.getReadOnlyProperties();
        ISupportedLanguage supportedLanguage = readOnlyProperties.getSupportedLanguage(localizationLocale);
        if (supportedLanguage == null) {
            supportedLanguage = readOnlyProperties.getDefaultLanguage();
        }
        return supportedLanguage.getLocale();
    }

    @Override
    public String getLocalizedContent(IValue<?> value, IIpsProject ipsProject) {
        String localizedContent = value.getLocalizedContent(getLocalizationLocale());
        if (IpsStringUtils.isEmpty(localizedContent)) {
            localizedContent = value.getDefaultLocalizedContent(ipsProject);
            if (IpsStringUtils.isEmpty(localizedContent)) {
                localizedContent = value.getLocalizedContent();
            }
        }
        return localizedContent;
    }
}
