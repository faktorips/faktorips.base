/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.bf;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.bf.ui.edit.BusinessFunctionEditPart;
import org.faktorips.devtools.bf.ui.edit.CallBusinessFunctionActionEditPart;
import org.faktorips.devtools.bf.ui.edit.CallMethodActionEditPart;
import org.faktorips.devtools.bf.ui.edit.ControlFlowEditPart;
import org.faktorips.devtools.bf.ui.edit.DecisionEditPart;
import org.faktorips.devtools.bf.ui.edit.MergeEditPart;
import org.faktorips.devtools.bf.ui.edit.OpaqueActionEditPart;
import org.faktorips.devtools.bf.ui.edit.ParameterEditPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;

//TODO string externalization
public class BusinessFunctionLabelProvider implements ILabelProvider {

    
    public Image getImage(Object element) {
        IStructuredSelection sel = (IStructuredSelection)element;
        EditPart editPart = (EditPart)sel.getFirstElement();
        if(editPart instanceof ParameterEditPart){
            return BFElementType.PARAMETER.getImage();
        }
        if(editPart instanceof CallMethodActionEditPart){
            return BFElementType.ACTION_METHODCALL.getImage(); 
        }
        if(editPart instanceof OpaqueActionEditPart){
            return BFElementType.ACTION_INLINE.getImage(); 
        }
        if(editPart instanceof CallBusinessFunctionActionEditPart){
            return BFElementType.ACTION_BUSINESSFUNCTIONCALL.getImage(); 
        }
        if(editPart instanceof MergeEditPart){
            return BFElementType.MERGE.getImage(); 
        }
        if(editPart instanceof DecisionEditPart){
            return BFElementType.DECISION.getImage(); 
        }
        if(editPart instanceof ControlFlowEditPart){
            return IpsPlugin.getDefault().getImage("/obj16/" + "ControlFlow.gif");
        }
        IIpsElement ipsElement = (IIpsElement)editPart.getModel();
        return ipsElement.getImage();
    }

    public String getText(Object element) {
        IStructuredSelection sel = (IStructuredSelection)element;
        EditPart editPart = (EditPart)sel.getFirstElement();
        if (editPart instanceof BusinessFunctionEditPart) {
            IBusinessFunction bf = (IBusinessFunction)editPart.getModel();
            return "Business Function: " + bf.getName();
        }
        if (editPart instanceof ParameterEditPart) {
            return "Parameters";
        }
        IIpsElement ipsElement = (IIpsElement)editPart.getModel();
        if(ipsElement == null){
            return "";
        }
        if(editPart instanceof ControlFlowEditPart){
            return "";
        }
        String displayName = ((IBFElement)ipsElement).getDisplayString();
        if (editPart instanceof CallBusinessFunctionActionEditPart) {
            return "Call Business Function Action: " + displayName;
        }
        if (editPart instanceof OpaqueActionEditPart) {
            return "Opaque Action: " + displayName;
        }
        if (editPart instanceof CallMethodActionEditPart) {
            return "Call Method Action: " + displayName;
        }
        if (editPart instanceof DecisionEditPart) {
            return "Decision: " + displayName;
        }
        if (editPart instanceof MergeEditPart) {
            return "Merge: " + displayName;
        }
        return "";
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }

}
