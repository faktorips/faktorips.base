/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

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
    public IIpsObject getIpsObject(IIpsProject project, IpsObjectType type) {
        return project.findIpsObject(type, getText());
    }

}
