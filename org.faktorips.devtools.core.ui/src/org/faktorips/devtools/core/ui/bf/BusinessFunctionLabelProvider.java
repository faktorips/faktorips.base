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

package org.faktorips.devtools.core.ui.bf;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.bf.edit.BusinessFunctionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.CallBusinessFunctionActionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.CallMethodActionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.CallMethodDecisionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.ControlFlowEditPart;
import org.faktorips.devtools.core.ui.bf.edit.DecisionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.InlineActionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.MergeEditPart;
import org.faktorips.devtools.core.ui.bf.edit.ParameterEditPart;

/**
 * Provides display text and images for business function elements. It is used by the property view.
 */
public class BusinessFunctionLabelProvider implements ILabelProvider {

    @Override
    public Image getImage(Object element) {
        IStructuredSelection sel = (IStructuredSelection)element;
        EditPart editPart = (EditPart)sel.getFirstElement();
        return IpsUIPlugin.getImageHandling().getImage((IIpsElement)editPart.getModel());
    }

    @Override
    public String getText(Object element) {
        IStructuredSelection sel = (IStructuredSelection)element;
        EditPart editPart = (EditPart)sel.getFirstElement();
        if (editPart instanceof BusinessFunctionEditPart) {
            IBusinessFunction bf = (IBusinessFunction)editPart.getModel();
            return Messages.BusinessFunctionLabelProvider_bf + bf.getName();
        }
        if (editPart instanceof ParameterEditPart) {
            return Messages.BusinessFunctionLabelProvider_parameters;
        }
        IIpsElement ipsElement = (IIpsElement)editPart.getModel();
        if (ipsElement == null) {
            return ""; //$NON-NLS-1$
        }
        if (editPart instanceof ControlFlowEditPart) {
            return Messages.BusinessFunctionLabelProvider_controlFlow;
        }
        String displayName = ((IBFElement)ipsElement).getDisplayString();
        if (editPart instanceof CallBusinessFunctionActionEditPart) {
            return Messages.BusinessFunctionLabelProvider_callBfAction + displayName;
        }
        if (editPart instanceof InlineActionEditPart) {
            return Messages.BusinessFunctionLabelProvider_inlineAction + displayName;
        }
        if (editPart instanceof CallMethodActionEditPart) {
            return Messages.BusinessFunctionLabelProvider_callMethodAction + displayName;
        }
        if (editPart instanceof DecisionEditPart) {
            return Messages.BusinessFunctionLabelProvider_decision + displayName;
        }
        if (editPart instanceof CallMethodDecisionEditPart) {
            return Messages.BusinessFunctionLabelProvider_callMethodDecision + displayName;
        }
        if (editPart instanceof MergeEditPart) {
            return Messages.BusinessFunctionLabelProvider_merge + displayName;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        // Nothing to do
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    /**
     * Returns false.
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // Nothing to do
    }

}
