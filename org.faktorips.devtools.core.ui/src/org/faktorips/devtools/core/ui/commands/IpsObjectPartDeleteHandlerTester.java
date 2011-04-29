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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

public class IpsObjectPartDeleteHandlerTester extends PropertyTester {

    public static final String PROPERTY = "isDeletable"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (PROPERTY.equals(property)) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)receiver;
            IWorkbenchPart activePart = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().getActivePart();
            if (activePart instanceof IpsObjectEditor) {
                IpsObjectEditor ipsEditor = (IpsObjectEditor)activePart;
                if (ipsEditor.getIpsSrcFile().equals(ipsObjectPart.getIpsSrcFile())) {
                    return true;
                }
            }
            /*
             * TODO At the moment delete of IpsObjectPart is only enabled when the editor for the
             * IpsSrcFile you want to manipulate is active. This is because we do not have a
             * refactoring dialog for deleting IpsObjectParts. We could activate this feature if
             * there is a dialog to confirm the deletion.
             */
            return false;
        } else {
            return false;
        }
    }

}
