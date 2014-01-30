/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;

/**
 * A dialog that enables the user to select <tt>IAttribute</tt>s to overwrite.
 * 
 * @author Alexander Weickmann
 */
public class OverrideAttributeDialog extends SelectSupertypeHierarchyPartsDialog<IAttribute> {

    /**
     * @param cmptType The <tt>cmptType</tt> to get the candidates to possibly overwrite from.
     * @param parent The <tt>Shell</tt> to show this dialog in.
     */
    public OverrideAttributeDialog(IType cmptType, Shell parent) {
        super(parent, new CandidatesContentProvider(cmptType));
        setTitle(Messages.OverrideAttributeDialog_title);
        setEmptyListMessage(Messages.OverrideAttributeDialog_labelNoAttributes);
        setSelectLabelText(Messages.OverrideAttributeDialog_labelSelectAttribute);
    }

    /** Provides the <tt>IAttribute</tt>s available for selection. */
    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        /**
         * @param cmptType The <tt>cmptType</tt> the <tt>IAttribute</tt>s available for selection
         *            belong to.
         */
        public CandidatesContentProvider(IType cmptType) {
            super(cmptType);
        }

        @Override
        public List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
            IType cmptType = (IType)ipsObject;
            try {
                return cmptType.findOverrideAttributeCandidates(cmptType.getIpsProject());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) throws CoreException {
            IType cmptType = (IType)ipsObject;
            return cmptType.getSupertypeHierarchy().getAllSupertypes(cmptType);
        }

    }

}
