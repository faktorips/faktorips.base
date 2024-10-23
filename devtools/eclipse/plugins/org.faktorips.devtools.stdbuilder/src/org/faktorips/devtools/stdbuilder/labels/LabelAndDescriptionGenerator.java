/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.labels;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.util.DefaultLineSeparator;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractPropertiesGenerator;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.type.DocumentationKind;

public class LabelAndDescriptionGenerator extends AbstractPropertiesGenerator {

    public LabelAndDescriptionGenerator(AFile messagesPropertiesFile, ISupportedLanguage supportedLanguage,
            AbstractLocalizedPropertiesBuilder labelAndDescriptionPropertiesBuilder) {
        super(messagesPropertiesFile, supportedLanguage, labelAndDescriptionPropertiesBuilder,
                new LabelAndDescriptionProperties(supportedLanguage.isDefaultLanguage(),
                        DefaultLineSeparator.of(messagesPropertiesFile)));
    }

    void addLabelsAndDescriptions(IIpsObjectPartContainer ipsObjectPartContainer,
            LabelAndDescriptionProperties labelsAndDescriptions) {
        addLabelAndDescription(ipsObjectPartContainer, labelsAndDescriptions);
        IIpsElement[] children = ipsObjectPartContainer.getChildren();
        for (IIpsElement ipsElement : children) {
            if (ipsElement instanceof IIpsObjectPartContainer) {
                addLabelsAndDescriptions((IIpsObjectPartContainer)ipsElement, labelsAndDescriptions);
            }
        }
    }

    /** for testing only **/
    void addLabelAndDescription(IIpsObjectPartContainer ipsObjectPart) {
        addLabelAndDescription(ipsObjectPart, getLocalizedProperties());
    }

    private void addLabelAndDescription(IIpsObjectPartContainer ipsObjectPart,
            LabelAndDescriptionProperties labelsAndDescriptions) {
        if (ipsObjectPart instanceof ILabeledElement labeledElement) {
            String label = labeledElement.getLabelValue(getLocale());
            if (IpsStringUtils.isNotBlank(label)) {
                labelsAndDescriptions.put(ipsObjectPart, DocumentationKind.LABEL, label);
            }
            if (labeledElement.isPluralLabelSupported()) {
                String pluralLabelValue = labeledElement.getPluralLabelValue(getLocale());
                if (IpsStringUtils.isNotBlank(pluralLabelValue)) {
                    labelsAndDescriptions.put(ipsObjectPart, DocumentationKind.PLURAL_LABEL, pluralLabelValue);
                }
            }
        }
        if (ipsObjectPart instanceof IDescribedElement) {
            IDescription description = ((IDescribedElement)ipsObjectPart).getDescription(getLocale());
            if (description != null) {
                String text = description.getText();
                if (IpsStringUtils.isNotBlank(text)) {
                    if (Abstractions.getWorkspace().isWindows()) {
                        text = text.replace("\r\n", "\n");
                    }
                    labelsAndDescriptions.put(ipsObjectPart, DocumentationKind.DESCRIPTION, text);
                }
            }
        }
    }

    @Override
    public LabelAndDescriptionProperties getLocalizedProperties() {
        return (LabelAndDescriptionProperties)super.getLocalizedProperties();
    }

    public void generate(IIpsObject ipsObject) {
        deleteMessagesForDeletedParts(ipsObject.getQualifiedNameType(), createLocalizedProperties(ipsObject));
        addLabelsAndDescriptions(ipsObject, getLocalizedProperties());
    }

    protected LabelAndDescriptionProperties createLocalizedProperties(IIpsObject ipsObject) {
        LabelAndDescriptionProperties tmpLabelsAndDescriptions = new LabelAndDescriptionProperties(
                getSupportedLanguage().isDefaultLanguage(), DefaultLineSeparator.of(ipsObject));
        addLabelsAndDescriptions(ipsObject, tmpLabelsAndDescriptions);
        return tmpLabelsAndDescriptions;
    }

}
