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

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Some extension properties contain data that can not be displayed in a simple EditField as defined
 * in {@link IExtensionPropertyEditFieldFactory}. IExtensionPropertySectionFactory create whole
 * sections for extension properties which may be incorporated in appropriate parts of the UI, e.g.
 * {@link org.faktorips.devtools.core.ui.editors.productcmpt.GenerationPropertiesPage} (which is up
 * to October 2011 the only editor that supports these sections).
 * 
 * @author schwering
 */
public interface IExtensionPropertySectionFactory {

    public enum Position {
        LEFT,
        RIGHT
    }

    Position getPosition();

    IpsSection newSection(IIpsObjectPartContainer ipsObjectPart, Composite parent, UIToolkit toolkit);
}
