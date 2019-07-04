/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.labels;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractPropertiesGenerator;
import org.faktorips.runtime.model.type.DocumentationKind;

public class LabelAndDescriptionGenerator extends AbstractPropertiesGenerator {

    public LabelAndDescriptionGenerator(IFile messagesPropertiesFile, ISupportedLanguage supportedLanguage,
            AbstractLocalizedPropertiesBuilder labelAndDescriptionPropertiesBuilder) {
        super(messagesPropertiesFile, supportedLanguage, labelAndDescriptionPropertiesBuilder,
                new LabelAndDescriptionProperties(supportedLanguage.isDefaultLanguage()));
    }

    void addLabelsAndDescriptions(IIpsObjectPartContainer ipsObjectPartContainer,
            LabelAndDescriptionProperties labelsAndDescriptions) {
        addLabelAndDescription(ipsObjectPartContainer, labelsAndDescriptions);
        try {
            IIpsElement[] children = ipsObjectPartContainer.getChildren();
            for (IIpsElement ipsElement : children) {
                if (ipsElement instanceof IIpsObjectPartContainer) {
                    addLabelsAndDescriptions((IIpsObjectPartContainer)ipsElement, labelsAndDescriptions);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /** for testing only **/
    void addLabelAndDescription(IIpsObjectPartContainer ipsObjectPart) {
        addLabelAndDescription(ipsObjectPart, getLocalizedProperties());
    }

    private void addLabelAndDescription(IIpsObjectPartContainer ipsObjectPart,
            LabelAndDescriptionProperties labelsAndDescriptions) {
        if (ipsObjectPart instanceof ILabeledElement) {
            ILabeledElement labeledElement = (ILabeledElement)ipsObjectPart;
            String label = labeledElement.getLabelValue(getLocale());
            if (StringUtils.isNotBlank(label)) {
                labelsAndDescriptions.put(ipsObjectPart, DocumentationKind.LABEL, label);
            }
            if (labeledElement.isPluralLabelSupported()) {
                String pluralLabelValue = labeledElement.getPluralLabelValue(getLocale());
                if (StringUtils.isNotBlank(pluralLabelValue)) {
                    labelsAndDescriptions.put(ipsObjectPart, DocumentationKind.PLURAL_LABEL, pluralLabelValue);
                }
            }
        }
        if (ipsObjectPart instanceof IDescribedElement) {
            IDescription description = ((IDescribedElement)ipsObjectPart).getDescription(getLocale());
            if (description != null && StringUtils.isNotBlank(description.getText())) {
                labelsAndDescriptions.put(ipsObjectPart, DocumentationKind.DESCRIPTION, description.getText());
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
                getSupportedLanguage().isDefaultLanguage());
        addLabelsAndDescriptions(ipsObject, tmpLabelsAndDescriptions);
        return tmpLabelsAndDescriptions;
    }

}
