/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;

/**
 * Dialog that enables the user to select <code>IEnumAttribute</code>s to inherit.
 * 
 * @author Alexander Weickmann
 */
public class InheritEnumAttributesDialog extends SelectSupertypeHierarchyPartsDialog<IEnumAttribute> {

    /**
     * @param enumType The <code>IEnumType</code> to get the candidates to possibly inherit from.
     * @param parent The <code>Shell</code> to show this dialog in.
     */
    public InheritEnumAttributesDialog(IEnumType enumType, Shell parent) {
        super(parent, new CandidatesContentProvider(enumType));
        setTitle(Messages.InheritAttributesDialog_title);
        setEmptyListMessage(Messages.InheritAttributesDialog_labelNoAttributes);
        setSelectLabelText(Messages.InheritAttributesDialog_labelSelectAttribute);
    }

    /** Provides the <code>IEnumAttribute</code>s available for selection. */
    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        /**
         * @param enumType The <code>IEnumType</code> the <code>IEnumAttribute</code>s available for
         *            selection belong to.
         */
        public CandidatesContentProvider(IEnumType enumType) {
            super(enumType);
        }

        @Override
        protected List<IEnumAttribute> getAvailableParts(IIpsObject ipsObject) {
            IEnumType enumType = (IEnumType)ipsObject;
            return enumType.findInheritEnumAttributeCandidates(enumType.getIpsProject());
        }

        @Override
        protected List<IEnumType> getSupertypes(IIpsObject ipsObject) {
            IEnumType enumType = (IEnumType)ipsObject;
            return enumType.findAllSuperEnumTypes(enumType.getIpsProject());
        }

    }

}
