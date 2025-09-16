/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.internal.ipsobject;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Validation for {@link IVersionControlledElement IVersionControlledElements} concerning their
 * {@link IDeprecation deprecation}.
 */
public class DeprecationValidation {

    private DeprecationValidation() {
        // util
    }

    public static void validateProductCmptTypeIsNotDeprecated(@CheckForNull IProductCmpt productCmpt,
            String qualifiedName,
            IProductCmptType productCmptType,
            IIpsProject ipsProject,
            MessageList messageList) {
        validateBaseIsNotDeprecated(productCmptType, ipsProject, messageList,
                IProductCmpt.MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE,
                IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE,
                IpsObjectType.PRODUCT_CMPT, productCmpt, qualifiedName);
    }

    public static void validateEnumTypeIsNotDeprecated(@CheckForNull IEnumContent enumContent,
            IEnumType enumType,
            IIpsProject ipsProject,
            MessageList messageList) {
        validateBaseIsNotDeprecated(enumType, ipsProject, messageList, IEnumContent.MSGCODE_DEPRECATED_ENUM_TYPE,
                IEnumContent.PROPERTY_ENUM_TYPE,
                IpsObjectType.ENUM_CONTENT, enumContent, enumType.getEnumContentName());
    }

    public static void validateTableStructureIsNotDeprecated(@CheckForNull ITableContents tableContents,
            String qualifiedName,
            ITableStructure tableStructure,
            IIpsProject ipsProject,
            MessageList messageList) {
        validateBaseIsNotDeprecated(tableStructure, ipsProject, messageList,
                ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE,
                ITableContents.PROPERTY_TABLESTRUCTURE,
                IpsObjectType.TABLE_CONTENTS, tableContents, qualifiedName);
    }

    // CSOFF: ParameterNumber
    private static <T extends IVersionControlledElement & IIpsObject> void validateBaseIsNotDeprecated(
            T type,
            IIpsProject ipsProject,
            MessageList messageList,
            String msgCode,
            String invalidProperty,
            IpsObjectType instanceObjectType,
            IIpsMetaObject instance,
            String instanceQualifiedName) {
        // CSON: ParameterNumber
        if (type.isDeprecated()) {
            String sinceVersionString = type.getDeprecation().getSinceVersionString();
            String descriptionText = type.getDeprecation().getDescriptionText(
                    IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
            if (IpsStringUtils.isBlank(descriptionText)) {
                descriptionText = type.getDeprecation().getDescriptionText(
                        ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale());
            }
            var msgText = MessageFormat.format(Messages.DeprecationValidation_DeprecatedBase,
                    type.getIpsObjectType().getDisplayName(),
                    type.getQualifiedName(),
                    instanceObjectType.getDisplayName(),
                    instanceQualifiedName,
                    (IpsStringUtils.isBlank(sinceVersionString) ? IpsStringUtils.EMPTY
                            : MessageFormat.format(Messages.DeprecationValidation_SinceVersion,
                                    sinceVersionString)),
                    descriptionText);
            var messageBuilder = Message.warning(msgText).code(msgCode);
            if (instance != null) {
                messageBuilder.invalidObjectWithProperties(instance, invalidProperty);
            }
            messageList.add(messageBuilder.create());
        }
    }

}
