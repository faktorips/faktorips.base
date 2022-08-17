/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;

/**
 * A dialog that enables the user to select <code>IAttribute</code>s to overwrite.
 * 
 * @author Alexander Weickmann
 */
public class OverrideAttributeDialog extends SelectSupertypeHierarchyPartsDialog<IAttribute> {

    /**
     * @param cmptType The <code>cmptType</code> to get the candidates to possibly overwrite from.
     * @param parent The <code>Shell</code> to show this dialog in.
     */
    public OverrideAttributeDialog(IType cmptType, Shell parent) {
        super(parent, new CandidatesContentProvider(cmptType));
        setTitle(Messages.OverrideAttributeDialog_title);
        setEmptyListMessage(Messages.OverrideAttributeDialog_labelNoAttributes);
        setSelectLabelText(Messages.OverrideAttributeDialog_labelSelectAttribute);
    }

    /** Provides the <code>IAttribute</code>s available for selection. */
    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        /**
         * @param cmptType The <code>cmptType</code> the <code>IAttribute</code>s available for
         *            selection belong to.
         */
        public CandidatesContentProvider(IType cmptType) {
            super(cmptType);
        }

        @Override
        public List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
            IType cmptType = (IType)ipsObject;
            return cmptType.findOverrideAttributeCandidates(cmptType.getIpsProject());
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) {
            IType cmptType = (IType)ipsObject;
            return cmptType.getSupertypeHierarchy().getAllSupertypes(cmptType);
        }

    }

}
