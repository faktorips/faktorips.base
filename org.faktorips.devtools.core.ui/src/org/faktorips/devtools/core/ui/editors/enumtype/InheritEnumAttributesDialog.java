/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;

/**
 * Dialog that enables the user to select <tt>IEnumAttribute</tt>s to inherit.
 * 
 * @author Alexander Weickmann
 */
public class InheritEnumAttributesDialog extends SelectSupertypeHierarchyPartsDialog<IEnumAttribute> {

    /**
     * @param enumType The <tt>IEnumType</tt> to get the candidates to possibly inherit from.
     * @param parent The <tt>Shell</tt> to show this dialog in.
     */
    public InheritEnumAttributesDialog(IEnumType enumType, Shell parent) {
        super(parent, new CandidatesContentProvider(enumType));
        setTitle(Messages.InheritAttributesDialog_title);
        setEmptyListMessage(Messages.InheritAttributesDialog_labelNoAttributes);
        setSelectLabelText(Messages.InheritAttributesDialog_labelSelectAttribute);
    }

    /** Provides the <tt>IEnumAttribute</tt>s available for selection. */
    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        /**
         * @param enumType The <tt>IEnumType</tt> the <tt>IEnumAttribute</tt>s available for
         *            selection belong to.
         */
        public CandidatesContentProvider(IEnumType enumType) {
            super(enumType);
        }

        @Override
        protected List<IEnumAttribute> getAvailableParts(IIpsObject ipsObject) {
            IEnumType enumType = (IEnumType)ipsObject;
            try {
                List<IEnumAttribute> candiates = enumType.findInheritEnumAttributeCandidates(enumType.getIpsProject());
                return candiates;
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected List<IEnumType> getSupertypes(IIpsObject ipsObject) throws CoreException {
            IEnumType enumType = (IEnumType)ipsObject;
            List<IEnumType> supertypes = enumType.findAllSuperEnumTypes(enumType.getIpsProject());
            return supertypes;
        }

    }

}
