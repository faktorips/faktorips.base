/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;

/**
 * A dialog that enables the user to select <tt>IPolicyCmptTypeAttribute</tt>s to overwrite.
 * 
 * @author Alexander Weickmann
 */
public class OverrideAttributeDialog extends SelectSupertypeHierarchyPartsDialog {

    /**
     * @param policyCmptType The <tt>IPolicyCmptType</tt> to get the candidates to possibly
     *            overwrite from.
     * @param parent The <tt>Shell</tt> to show this dialog in.
     */
    public OverrideAttributeDialog(IPolicyCmptType policyCmptType, Shell parent) {
        super(parent, new CandidatesContentProvider(policyCmptType));
        setTitle(Messages.OverrideAttributeDialog_title);
        setEmptyListMessage(Messages.OverrideAttributeDialog_labelNoAttributes);
        setSelectLabelText(Messages.OverrideAttributeDialog_labelSelectAttribute);
    }

    /** Returns the <tt>IPolicyCmptTypeAttribute</tt>s the user has selected to override. */
    public IPolicyCmptTypeAttribute[] getSelectedAttributes() {
        List<IPolicyCmptTypeAttribute> attributes = new ArrayList<IPolicyCmptTypeAttribute>();
        Object[] checked = getResult();
        for (Object element : checked) {
            if (element instanceof IPolicyCmptTypeAttribute) {
                IPolicyCmptTypeAttribute attr = (IPolicyCmptTypeAttribute)element;
                attributes.add(attr);
            }
        }
        return attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
    }

    /** Provides the <tt>IPolicyCmptTypeAttribute</tt>s available for selection. */
    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        /**
         * @param policyCmptType The <tt>IPolicyCmptType</tt> the <tt>IPolicyCmptTypeAttribute</tt>s
         *            available for selection belong to.
         */
        public CandidatesContentProvider(IPolicyCmptType policyCmptType) {
            super(policyCmptType);
        }

        @Override
        public IIpsObjectPart[] getAvailableParts(IIpsObject ipsObject) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            try {
                return policyCmptType.findOverrideAttributeCandidates(policyCmptType.getIpsProject());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected IIpsObject[] getSupertypes(IIpsObject ipsObject) throws CoreException {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            return policyCmptType.getSupertypeHierarchy().getAllSupertypes(policyCmptType);
        }

    }

}
