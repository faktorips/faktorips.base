/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.rule;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;

/**
 * A dialog that enables the user to select {@link IValidationRule validation rules} to overwrite.
 */
public class OverrideRuleDialog extends SelectSupertypeHierarchyPartsDialog<IValidationRule> {

    /**
     * @param policyCmptType The {@link IPolicyCmptType policy component type} to get the candidates to possibly overwrite from.
     * @param parent The {@link Shell} to show this dialog in.
     */
    public OverrideRuleDialog(IPolicyCmptType policyCmptType, Shell parent) {
        super(parent, new CandidatesContentProvider(policyCmptType));
        setTitle(Messages.OverrideValidationRuleDialog_title);
        setEmptyListMessage(Messages.OverrideValidationRuleDialog_labelNoAttributes);
        setSelectLabelText(Messages.OverrideValidationRuleDialog_labelSelectAttribute);
    }

    /** Provides the {@link IValidationRule validation rules} available for selection. */
    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        /**
         * @param policyCmptType The {@link IPolicyCmptType policy component type} the {@link IValidationRule validation rules} available for
         *            selection belong to.
         */
        public CandidatesContentProvider(IPolicyCmptType policyCmptType) {
            super(policyCmptType);
        }

        @Override
        public List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            return policyCmptType.findOverrideValidationRuleCandidates(policyCmptType.getIpsProject());
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            return policyCmptType.getSupertypeHierarchy().getAllSupertypes(policyCmptType);
        }
    }
}
