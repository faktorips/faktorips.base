/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.runtime.MessageList;

public class InferTemplateValidator extends NewProductCmptValidator {

    public InferTemplateValidator(NewProductCmptPMO pmo) {
        super(pmo);
    }

    @Override
    public InferTemplatePmo getPmo() {
        return (InferTemplatePmo)super.getPmo();
    }

    @Override
    public MessageList validateProductCmptPage() {
        MessageList msgList = super.validateProductCmptPage();
        GregorianCalendar firstValidFrom = getPmo().getEarliestValidFrom();
        if (getPmo().getEffectiveDate().after(firstValidFrom)) {
            IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
            String formattedValidFrom = prefs.getFormattedDate(firstValidFrom);
            msgList.newError(MSG_INVALID_EFFECTIVE_DATE,
                    NLS.bind(Messages.InferTemplateValidator_error_invalidValidFrom, formattedValidFrom), getPmo(),
                    NewProductCmptPMO.PROPERTY_EFFECTIVE_DATE);
        }
        return msgList;
    }

}
