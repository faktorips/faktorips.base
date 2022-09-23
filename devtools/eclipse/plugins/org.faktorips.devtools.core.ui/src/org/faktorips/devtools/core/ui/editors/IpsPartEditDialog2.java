/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.memento.Memento;

/**
 * Base class for dialogs that allows to edit an ips object part. In contrast to the original
 * <code>IpsPartEditDialog</code> this version uses the new databinding.
 * 
 * @since 2.0
 * 
 * @see BindingContext
 * 
 * @author Jan Ortmann
 */
public abstract class IpsPartEditDialog2 extends EditDialog implements ContentsChangeListener {

    private static final String SETTINGS_X = "XPos"; //$NON-NLS-1$
    private static final String SETTINGS_Y = "YPos"; //$NON-NLS-1$
    private static final String SETTINGS_WIDTH = "Width"; //$NON-NLS-1$
    private static final String SETTINGS_HEIGHT = "Height"; //$NON-NLS-1$

    /**
     * The binding context to use to bind model object properties to ui controls.
     * 
     */
    private BindingContext bindingContext = new BindingContext();

    private IIpsObjectPart part;
    private Memento oldState;
    private boolean dirty = false;
    private boolean dialogSizePersistence = false;
    private String dialogSizeSettingsKey;
    private Point initialDialogSize;
    private Point initialDialogPosition;

    private LabelEditComposite labelEditComposite;

    /**
     * Creates a new <code>IpsPartEditDialog2</code>.
     * 
     * @param part The ips object part to edit.
     * @param windowTitle The window title of the dialog.
     */
    public IpsPartEditDialog2(IIpsObjectPart part, Shell parentShell, String windowTitle) {
        this(part, parentShell, windowTitle, false);
    }

    /**
     * Creates a new <code>IpsPartEditDialog2</code>.
     * 
     * @param part The ips object part to edit.
     * @param windowTitle The window title of the dialog.
     */
    public IpsPartEditDialog2(IIpsObjectPart part, Shell parentShell, String windowTitle, boolean useTabFolder) {
        super(parentShell, windowTitle, useTabFolder);

        this.part = part;
        oldState = part.getIpsObject().newMemento();
        dirty = part.getIpsObject().getIpsSrcFile().isDirty();
        IIpsModel.get().addChangeListener(this);
    }

    /**
     * Returns the binding context to use to bind model object properties to UI controls.
     * 
     * @return the binding context to use to bind model object properties to UI controls
     */
    protected BindingContext getBindingContext() {
        return bindingContext;
    }

    // Overwritten to be sure to get the cancel-button as soon as possible ...
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

    @Override
    public boolean close() {
        if (getBindingContext() != null) {
            getBindingContext().dispose();
        }
        IIpsModel.get().removeChangeListener(this);

        return super.close();
    }

    protected void handleAdditionalCleanupDuringAbortion() {
        // the default implementation does nothing
    }

    private void handleAbortion() {
        // handleAdditionalCleanupDuringAbortion();
        part.getIpsObject().setState(oldState);
        if (!dirty) {
            part.getIpsObject().getIpsSrcFile().markAsClean();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        getBindingContext().updateUI();
        updateTitleInTitleArea();
        // updateMessageArea should be called after the size of the dialog has been calculated -->
        // @see method create() (MTB#142)
        // at least an empty message must be set, otherwise the height of the dialog will be
        // to short (MTB#0291 )
        setMessage(null);
        return control;
    }

    @Override
    public void create() {
        super.create();
        // updateMessageArea have to be called after the size of the dialog is set. (MTB#142)
        updateMessageArea();
    }

    @Override
    protected final Composite createWorkArea(Composite parent) {
        Composite composite = createWorkAreaThis(parent);
        if (isTabFolderUsed() && isContentRequired()) {
            createWorkAreaContent((TabFolder)parent);
        }
        return composite;
    }

    protected abstract Composite createWorkAreaThis(Composite parent);

    private boolean isContentRequired() {
        return part instanceof IVersionControlledElement || part instanceof ILabeledElement
                || part instanceof IDescribedElement;
    }

    private void createWorkAreaContent(TabFolder tabFolder) {
        Composite tabFolderComposite = getToolkit().createGridComposite(tabFolder, 1, true, true);
        createLabelGroupIfRequired(tabFolderComposite);
        createDescriptionGroupIfRequired(tabFolderComposite);
        createVersionGroupIfRequired(tabFolderComposite);
        createDeprecationGroupIfRequired(tabFolderComposite);
        createTabItem(tabFolder, tabFolderComposite);
    }

    private void createLabelGroupIfRequired(Composite composite) {
        if (part instanceof ILabeledElement) {
            labelEditComposite = createLabelGroup(composite, (ILabeledElement)part, getToolkit());
        }
    }

    protected LabelEditComposite createLabelGroup(Composite composite, ILabeledElement part, UIToolkit toolkit) {
        Group labelGroup = toolkit.createGroup(composite, Messages.IpsPartEditDialog_groupLabel);
        labelGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        return new LabelEditComposite(labelGroup, part, toolkit);
    }

    private void createDescriptionGroupIfRequired(Composite composite) {
        if (part instanceof IDescribedElement) {
            Group descriptionGroup = getToolkit().createGroup(composite, Messages.IpsPartEditDialog_groupDescription);
            descriptionGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
            new DescriptionEditComposite(descriptionGroup, (IDescribedElement)part, getToolkit(), getBindingContext());
        }
    }

    private void createVersionGroupIfRequired(Composite composite) {
        if (part instanceof IVersionControlledElement) {
            Group versionGroup = getToolkit().createGroup(composite, Messages.IpsPartEditDialog_groupVersion);
            versionGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
            new VersionsComposite(versionGroup, (IVersionControlledElement)part, getToolkit(), getBindingContext());
        }
    }

    private void createDeprecationGroupIfRequired(Composite composite) {
        if (part instanceof IVersionControlledElement) {
            Group versionGroup = getToolkit().createGroup(composite,
                    Messages.DeprecationSection_label);
            versionGroup.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
            new DeprecationEditComposite(versionGroup, (IpsObjectPartContainer)part, getToolkit(),
                    getBindingContext());
        }
    }

    private void createTabItem(TabFolder folder, Composite composite) {
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.IpsPartEditDialog_tabItemDocumentation);
        item.setControl(composite);
    }

    public IIpsObjectPart getIpsPart() {
        return part;
    }

    @Override
    protected void updateTitleInTitleArea() {
        setTitle(buildTitle());
    }

    /**
     * Creates the title of this dialog. The title will include the name of the ips object the ips
     * object part belongs to and the name of the ips object part itself.
     */
    protected String buildTitle() {
        IIpsObjectPart ipsPpart = getIpsPart();
        if (ipsPpart.getParent() instanceof IIpsObjectGeneration) {
            return ipsPpart.getIpsObject().getName() + ' ' + ipsPpart.getParent().getName() + '.' + ipsPpart.getName();
        }
        return ipsPpart.getIpsObject().getName() + '.' + ipsPpart.getName();
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(getIpsPart().getIpsSrcFile())) {
            updateTitleInTitleArea();
            updateMessageArea();
            contentsChangedInternal(event);
        }
    }

    /**
     * Updates the message area at the top of the dialog beneath the title with up-to-date
     * validation information.
     */
    protected void updateMessageArea() {
        MessageList msgList;
        try {
            msgList = part.validate(part.getIpsProject());
            MessageList objMsgList = part.getIpsObject().validate(part.getIpsProject());
            msgList.add(objMsgList.getMessagesFor(part));
            addAdditionalDialogMessages(msgList);
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return;
        }

        if (msgList.size() > 0) {
            Message firstErrorMsg = getFirstMessage(msgList, Message.ERROR, getBindingContext()
                    .getIgnoredMessageCodes());
            if (firstErrorMsg != null) {
                setMessage(firstErrorMsg.getText(), IMessageProvider.ERROR);
                return;
            }

            Message firstWarningMsg = getFirstMessage(msgList, Message.WARNING, getBindingContext()
                    .getIgnoredMessageCodes());
            if (firstWarningMsg != null) {
                setMessage(firstWarningMsg.getText(), IMessageProvider.WARNING);
                return;
            }

            Message firstInfoMsg = getFirstMessage(msgList, Message.INFO, getBindingContext().getIgnoredMessageCodes());
            if (firstInfoMsg != null) {
                setMessage(firstInfoMsg.getText(), IMessageProvider.INFORMATION);
                return;
            }
        }

        if (part instanceof IDescribedElement) {
            String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(
                    (IDescribedElement)part);
            if (!(IpsStringUtils.isEmpty(localizedDescription.trim()))) {
                setMessage(localizedDescription, IMessageProvider.INFORMATION);
                return;
            }
        }

        setMessage(IpsStringUtils.EMPTY);
    }

    /**
     * Overwrite this method to add additional messages to the dialog message area.
     * 
     * @param messageList Add your additional messages to this {@link MessageList}.
     * 
     */
    protected void addAdditionalDialogMessages(MessageList messageList) {
        // default implementation does nothing
    }

    private Message getFirstMessage(MessageList messageList, Severity severity, Set<String> ignoredMessageCodes) {
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.getMessage(i);
            if (message.getSeverity() == severity && !(ignoredMessageCodes.contains(message.getCode()))) {
                return message;
            }
        }
        return null;
    }

    /**
     * Method for sub classes to hook into the contents changed notification.
     * 
     * @param event The event that caused the content change
     */
    protected void contentsChangedInternal(ContentChangeEvent event) {
        // Empty default implementation
    }

    /**
     * Allows subclasses to set whether the composite that allows to edit labels is enabled in case
     * the part being edited is an {@link ILabeledElement}.
     * 
     * @throws RuntimeException If the part being edited is not an {@link ILabeledElement}
     */
    protected final void setLabelCompositeEnabled(boolean enabled) {
        if (!(part instanceof ILabeledElement)) {
            throw new RuntimeException();
        }
        labelEditComposite.setEnabled(enabled);
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
        int posX;
        int posY;
        int width;
        int height;

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
            shell.addListener(SWT.Move, $ -> storePosition(shell));
            shell.addListener(SWT.Resize, $ -> storePosition(shell));
            IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection(dialogSizeSettingsKey);
            configurePosition(settings, shell);
        }
    }
}
