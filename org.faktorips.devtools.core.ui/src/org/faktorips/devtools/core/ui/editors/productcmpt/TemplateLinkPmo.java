/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TemplateLinkPmo extends AbstractTemplateValuePmo<IProductCmptLink> {

    public static final String PROPERTY_STATUS_BUTTON_ENABLED = "statusButtonEnabled"; //$NON-NLS-1$

    @Override
    public TemplateValueUiStatus getTemplateValueStatus() {
        if (isLinkAvailable()) {
            return TemplateValueUiStatus.mapStatus(getTemplatedProperty());
        } else {
            return TemplateValueUiStatus.UNDEFINED;
        }
    }

    private boolean isLinkAvailable() {
        return getTemplatedProperty() != null;
    }

    @Override
    public String getToolTipText() {
        if (!isLinkAvailable()) {
            return null;
        }
        switch (getTemplateValueStatus()) {
            case INHERITED:
                return NLS.bind(Messages.TemplateLinkPmo_Status_Inherited, getLinkLabel(), getTemplateName());
            case OVERWRITE:
                return NLS.bind(Messages.TemplateLinkPmo_Status_Override, getLinkLabel(), getTemplateName());
            case OVERWRITE_EQUAL:
                return NLS.bind(Messages.TemplateLinkPmo_Status_OverrideEqual, getTemplateName());
            case UNDEFINED:
                return NLS.bind(Messages.TemplateLinkPmo_Status_Delete, getLinkLabel(), getTemplateName());
            case NEWLY_DEFINED:
                return Messages.TemplateLinkPmo_Status_NewlyDefined;
            default:
                return StringUtils.EMPTY;
        }
    }

    private String getLinkLabel() {
        if (isLinkAvailable()) {
            return IpsUIPlugin.getLabel(getTemplatedProperty());
        }
        return StringUtils.EMPTY;
    }

    public boolean isStatusButtonEnabled() {
        return isLinkAvailable();
    }

    public void setLink(IProductCmptLink link) {
        setTemplatedProperty(link);
    }

}