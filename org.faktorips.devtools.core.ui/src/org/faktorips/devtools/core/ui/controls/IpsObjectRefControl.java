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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.util.StringUtil;

/**
 * Control to edit a reference to an ips source file in a text control with an associated browse
 * button that allows to browse the available objects.
 */
public abstract class IpsObjectRefControl extends TextButtonControl {

    private IIpsProject ipsProject;

    private String dialogTitle;
    private boolean enableDialogFilter = true;
    private String dialogMessage;
    private ContentAssistHandler handler;

    public IpsObjectRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, String dialogTitle,
            String dialogMessage) {

        super(parent, toolkit, Messages.IpsObjectRefControl_title);
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        setIpsProject(project);
    }

    public void setIpsProject(IIpsProject project) {
        ipsProject = project;
        setButtonEnabled(project != null && project.exists());
        if (handler != null) {
            handler.setEnabled(false);
        }
        handler = ContentAssistHandler.createHandlerForText(getTextControl(),
                CompletionUtil.createContentAssistant(new IpsObjectCompletionProcessor(this)));
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    protected void buttonClicked() {
        /*
         * using the StaticContentSelectIpsObjectContext is not the recommended way to use the
         * OpenIpsObjectSelecitonDialog. It is only used for older implementation If you have a
         * choice use your own implementation of SelectIpsObjectContext for better performance and
         * correct progress monitoring
         */
        final StaticContentSelectIpsObjectContext context = new StaticContentSelectIpsObjectContext();
        final OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(getShell(), dialogTitle, context);
        dialog.setMessage(dialogMessage);
        BusyIndicator.showWhile(getDisplay(), new Runnable() {
            @Override
            public void run() {
                try {
                    context.setElements(getIpsSrcFiles());
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        try {
            if (isDialogFilterEnabled()) {
                dialog.setFilter(getDefaultDialogFilterExpression());
            }
            if (dialog.open() == Window.OK) {
                if (dialog.getResult().length > 0) {
                    List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
                    Object[] result = dialog.getResult();
                    for (Object element : result) {
                        srcFiles.add((IIpsSrcFile)element);
                    }
                    updateTextControlAfterDialogOK(srcFiles);
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    protected String getDefaultDialogFilterExpression() {
        return StringUtil.unqualifiedName(super.getText());
    }

    /**
     * Called when the user closes the dialog by clicking OK.
     * 
     * @param ipsSrcFiles List of selected ips source files containing at least 1 element!
     */
    protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
        setText(ipsSrcFiles.get(0).getQualifiedNameType().getName());
    }

    public boolean isDialogFilterEnabled() {
        return enableDialogFilter;
    }

    public void setDialogFilterEnabled(boolean enable) {
        enableDialogFilter = enable;
    }

    /**
     * Returns all ips source files that can be chosen by the user.
     */
    protected abstract IIpsSrcFile[] getIpsSrcFiles() throws CoreException;

}
