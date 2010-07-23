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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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

    /** The binding context to use to bind model object properties to ui controls. */
    protected BindingContext bindingContext = new BindingContext();

    private IIpsObjectPart part;
    private TextField descriptionField;
    private Memento oldState;
    private boolean dirty = false;

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
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
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
        if (bindingContext != null) {
            bindingContext.dispose();
        }

        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);

        return super.close();
    }

    private void handleAbortion() {
        part.getIpsObject().setState(oldState);
        if (!dirty) {
            part.getIpsObject().getIpsSrcFile().markAsClean();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        bindingContext.updateUI();
        updateTitleInTitleArea();
        // updateMessageArea should be called after the size of the dialog has been calculated -->
        // @see method create() (MTB#142)
        return control;
    }

    @Override
    public void create() {
        super.create();
        // updateMessageArea have to be called after the size of the dialog is set. (MTB#142)
        updateMessageArea();
    }

    /**
     * Creates the description tab.
     */
    protected TabItem createDescriptionTabItem(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Text text = uiToolkit.createMultilineText(c);
        bindingContext.bindContent(text, getIpsPart(), IIpsObjectPart.PROPERTY_DESCRIPTION);
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(Messages.IpsPartEditDialog_description);
        item.setControl(c);

        return item;
    }

    public IIpsObjectPart getIpsPart() {
        return part;
    }

    @Override
    protected void updateTitleInTitleArea() {
        setTitle(buildTitle());
        setTitleImage(IpsUIPlugin.getImageHandling().getImage(getIpsPart()));
    }

    /**
     * Creates the title of this dialog. The title will include the name of the ips object the ips
     * object part belongs to and the name of the ips object part itself.
     */
    protected String buildTitle() {
        IIpsObjectPart part = getIpsPart();
        if (part.getParent() instanceof IIpsObjectGeneration) {
            return part.getIpsObject().getName() + ' ' + part.getParent().getName() + '.' + part.getName();
        }
        return part.getIpsObject().getName() + '.' + part.getName();
    }

    /**
     * Enables / disables the description field.
     * 
     * @param enabled Enables the description field if <code>true</code>, disables it if
     *            <code>false</code>.
     */
    protected void setEnabledDescription(boolean enabled) {
        if (descriptionField != null) {
            descriptionField.getControl().setEnabled(enabled);
        }
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
        try {
            MessageList msgList = part.validate(part.getIpsProject());
            MessageList objMsgList = part.getIpsObject().validate(part.getIpsProject());
            msgList.add(objMsgList.getMessagesFor(part));

            Message msg = null;
            if (msgList.getNoOfMessages() > 0) {
                msg = msgList.getFirstMessage(Message.ERROR);
                if (msg != null) {
                    setMessage(msg.getText(), IMessageProvider.ERROR);
                    return;
                }

                msg = msgList.getFirstMessage(Message.WARNING);
                if (msg != null) {
                    setMessage(msg.getText(), IMessageProvider.WARNING);
                    return;
                }

                msg = msgList.getFirstMessage(Message.INFO);
                if (msg != null) {
                    setMessage(msg.getText(), IMessageProvider.INFORMATION);
                    return;
                }
            } else if (part.getDescription() != null && !StringUtils.isEmpty(part.getDescription().trim())) {
                setMessage(part.getDescription(), IMessageProvider.INFORMATION);
            } else {
                setMessage(null);
            }

        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Method for sub classes to hook into the contents changed notification.
     * 
     * @param event The event that caused the content change
     */
    protected void contentsChangedInternal(ContentChangeEvent event) {
        // Empty default implementation
    }

    // TODO: code duplication in PersistentTypeInfoSection
    protected <E extends Enum<E>> void setComboItemsForEnum(Combo combo, Class<E> clazz) {
        Enum<E>[] allEnumConstants = clazz.getEnumConstants();
        String[] allEnumValues = new String[allEnumConstants.length];
        for (int i = 0; i < allEnumConstants.length; i++) {
            allEnumValues[i] = allEnumConstants[i].toString();
        }
        combo.setItems(allEnumValues);
    }
}
