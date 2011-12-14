/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

public class IpsObjectEditorTester extends PropertyTester {

    public static final String PROPERTY_EDITOR_EDITABLE = "isEditorEditable"; //$NON-NLS-1$

    public IpsObjectEditorTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IpsObjectEditor editor = (IpsObjectEditor)receiver;
        if (PROPERTY_EDITOR_EDITABLE.equals(property)) {
            return editor.isDataChangeable();
        } else {
            return false;
        }
    }

}
