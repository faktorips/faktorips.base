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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * A dialog that enables the user to select {@link IMethod}s to overwrite.
 * 
 * @author Alexander Weickmann
 */
public class OverrideMethodDialog extends SelectSupertypeHierarchyPartsDialog<IMethod> {

    /**
     * @param type The type to get the candidates for overwriting from
     * @param parent The shell to show this dialog in
     */
    public OverrideMethodDialog(IType type, Shell parent) {
        super(parent, new CandidatesContentProvider(type));
        setTitle(Messages.OverrideMethodDialog_title);
        setEmptyListMessage(Messages.OverrideMethodDialog_msgEmpty);
        setSelectLabelText(Messages.OverrideMethodDialog_labelSelectMethods);
        selectAbstractMethods(type);
    }

    private void selectAbstractMethods(IType type) {
        List<IMethod> selected = new ArrayList<>();
        List<IMethod> method = type.findOverrideMethodCandidates(false, type.getIpsProject());
        for (IMethod element : method) {
            if (element.isAbstract()) {
                selected.add(element);
            }
        }
        setInitialElementSelections(selected);
    }

    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        public CandidatesContentProvider(IType type) {
            super(type);
        }

        @Override
        protected List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
            IType type = (IType)ipsObject;
            return type.findOverrideMethodCandidates(false, type.getIpsProject());
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) {
            IType type = (IType)ipsObject;
            SupertypesCollector collector = new SupertypesCollector(type.getIpsProject());
            collector.start(type.findSupertype(type.getIpsProject()));
            return collector.supertypes;
        }

        private static class SupertypesCollector extends TypeHierarchyVisitor<IType> {

            private List<IType> supertypes = new ArrayList<>();

            public SupertypesCollector(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IType currentType) {
                supertypes.add(currentType);
                return true;
            }

        }

    }

}
