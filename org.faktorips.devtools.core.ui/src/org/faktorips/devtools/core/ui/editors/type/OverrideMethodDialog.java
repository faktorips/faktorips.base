/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.editors.SelectSupertypeHierarchyPartsDialog;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;

/**
 * A dialog that enables the user to select {@link IMethod}s to overwrite.
 * 
 * @author Alexander Weickmann
 */
public class OverrideMethodDialog extends SelectSupertypeHierarchyPartsDialog {

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
        try {
            List<IMethod> selected = new ArrayList<IMethod>();
            List<IMethod> method = type.findOverrideMethodCandidates(false, type.getIpsProject());
            for (IMethod element : method) {
                if (element.isAbstract()) {
                    selected.add(element);
                }
            }
            setInitialElementSelections(selected);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the {@link IMethod}s the user has selected to override.
     */
    public List<IMethod> getSelectedMethods() {
        List<IMethod> methods = new ArrayList<IMethod>();
        Object[] checked = getResult();
        for (Object element : checked) {
            if (element instanceof IMethod) {
                methods.add((IMethod)element);
            }
        }
        return methods;
    }

    private static class CandidatesContentProvider extends SupertypeHierarchyPartsContentProvider {

        public CandidatesContentProvider(IType type) {
            super(type);
        }

        @Override
        protected List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject) {
            IType type = (IType)ipsObject;
            try {
                return type.findOverrideMethodCandidates(false, type.getIpsProject());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject) throws CoreException {
            IType type = (IType)ipsObject;
            SupertypesCollector collector = new SupertypesCollector(type.getIpsProject());
            try {
                collector.start(type.findSupertype(type.getIpsProject()));
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return collector.supertypes;
        }

        private static class SupertypesCollector extends TypeHierarchyVisitor<IType> {

            private List<IType> supertypes = new ArrayList<IType>();

            public SupertypesCollector(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IType currentType) throws CoreException {
                supertypes.add(currentType);
                return true;
            }

        }

    }

}
