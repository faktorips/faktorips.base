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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

public abstract class IpsPartEditDialog extends EditDialog {

    protected IpsObjectUIController uiController;

    private Memento oldState;

    private IIpsObjectPart part;

    private boolean dirty = false;

    private boolean descriptionEnabled;

    public IpsPartEditDialog(IIpsObjectPart part, Shell parentShell, String windowTitle) {
        this(part, parentShell, windowTitle, false);
    }

    public IpsPartEditDialog(IIpsObjectPart part, Shell parentShell, String windowTitle, boolean useTabFolder) {
        super(parentShell, windowTitle, useTabFolder);
        ArgumentCheck.notNull(part, this);
        this.part = part;
        uiController = createUIController(part);
        oldState = part.newMemento();
        dirty = part.getIpsObject().getIpsSrcFile().isDirty();
    }

    // overwritten to be sure to get the cancel-button as soon as possible...
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        super.getButton(Window.CANCEL).addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAbortion();
            }
        });
    }

    @Override
    protected void handleShellCloseEvent() {
        handleAbortion();
        super.handleShellCloseEvent();
    }

    private void handleAbortion() {
        part.setState(oldState);
        if (!dirty) {
            uiController.getIpsObject().getIpsSrcFile().markAsClean();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        connectToModel();
        uiController.updateUI();
        setTitle(buildTitle());
        return control;
    }

    @Override
    protected final Composite createWorkArea(Composite parent) {
        Composite composite = createWorkAreaThis(parent);
        if (isTabFolderUsed()) {
            TabFolder tabFolder = (TabFolder)parent;
            if (part instanceof IDescribedElement) {
                createDescriptionTabItem(tabFolder);
            }
            if (part instanceof ILabeledElement) {
                createLabelTabItem(tabFolder);
            }
        }
        return composite;
    }

    protected abstract Composite createWorkAreaThis(Composite parent);

    private TabItem createLabelTabItem(TabFolder folder) {
        Composite editComposite = new LabelEditComposite(folder, (ILabeledElement)part);

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.IpsPartEditDialog_tabItemLabel);
        item.setControl(editComposite);

        return item;
    }

    private TabItem createDescriptionTabItem(TabFolder folder) {
        IDescribedElement describedElement = (IDescribedElement)part;
        DescriptionEditComposite descriptionEditComposite = new DescriptionEditComposite(folder, describedElement,
                uiToolkit);
        descriptionEditComposite.setViewOnly(!(descriptionEnabled));

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.IpsPartEditDialog_tabItemDescription);
        item.setControl(descriptionEditComposite);

        return item;
    }

    protected IpsObjectUIController createUIController(IIpsObjectPart part) {
        IpsObjectUIController controller = new IpsObjectUIController(part) {
            @Override
            public void valueChanged(FieldValueChangedEvent e) {
                try {
                    super.valueChanged(e);
                    setTitle(buildTitle());
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        };
        return controller;
    }

    /**
     * Returns the part being edited.
     */
    public IIpsObjectPart getIpsPart() {
        return (IIpsObjectPart)uiController.getIpsObjectPartContainer();
    }

    protected String buildTitle() {
        IIpsObjectPart part = getIpsPart();
        if (part.getParent() instanceof IIpsObjectGeneration) {
            return part.getIpsObject().getName() + " " //$NON-NLS-1$
                    + part.getParent().getName() + "." + part.getName(); //$NON-NLS-1$
        }
        return part.getIpsObject().getName() + "." + part.getName(); //$NON-NLS-1$
    }

    protected void connectToModel() {
        // Empty default implementation
    }

    protected void setDescriptionEnabled(boolean descriptionEnabled) {
        this.descriptionEnabled = descriptionEnabled;
    }

}
