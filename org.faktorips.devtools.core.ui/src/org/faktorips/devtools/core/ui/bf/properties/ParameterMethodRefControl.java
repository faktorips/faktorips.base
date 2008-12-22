package org.faktorips.devtools.core.ui.bf.properties;

/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version
 * 0.1 (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

import java.util.ArrayList;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
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

    public ParameterMethodRefControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.getString("ParameterMethodRefControl.ChooseMethodLabel")); //$NON-NLS-1$
    }

    public void setParameterType(IType type) {
        this.parameterType = type;
    }

    private IMethod[] getSelectableMethods() {
        if (parameterType == null) {
            return new IMethod[0];
        }
        ArrayList<IMethod> methods = new ArrayList<IMethod>();
        for (IMethod method : parameterType.getMethods()) {
            if (method.getParameters().length == 0) {
                methods.add(method);
            }
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

    /**
     * {@inheritDoc}
     */
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(),
                    new DefaultLabelProvider());
            selectDialog.setTitle(Messages.getString("ParameterMethodRefControl.ChooseMethodTitle")); //$NON-NLS-1$
            selectDialog.setMessage(Messages.getString("ParameterMethodRefControl.dialogDescription") //$NON-NLS-1$
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
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
}
