/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class NewProdutCmptValidator {

    public static final String MSGCODE_PREFIX = "NEW_PRODUCT_CMPT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_INVALID_PROJECT = MSGCODE_PREFIX + "invalidProject"; //$NON-NLS-1$

    public static final String MSG_INVALID_BASE_TYPE = MSGCODE_PREFIX + "invalidBaseType"; //$NON-NLS-1$

    public static final String MSG_INVALID_SELECTED_TYPE = MSGCODE_PREFIX + "invalidSelectedType"; //$NON-NLS-1$

    public static final String MSG_EMPTY_KIND_ID = MSGCODE_PREFIX + "emptyKindId"; //$NON-NLS-1$

    public static final String MSG_INVALID_KIND_ID = MSGCODE_PREFIX + "invalidKindId"; //$NON-NLS-1$

    public static final String MSG_EMPTY_VERSION_ID = MSGCODE_PREFIX + "emptyVersionId"; //$NON-NLS-1$

    public static final String MSG_INVALID_VERSION_ID = MSGCODE_PREFIX + "invalidVersionId"; //$NON-NLS-1$

    public static final String MSG_INVALID_PACKAGE_ROOT = MSGCODE_PREFIX + "invalidPackageRoot";

    public static final String MSG_INVALID_PACKAGE = MSGCODE_PREFIX + "invalidPackage";

    public static final String MSG_SRC_FILE_EXISTS = MSGCODE_PREFIX + "sourceFileExists";

    public static final String MSG_INVALID_FULL_NAME = MSGCODE_PREFIX + "invalidFullName";

    private final NewProductCmptPMO pmo;

    public NewProdutCmptValidator(NewProductCmptPMO pmo) {
        this.pmo = pmo;
    }

    public MessageList validateTypeSelection() {
        MessageList result = new MessageList();

        if (pmo.getIpsProject() == null || !pmo.getIpsProject().isProductDefinitionProject()) {
            result.add(new Message(MSG_INVALID_PROJECT, "Please select a product definition project", Message.ERROR,
                    pmo, NewProductCmptPMO.PROPERTY_IPS_PROJECT));
        }

        if (pmo.getSelectedBaseType() == null) {
            result.add(new Message(MSG_INVALID_BASE_TYPE,
                    "Please select the type of product component you want to create", Message.ERROR, pmo,
                    NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE));
        }

        return result;
    }

    public MessageList validateProductCmptPage() {
        MessageList result = new MessageList();
        if (validateTypeSelection().containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (pmo.getSelectedType() == null) {
            result.add(new Message(MSG_INVALID_SELECTED_TYPE,
                    "Please select the type of product component you want to create", Message.ERROR, pmo,
                    NewProductCmptPMO.PROPERTY_SELECTED_TYPE));
        }

        if (StringUtils.isEmpty(pmo.getKindId())) {
            result.add(new Message(MSG_EMPTY_KIND_ID, "Please insert a name for the new product component",
                    Message.ERROR, pmo, NewProductCmptPMO.PROPERTY_KIND_ID));
        }
        if (pmo.isNeedVersionId() && StringUtils.isEmpty(pmo.getVersionId())) {
            result.add(new Message(MSG_EMPTY_VERSION_ID, "Please insert a correct version id", Message.ERROR, pmo,
                    NewProductCmptPMO.PROPERTY_VERSION_ID));
        }
        if (pmo.getKindId() != null && (!pmo.isNeedVersionId() || pmo.getVersionId() != null)) {
            IProductCmptNamingStrategy productCmptNamingStrategy = pmo.getIpsProject().getProductCmptNamingStrategy();
            try {
                if (!pmo.getKindId().equals(productCmptNamingStrategy.getKindId(pmo.getFullName()))) {
                    result.add(new Message(MSG_INVALID_KIND_ID, "Please insert a correct kind id", Message.ERROR, pmo,
                            NewProductCmptPMO.PROPERTY_KIND_ID));
                }
                if (pmo.isNeedVersionId()
                        && !pmo.getVersionId().equals(productCmptNamingStrategy.getVersionId(pmo.getFullName()))) {
                    result.add(new Message(MSG_INVALID_VERSION_ID, "Please insert a correct version id", Message.ERROR,
                            pmo, NewProductCmptPMO.PROPERTY_VERSION_ID));
                }
            } catch (IllegalArgumentException e) {
                result.add(new Message(MSG_INVALID_FULL_NAME, "Please insert a valid name", Message.ERROR, pmo,
                        NewProductCmptPMO.PROPERTY_VERSION_ID, NewProductCmptPMO.PROPERTY_KIND_ID));
            }
        }

        if (result.isEmpty()) {
            IIpsProjectNamingConventions namingConventions = pmo.getIpsProject().getNamingConventions();
            try {
                result.add(namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.PRODUCT_CMPT,
                        pmo.getFullName()));
                IIpsSrcFile file = pmo.getIpsProject().findIpsSrcFile(IpsObjectType.PRODUCT_CMPT,
                        pmo.getQualifiedName());
                if (file != null) {
                    result.add(new Message(MSG_SRC_FILE_EXISTS,
                            "A product component with this name does already exist.", Message.ERROR));
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        return result;
    }

    public MessageList validateFolderAndPackage() {
        MessageList result = new MessageList();
        if (validateTypeSelection().containsErrorMsg()) {
            // validation only makes sense if there are no error on type selection page.
            return result;
        }

        if (pmo.getPackageRoot() == null) {
            result.add(new Message(MSG_INVALID_PACKAGE_ROOT, "No valid root folder is selected", Message.ERROR, pmo,
                    NewProductCmptPMO.PROPERTY_PACKAGE_ROOT));
        }

        if (pmo.getIpsPackage() == null || !pmo.getIpsPackage().getRoot().equals(pmo.getPackageRoot())) {
            result.add(new Message(MSG_INVALID_PACKAGE, "Please specify a valid package", Message.ERROR, pmo,
                    NewProductCmptPMO.PROPERTY_IPS_PACKAGE));
        }

        return result;
    }

    public MessageList validateAll() {
        MessageList result = validateTypeSelection();
        result.add(validateProductCmptPage());
        result.add(validateFolderAndPackage());
        return result;
    }

}
