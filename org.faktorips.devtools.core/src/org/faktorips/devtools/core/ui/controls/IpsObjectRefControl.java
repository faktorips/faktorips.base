/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.StringUtil;

/**
 * Control to edit a reference to an ips source file in a text control with an associated browse
 * button that allows to browse the available objects.
 */
public abstract class IpsObjectRefControl extends TextButtonControl {
    
    private IIpsProject ipsProject;
    
    private String dialogTitle;
    private String dialogMessage;
    
    private IpsObjectCompletionProcessor completionProcessor;

    private ILabelProvider labelProvider;
    
    public IpsObjectRefControl(
            IIpsProject project,
            Composite parent, 
            UIToolkit toolkit,
            String dialogTitle,
            String dialogMessage) {
        this(project, parent, toolkit, dialogTitle, dialogMessage, DefaultLabelProvider.createWithIpsSourceFileMapping());
    }
    
    /**
     * @param parent
     * @param style
     */
    public IpsObjectRefControl(
            IIpsProject project,
            Composite parent, 
            UIToolkit toolkit,
            String dialogTitle,
            String dialogMessage,
            ILabelProvider labelProvider) {
        super(parent, toolkit, Messages.IpsObjectRefControl_title);
        this.ipsProject = project;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        this.labelProvider = labelProvider;
        completionProcessor = new IpsObjectCompletionProcessor(this);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
    }
    
    public void setIpsProject(IIpsProject project) {
        this.ipsProject = project;
        setButtonEnabled(project!=null && project.exists());
    }
    
    public IIpsProject getIpsProject() {
        return ipsProject;
    }
    
    protected void buttonClicked() {
        final IpsObjectSelectionDialog dialog = new IpsObjectSelectionDialog(getShell(), dialogTitle, dialogMessage, labelProvider);
        BusyIndicator.showWhile(getDisplay(), new Runnable() {
            public void run() {
                try {
                    dialog.setElements(getIpsSrcFiles());
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        try {
            dialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (dialog.open() == Window.OK) {
                if (dialog.getResult().length > 0) {
                    IIpsSrcFile ipsSrcFile = (IIpsSrcFile)dialog.getResult()[0];
                    setText(ipsSrcFile.getQualifiedNameType().getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns all ips source files that can be chosen by the user.
     */
    protected abstract IIpsSrcFile[] getIpsSrcFiles() throws CoreException;
}
