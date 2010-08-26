/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;

/**
 * Dialog that enables the user to select <tt>IEnumAttribute</tt>s to inherit.
 * 
 * @author Alexander Weickmann
 */
public class InheritEnumAttributesDialog extends SelectSupertypeHierarchyPartsDialog {

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

    /** Returns the <tt>IEnumAttribute</tt>s the user has selected to inherit. */
    public IEnumAttribute[] getSelectedAttributes() {
        List<IEnumAttribute> attributes = new ArrayList<IEnumAttribute>();
        Object[] checked = getResult();
        for (Object element : checked) {
            if (element instanceof IEnumAttribute) {
                IEnumAttribute attribute = (IEnumAttribute)element;
                attributes.add(attribute);
            }
        }
        return attributes.toArray(new IEnumAttribute[attributes.size()]);
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
        protected IIpsObjectPart[] getAvailableParts(IIpsObject ipsObject) {
            IEnumType enumType = (IEnumType)ipsObject;
            try {
                List<IEnumAttribute> candiates = enumType.findInheritEnumAttributeCandidates(enumType.getIpsProject());
                return candiates.toArray(new IEnumAttribute[candiates.size()]);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected IIpsObject[] getSupertypes(IIpsObject ipsObject) throws CoreException {
            IEnumType enumType = (IEnumType)ipsObject;
            List<IEnumType> supertypes = enumType.findAllSuperEnumTypes(enumType.getIpsProject());
            return supertypes.toArray(new IEnumType[supertypes.size()]);
        }

    }

}
