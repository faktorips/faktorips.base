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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

/**
 * A field for object references.
 */
public class IpsObjectField extends TextButtonField {

    public IpsObjectField(TextButtonControl control) {
        super(control);
        setSupportsNullStringRepresentation(false);
    }

    /**
     * Returns the datatype if possible, or null if the current value in the control does not
     * specifiy a datatype.
     */
    public IIpsObject getIpsObject(IIpsProject project, IpsObjectType type) throws CoreException {
        return project.findIpsObject(type, getText());
    }

}
