/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.util.StringUtil;

/**
 * Delta entry for a link where the target runtime ID does not match the product component
 * identified by the target qualified name.
 *
 * The fix replaces the target runtime ID with the target's current runtime ID.
 *
 * @since 23.6
 */
public class WrongRuntimeIdForLinkEntry extends AbstractDeltaEntryForLinks {

    public WrongRuntimeIdForLinkEntry(IProductCmptLink linkWithWrongTargetRuntimeId) {
        super(linkWithWrongTargetRuntimeId);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_TARGET_RUNTIME_ID_MISMATCH;
    }

    @Override
    public String getDescription() {
        String linkCaption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getLink());
        String linkTarget = StringUtil.unqualifiedName(getLink().getTarget());
        return MessageFormat.format(Messages.WrongRuntimeIdForLinkEntry_Description, linkCaption,
                linkTarget);
    }

    /**
     * Set the current runtime ID.
     */
    @Override
    public void fix() {
        getLink().setTargetRuntimeId(getLink().findTarget(getLink().getIpsProject()).getRuntimeId());
    }

}
