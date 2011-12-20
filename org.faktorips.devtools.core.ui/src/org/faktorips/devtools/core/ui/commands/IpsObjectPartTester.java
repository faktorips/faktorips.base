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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IWorkbenchPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * This tester expects an {@link IIpsObjectPart} as receiver and is able to test properties
 * concerning this part.
 * 
 * @author dirmeier
 */
public class IpsObjectPartTester extends PropertyTester {

    /**
     * Check whether the receiver part is editable in the current active editor
     */
    public static final String PROPERTY_PART_EDITABLE_IN_EDITOR = "isPartEditableInEditor"; //$NON-NLS-1$

    /**
     * Check wether the receiver part is editable
     */
    public static final String PROPERTY_PART_EDITABLE = "isPartEditable"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IIpsObjectPart ipsObjectPart = (IIpsObjectPart)receiver;
        if (PROPERTY_PART_EDITABLE_IN_EDITOR.equals(property)) {
            return isPartEditableInEditor(ipsObjectPart);
        } else if (PROPERTY_PART_EDITABLE.equals(property)) {
            return isPartEditable(ipsObjectPart);
        } else {
            return false;
        }
    }

    private boolean isPartEditableInEditor(IIpsObjectPart ipsObjectPart) {
        IWorkbenchPart activePart = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getActivePart();
        if (activePart instanceof IpsObjectEditor) {
            IpsObjectEditor ipsEditor = (IpsObjectEditor)activePart;
            if (ipsEditor.getIpsSrcFile().equals(ipsObjectPart.getIpsSrcFile()) && ipsEditor.isDataChangeable()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPartEditable(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptGeneration) {
            return IpsUIPlugin.getDefault().isGenerationEditable((IProductCmptGeneration)ipsObjectPart);
        } else {
            return IpsUIPlugin.isEditable(ipsObjectPart.getIpsSrcFile());
        }
    }

}
