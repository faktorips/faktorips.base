/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enumcontent.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.core.ui.editors.type.TypeEditorStructurePage;

/**
 * The <code>EnumContentPage</code> provides controls to edit the values of an
 * <code>IEnumContent</code> and is intended to be used with the <code>EnumContentEditor</code>.
 * <p>
 * This page is a listener for changes in the ips model: If the enum type the edited enum content is
 * built upon changes the enabled states of the toolbar buttons will be updated.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentPage extends TypeEditorStructurePage implements ContentsChangeListener {

    /** The enum content the enum content editor this page belongs to is currently editing. */
    private IEnumContent enumContent;

    /** The action to open a <code>FixEnumTypeDialog</code>. */
    private IAction openFixEnumTypeDialogAction;

    /** The enum values section of this page. */
    private EnumValuesSection enumValuesSection;

    /**
     * Creates a new <code>EnumContentPage</code>.
     * 
     * @param editor The <code>EnumContentEditor</code> this page belongs to.
     */
    public EnumContentPage(EnumContentEditor editor) {
        super(editor, false, Messages.EnumContentValuesPage_title);

        enumContent = editor.getEnumContent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        createContent(parentContainer, toolkit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        createContent(parentContainer, toolkit);
    }

    /** Creates the content of this page. */
    private void createContent(Composite parentContainer, UIToolkit toolkit) {
        createToolbarActions();
        createToolbar();

        new EnumContentGeneralInfoSection(enumContent, parentContainer, toolkit);

        try {
            enumValuesSection = new EnumValuesSection(enumContent, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /** Creates the actions for the toolbar. */
    private void createToolbarActions() {
        openFixEnumTypeDialogAction = new OpenFixEnumTypeDialogAction(enumContent, getSite().getShell());
    }

    /** Creates the toolbar of this page. */
    private void createToolbar() {
        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(openFixEnumTypeDialogAction);

        form.updateToolBar();
        updateToolbarActionsEnabledStates();
    }

    /**
     * Updates the enabled states of the toolbar.
     * <p>
     * The <code>OpenFixEnumTypeDialogAction</code> will be enabled if the enum type the enum
     * content to edit is built upon does not exist or is missing.
     * <p>
     * The <code>OpenFixColumnsDialogAction</code> will be enabled if there are not exactly as much
     * columns in the enum values table of the <code>EnumValuesSection</code> as enum attributes in
     * the enum type the enum content to be edited is built upon.
     */
    private void updateToolbarActionsEnabledStates() {
        boolean enableOpenFixEnumTypeDialogAction = false;

        String enumTypeQualifiedName = enumContent.getEnumType();
        if (enumTypeQualifiedName.equals("")) {
            enableOpenFixEnumTypeDialogAction = true;
        } else {
            try {
                if (enumContent.findEnumType() == null) {
                    enableOpenFixEnumTypeDialogAction = true;
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        openFixEnumTypeDialogAction.setEnabled(enableOpenFixEnumTypeDialogAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        IIpsSrcFile changedIpsSrcFile = event.getIpsSrcFile();

        // Return if the content changed was not the enum content to be edited
        if (!(changedIpsSrcFile.equals(enumContent.getIpsSrcFile()))) {
            return;
        }

        // Switch based upon event type
        switch (event.getEventType()) {
            case ContentChangeEvent.TYPE_PROPERTY_CHANGED:
                IIpsObject changedIpsObject;
                try {
                    changedIpsObject = changedIpsSrcFile.getIpsObject();
                    if (changedIpsObject != null) {
                        if (changedIpsObject instanceof IEnumContent) {
                            EnumContentPage.this.updateToolbarActionsEnabledStates();
                        }
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }

                break;
        }
    }

}
