/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
