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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.PdObjectSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.StringUtil;


/**
 * Control to edit a referennce to an ips object in a text control with an associated browse button
 * that allows to browse the available objects.
 */
abstract class IpsObjectRefControl extends TextButtonControl {
    
    private IIpsProject ipsProject;
    
    private String dialogTitle;
    private String dialogMessage;
    
    private IpsObjectCompletionProcessor completionProcessor;
    
    /**
     * @param parent
     * @param style
     */
    public IpsObjectRefControl(
            IIpsProject project,
            Composite parent, 
            UIToolkit toolkit,
            String dialogTitle,
            String dialogMessage) {
        super(parent, toolkit, Messages.IpsObjectRefControl_title);
        this.ipsProject = project;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        completionProcessor = new IpsObjectCompletionProcessor(this);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
    }
    
    public void setPdProject(IIpsProject project) {
        this.ipsProject = project;
        setButtonEnabled(project!=null && project.exists());
    }
    
    public IIpsProject getIpsProject() {
        return ipsProject;
    }
    
    protected void buttonClicked() {
        try {
            PdObjectSelectionDialog dialog = new PdObjectSelectionDialog(getShell(), dialogTitle, dialogMessage);
            dialog.setElements(getPdObjects());
            dialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (dialog.open()==Window.OK) {
                if (dialog.getResult().length>0) {
                    IIpsObject pdObject = (IIpsObject)dialog.getResult()[0];
                    setText(pdObject.getQualifiedName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    protected abstract IIpsObject[] getPdObjects() throws CoreException;
    
}
