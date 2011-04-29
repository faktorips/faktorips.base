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

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.EditField;

public interface IExtensionPropertyEditFieldFactory {

    /**
     * Returns a new EditField that allows to edit the property value. Can return <code>null</code>
     * if <code>isEditedInStandardExtensionArea()</code> returns <code>false</code>.
     * 
     * @param ipsObjectPart The ips object part which extension property an edit field is created
     *            for.
     * @param extensionArea The standard extension area composite. A new control that allows editing
     *            the property value has to be added to this composite in subclasses. The EditField
     *            has to be constructed based on the control.
     * @param toolkit The ui toolkit to be used to create the control to ensure a consistent user
     *            interface.
     */
    public EditField<?> newEditField(IIpsObjectPartContainer ipsObjectPart, Composite extensionArea, UIToolkit toolkit);

}
