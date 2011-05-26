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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section to display and edit a type's methods.
 */
public class MethodsSection extends SimpleIpsPartsSection {

    private MethodsComposite methodsComposite;

    public MethodsSection(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(type, parent, site, ExpandableComposite.TITLE_BAR, Messages.MethodsSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        methodsComposite = new MethodsComposite(getType(), parent, toolkit);
        return methodsComposite;
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
        methodsComposite.updateOverrideButtonEnabledState();
    }

    protected EditDialog createEditDialog(IMethod part, Shell shell) {
        return new MethodEditDialog(part, shell);
    }

    protected IType getType() {
        return (IType)getIpsObject();
    }

    /**
     * A composite that shows a type's methods in a viewer and allows to edit methods in a dialog,
     * create new methods and delete methods.
     */
    private class MethodsComposite extends IpsPartsComposite {

        private Button overrideButton;

        private MethodsComposite(IType type, Composite parent, UIToolkit toolkit) {
            super(type, parent, getSite(), true, true, true, true, true, false, true, toolkit);
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            overrideButton = toolkit.createButton(buttons, Messages.MethodsSection_button);
            overrideButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            overrideButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    overrideClicked();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Nothing to do
                }
            });
            updateOverrideButtonEnabledState();
            return true;
        }

        private void updateOverrideButtonEnabledState() {
            try {
                boolean supertypeExisting = getType().hasExistingSupertype(getType().getIpsProject());
                overrideButton.setEnabled(supertypeExisting);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        private void overrideClicked() {
            OverrideMethodDialog dialog = new OverrideMethodDialog(getType(), getShell());
            if (dialog.open() == Window.OK) {
                getType().overrideMethods(dialog.getSelectedParts());
            }
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getType().newMethod();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return MethodsSection.this.createEditDialog((IMethod)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveMethods(indexes, up);
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            overrideButton.setEnabled(flag);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new MethodContentProvider();
        }

        private class MethodContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return getType().getMethods().toArray();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        }

    }
}
