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

package org.faktorips.devtools.core.ui.bf.properties;

/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.StringUtil;

/**
 * A control consisting of a text field and a browse button by means of which on can select the
 * methods of a provided business function parameter.
 * 
 * @author Peter Erzberger
 */
public class ParameterMethodRefControl extends TextButtonControl {

    private IType parameterType;
    private ParameterMethodCompletionProcessor processor;

    public ParameterMethodRefControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.ParameterMethodRefControl_ChooseMethodLabel);
        processor = new ParameterMethodCompletionProcessor();
        processor.setComputeProposalForEmptyPrefix(true);
        SubjectControlContentAssistant assistant = CompletionUtil.createContentAssistant(processor);
        ContentAssistHandler.createHandlerForText(getTextControl(), assistant);
    }

    public void setParameterType(IType type) {
        parameterType = type;
    }

    private IMethod[] getSelectableMethods() throws CoreException {
        if (parameterType == null) {
            return new IMethod[0];
        }
        ArrayList<IMethod> methods = new ArrayList<IMethod>();
        for (IMethod method : parameterType.findAllMethods(parameterType.getIpsProject())) {
            if (method.getParameters().length == 0) {
                methods.add(method);
            }
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

    @Override
    protected void buttonClicked() {
        try {
            // TODO use styled label to add the type information
            // adding the attribute type to the labels
            DefaultLabelProvider lp = new DefaultLabelProvider() {

                @Override
                public String getText(Object element) {
                    String text = super.getText(element);
                    if (element instanceof IAttribute || element instanceof IMethod) {
                        text += (" - ") + ((IIpsObjectPart)element).getParent().getName(); //$NON-NLS-1$
                    }
                    return text;
                }

            };
            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), lp);
            selectDialog.setTitle(Messages.ParameterMethodRefControl_ChooseMethodTitle);
            selectDialog.setMessage(Messages.ParameterMethodRefControl_dialogDescription
                    + (parameterType == null ? "" : parameterType.getName())); //$NON-NLS-1$
            selectDialog.setElements(getSelectableMethods());
            selectDialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (selectDialog.open() == Window.OK) {
                if (selectDialog.getResult().length > 0) {
                    IMethod associationResult = (IMethod)selectDialog.getResult()[0];
                    setText(associationResult.getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    public void setIpsProject(IIpsProject ipsProject) {
        processor.setIpsProject(ipsProject);
    }

    private class ParameterMethodCompletionProcessor extends AbstractCompletionProcessor {
        @Override
        @SuppressWarnings("unchecked")
        protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {

            String match = prefix.toLowerCase();
            for (IMethod method : getSelectableMethods()) {
                if (method.getName().startsWith(match)) {
                    result.add(new CompletionProposal(method.getName(), 0, documentOffset, method.getName().length(),
                            IpsUIPlugin.getImageHandling().getImage(method), method.getSignatureString(), null, method
                                    .getDescription()));
                }
            }
        }
    }
}
