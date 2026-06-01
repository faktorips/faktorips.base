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

import org.faktorips.devtools.model.internal.productcmpt.PolicyCmptLinkCardinality;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;

/**
 * Delta entry for a {@link IPolicyCmptLinkCardinality} that should not be there because its
 * referenced of a {@link IPolicyCmptTypeAssociation} is missing or not configurable in the current
 * product component (generation).
 *
 * @since 26.7
 */
public class PolicyCmptLinkCardinalityNotFoundInModelEntry implements IDeltaEntry {

    private final IPolicyCmptLinkCardinality policyCmptLinkCardinality;

    public PolicyCmptLinkCardinalityNotFoundInModelEntry(IPolicyCmptLinkCardinality policyCmptLinkCardinality) {
        this.policyCmptLinkCardinality = policyCmptLinkCardinality;
    }

    @Override
    public Class<PolicyCmptLinkCardinality> getPartType() {
        return PolicyCmptLinkCardinality.class;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.POLICY_CMPT_LINK_CARDINALITY_NOT_FOUND_IN_MODEL;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(Messages.PolicyCmptLinkCardinalityNotFoundInModelEntry_fixMessage,
                policyCmptLinkCardinality.getAssociation());
    }

    @Override
    public void fix() {
        policyCmptLinkCardinality.delete();
    }

}
