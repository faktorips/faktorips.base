/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedPropertiesBuilder;

public class ValidationRuleMessagesPropertiesBuilder extends AbstractLocalizedPropertiesBuilder {

    public ValidationRuleMessagesPropertiesBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(ValidationRuleMessagesPropertiesBuilder.class));
    }

    @Override
    public String getName() {
        return "ValidationRuleMessagesPropertiesBuilder";
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        for (ISupportedLanguage supportedLanguage : ipsSrcFile.getIpsProject().getReadOnlyProperties()
                .getSupportedLanguages()) {
            ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile, supportedLanguage);
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            if (ipsObject instanceof IPolicyCmptType) {
                messagesGenerator.generate((IPolicyCmptType)ipsObject);
            }
        }
    }

    @Override
    protected ValidationRuleMessagesGenerator getMessagesGenerator(IIpsSrcFile ipsSrcFile,
            ISupportedLanguage supportedLanguage) {
        return (ValidationRuleMessagesGenerator)super.getMessagesGenerator(ipsSrcFile, supportedLanguage);
    }

    @Override
    protected String getResourceBundleBaseName(IIpsSrcFolderEntry entry) {
        return getBuilderSet().getValidationMessageBundleBaseName(entry);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    protected ValidationRuleMessagesGenerator createNewMessageGenerator(AFile propertyFile,
            ISupportedLanguage supportedLanguage) {
        return new ValidationRuleMessagesGenerator(propertyFile, supportedLanguage, this);
    }

}
