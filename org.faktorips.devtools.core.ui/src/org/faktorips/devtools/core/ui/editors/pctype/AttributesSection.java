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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.RenameHandler;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * A section to display and edit a type's attributes.
 */
public class AttributesSection extends SimpleIpsPartsSection {

    private AttributesComposite attributesComposite;

    public AttributesSection(IpsObjectEditorPage page, IPolicyCmptType pcType, Composite parent, UIToolkit toolkit) {
        super(pcType, parent, Messages.AttributesSection_title, toolkit);
        ArgumentCheck.notNull(page);
        ((AttributesComposite)getPartsComposite()).createContextMenu();
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        attributesComposite = new AttributesComposite(getIpsObject(), parent, toolkit);
        return attributesComposite;
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
        attributesComposite.updateOverrideButtonEnabledState();
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    private class AttributesComposite extends IpsPartsComposite {

        private Button overrideButton;

        public AttributesComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
            addDeleteListener();
        }

        private void addDeleteListener() {
            super.addDeleteListener(new IDeleteListener() {
                @Override
                public boolean aboutToDelete(IIpsObjectPart part) {
                    IValidationRule rule = findValidationRule(part);
                    if (rule == null) {
                        // Nothing to do if no special rule is defined.
                        return true;
                    }
                    String msg = Messages.AttributesSection_deleteMessage;
                    boolean delete = MessageDialog
                            .openQuestion(getShell(), Messages.AttributesSection_deleteTitle, msg);
                    if (delete) {
                        rule.delete();
                    } else if (!delete) {
                        rule.setCheckValueAgainstValueSetRule(false);
                    }
                    return true;
                }

                private IValidationRule findValidationRule(IIpsObjectPart part) {
                    String name = part.getName();
                    IValidationRule[] rules = getPolicyCmptType().getRules();
                    for (int i = 0; i < rules.length; i++) {
                        if (!rules[i].isCheckValueAgainstValueSetRule()) {
                            continue;
                        }
                        String[] attributes = rules[i].getValidatedAttributes();
                        if (attributes.length == 1 && attributes[0].equals(name)) {
                            return rules[i];
                        }
                    }
                    return null;
                }

                @Override
                public void deleted(IIpsObjectPart part) {
                    // Nothing to do.
                }
            });
        }

        private void createContextMenu() {
            MenuManager manager = new MenuManager();
            MenuManager refactorSubmenu = new MenuManager(Messages.AttributesSection_submenuRefactor);

            manager.add(refactorSubmenu);
            manager.add(new Separator());

            refactorSubmenu.add(RenameHandler.getContributionItem());
            // TODO AW: Pull Up not yet working
            // refactorSubmenu.add(new PullUpAction(editorSite.getShell(),
            // getPartsComposite()));
            Menu contextMenu = manager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            overrideButton.setEnabled(flag);
        }

        public IPolicyCmptType getPolicyCmptType() {
            return (IPolicyCmptType)getIpsObject();
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            overrideButton = toolkit.createButton(buttons, Messages.AttributesSection_OverrideButton);
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
                boolean supertypeExisting = getPolicyCmptType().hasExistingSupertype(
                        getPolicyCmptType().getIpsProject());
                overrideButton.setEnabled(supertypeExisting);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        private void overrideClicked() {
            OverrideAttributeDialog dialog = new OverrideAttributeDialog(getPolicyCmptType(), getShell());
            if (dialog.open() == Window.OK) {
                IPolicyCmptTypeAttribute[] attributesToOverwrite = dialog.getSelectedAttributes();
                getPolicyCmptType().overrideAttributes(attributesToOverwrite);
                refresh();
            }
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider();
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getPolicyCmptType().newPolicyCmptTypeAttribute();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPolicyCmptType().moveAttributes(indexes, up);
        }

        private class AttributeContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return getPolicyCmptType().getPolicyCmptTypeAttributes();
            }

            @Override
            public void dispose() {
                // Nothing to do.
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do.
            }

        }

    }

}
