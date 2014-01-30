/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * @deprecated Since 2.0 you should better use the {@link IpsPartEditDialog2} because it uses the
 *             {@link BindingContext} instead of the deprecated {@link IpsObjectUIController} for
 *             data binding.
 * 
 * @author dirmeier
 */
@Deprecated
public abstract class IpsPartEditDialog extends EditDialog {
    private static final String SETTINGS_X = "XPos"; //$NON-NLS-1$
    private static final String SETTINGS_Y = "YPos"; //$NON-NLS-1$
    private static final String SETTINGS_WIDTH = "Width"; //$NON-NLS-1$
    private static final String SETTINGS_HEIGHT = "Height"; //$NON-NLS-1$

    protected IpsObjectUIController uiController;

    private Memento oldState;

    private IIpsObjectPart part;

    private boolean dirty = false;

    private boolean descriptionEnabled = true;

    private boolean dialogSizePersistence = false;
    private String dialogSizeSettingsKey;
    private Point initialDialogSize;
    private Point initialDialogPosition;

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
            if (part instanceof IDescribedElement && part instanceof ILabeledElement) {
                createLabelAndDescriptionTabItem(tabFolder);
            } else if (part instanceof IDescribedElement) {
                createDescriptionTabItem(tabFolder);
            } else if (part instanceof ILabeledElement) {
                createLabelTabItem(tabFolder);
            }
        }
        return composite;
    }

    protected abstract Composite createWorkAreaThis(Composite parent);

    private TabItem createLabelAndDescriptionTabItem(TabFolder folder) {
        Composite composite = getToolkit().createGridComposite(folder, 1, true, true);
        Group labelGroup = getToolkit().createGroup(composite, Messages.IpsPartEditDialog_groupLabel);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.minimumHeight = 120;
        labelGroup.setLayoutData(layoutData);
        new LabelEditComposite(labelGroup, (ILabeledElement)part, getToolkit());
        Group descriptionGroup = getToolkit().createGroup(composite, Messages.IpsPartEditDialog_groupDescription);
        new DescriptionEditComposite(descriptionGroup, (IDescribedElement)part, getToolkit());

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.IpsPartEditDialog_tabItemLabelAndDescription);
        item.setControl(composite);

        return item;
    }

    private TabItem createLabelTabItem(TabFolder folder) {
        Composite editComposite = new LabelEditComposite(folder, (ILabeledElement)part, getToolkit());

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.IpsPartEditDialog_tabItemLabel);
        item.setControl(editComposite);

        return item;
    }

    private TabItem createDescriptionTabItem(TabFolder folder) {
        IDescribedElement describedElement = (IDescribedElement)part;
        DescriptionEditComposite descriptionEditComposite = new DescriptionEditComposite(folder, describedElement,
                getToolkit());
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

    public boolean isDialogSizePersistent() {
        return dialogSizePersistence;
    }

    protected void enableDialogSizePersistence(String settingsPrefix,
            String settingsDisambiguator,
            Point initialSize,
            Point initialPosition) {
        dialogSizePersistence = true;
        dialogSizeSettingsKey = settingsPrefix + (settingsDisambiguator == null ? "" : settingsDisambiguator); //$NON-NLS-1$
        initialDialogSize = initialSize;
        initialDialogPosition = initialPosition;
    }

    private void storePosition(Shell shell) {
        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection(dialogSizeSettingsKey);
        if (settings == null) {
            settings = IpsPlugin.getDefault().getDialogSettings().addNewSection(dialogSizeSettingsKey);
        }
        settings.put(SETTINGS_X, shell.getLocation().x);
        settings.put(SETTINGS_Y, shell.getLocation().y);
        settings.put(SETTINGS_WIDTH, shell.getSize().x);
        settings.put(SETTINGS_HEIGHT, shell.getSize().y);
    }

    protected void configurePosition(IDialogSettings settings, Shell shell) {
        int posX, posY, width, height;

        if (settings == null) {
            Composite parent = shell.getParent();
            Rectangle bounds = null;
            if (parent == null) {
                bounds = shell.getDisplay().getPrimaryMonitor().getBounds();
            } else {
                bounds = parent.getBounds();
            }
            width = initialDialogSize.x;
            height = initialDialogSize.y;
            if (initialDialogPosition == null) {
                posX = bounds.x + (bounds.width - width) / 2;
                posY = bounds.y + (bounds.height - height) / 2;
            } else {
                posX = initialDialogPosition.x;
                posY = initialDialogPosition.y;
            }
            IDialogSettings newSection = IpsPlugin.getDefault().getDialogSettings()
                    .addNewSection(dialogSizeSettingsKey);
            newSection.put(SETTINGS_X, posX);
            newSection.put(SETTINGS_Y, posY);
            newSection.put(SETTINGS_WIDTH, width);
            newSection.put(SETTINGS_HEIGHT, height);
        } else {
            posX = settings.getInt(SETTINGS_X);
            posY = settings.getInt(SETTINGS_Y);
            width = settings.getInt(SETTINGS_WIDTH);
            height = settings.getInt(SETTINGS_HEIGHT);
        }
        shell.setBounds(posX, posY, width, height);
    }

    @Override
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        if (dialogSizePersistence) {
            shell.addListener(SWT.Move, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    storePosition(shell);
                }
            });
            shell.addListener(SWT.Resize, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    storePosition(shell);
                }
            });
            IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection(dialogSizeSettingsKey);
            configurePosition(settings, shell);
        }
    }
}
