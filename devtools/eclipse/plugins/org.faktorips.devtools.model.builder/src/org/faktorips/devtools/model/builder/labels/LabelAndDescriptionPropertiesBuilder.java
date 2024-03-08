/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.labels;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.builder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;

public class LabelAndDescriptionPropertiesBuilder extends AbstractLocalizedPropertiesBuilder {

    private static final String LABEL_AND_DESCRIPTIONS_SUFFIX = "-label-and-descriptions";

    public LabelAndDescriptionPropertiesBuilder(IJavaBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(LabelAndDescriptionPropertiesBuilder.class));
    }

    @Override
    public String getName() {
        return "LabelAndDescriptionPropertiesBuilder";
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        for (ISupportedLanguage supportedLanguage : ipsSrcFile.getIpsProject().getReadOnlyProperties()
                .getSupportedLanguages()) {
            LabelAndDescriptionGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile, supportedLanguage);
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            messagesGenerator.generate(ipsObject);
        }
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        // as these labels and descriptions are used to read model data, product objects are not
        // relevant
        return !ipsSrcFile.getIpsObjectType().isProductDefinitionType();
    }

    @Override
    protected LabelAndDescriptionGenerator getMessagesGenerator(IIpsSrcFile ipsSrcFile,
            ISupportedLanguage supportedLanguage) {
        return (LabelAndDescriptionGenerator)super.getMessagesGenerator(ipsSrcFile, supportedLanguage);
    }

    @Override
    public String getResourceBundleBaseName(IIpsSrcFolderEntry entry) {
        return entry.getUniqueBasePackageNameForDerivedArtifacts() + "."
                + getIpsPackageFragmentRootFolderName(entry)
                + LABEL_AND_DESCRIPTIONS_SUFFIX;
    }

    private String getIpsPackageFragmentRootFolderName(IIpsSrcFolderEntry entry) {
        String packageFragmentRootName = entry.getIpsPackageFragmentRootName().replace("\\", ".").replace("/", ".");
        if (packageFragmentRootName.indexOf(".") > 0) {
            return packageFragmentRootName.substring(packageFragmentRootName.lastIndexOf(".") + 1,
                    packageFragmentRootName.length());
        }
        return packageFragmentRootName;
    }

    @Override
    protected LabelAndDescriptionGenerator createNewMessageGenerator(AFile propertyFile,
            ISupportedLanguage supportedLanguage) {
        return new LabelAndDescriptionGenerator(propertyFile, supportedLanguage, this);
    }

}
