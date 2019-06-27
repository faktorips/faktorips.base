/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
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
        IIpsObjectPart ipsObjectPart = castOrAdaptToPart(receiver, IIpsObjectPart.class);
        if (ipsObjectPart == null) {
            return false;
        }
        if (PROPERTY_PART_EDITABLE_IN_EDITOR.equals(property)) {
            return isPartEditableInEditor(ipsObjectPart);
        } else if (PROPERTY_PART_EDITABLE.equals(property)) {
            return isPartEditable(ipsObjectPart);
        } else {
            return false;
        }
    }

    /**
     * Checks if the given object is either an instance of, or adapts to the given
     * {@link IIpsObjectPart} class and returns that part. Returns <code>null</code> if the given
     * object neither is an {@link IIpsObjectPart} nor does it adapt to the specific class.
     * 
     */
    public static <T extends IIpsObjectPart> T castOrAdaptToPart(Object object, Class<T> partClass) {
        if (partClass.isAssignableFrom(object.getClass())) {
            @SuppressWarnings("unchecked")
            T part = (T)object;
            return part;
        } else if (object instanceof IAdaptable) {
            @SuppressWarnings("unchecked")
            T part = (T)Platform.getAdapterManager().getAdapter(object, partClass);
            return part;
        }
        return null;
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
