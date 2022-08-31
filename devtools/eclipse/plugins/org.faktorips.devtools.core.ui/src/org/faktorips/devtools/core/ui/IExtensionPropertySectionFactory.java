/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Some extension properties contain data that can not be displayed in a simple {@link EditField} as
 * defined in {@link IExtensionPropertyEditFieldFactory}. {@link IExtensionPropertySectionFactory
 * IExtensionPropertySectionFactories} create whole sections for extension properties which may be
 * incorporated in appropriate parts of the UI, e.g.
 * {@link org.faktorips.devtools.core.ui.editors.productcmpt.GenerationPropertiesPage} (which is up
 * to October 2011 the only editor that supports these sections).
 * 
 * @author schwering
 */
public interface IExtensionPropertySectionFactory {

    /**
     * The {@link Position} defines where the extension property {@link IpsSection} should be placed
     * in the editor. Possible values are {@link Position#LEFT} and {@link Position#RIGHT} for two
     * column editors.
     */
    public enum Position {
        LEFT,
        RIGHT
    }

    /**
     * Returns where the extension property {@link IpsSection} should be placed in the editor.
     * 
     * @see Position
     * @return where the extension property {@link IpsSection} should be placed in the editor
     */
    Position getPosition();

    /**
     * Returns whether this factory wants to create a section for the given
     * {@link IIpsObjectPartContainer}.
     * 
     * @return whether this factory wants to create a section for the given
     *             {@link IIpsObjectPartContainer}.
     */
    boolean createsSectionFor(IIpsObjectPartContainer ipsObjectPart);

    /**
     * Creates a new {@link IpsSection} representing the extension property data for the given
     * {@link IIpsObjectPartContainer part} in the {@link Composite parent Composite} using the
     * given {@link UIToolkit toolkit}.
     * 
     * @param ipsObjectPart the {@link IIpsObjectPartContainer} containing the
     *            {@link IExtensionPropertyDefinition extension property}
     * @param parent the {@link Composite} in which the new {@link IpsSection} will be created
     * @param toolkit the {@link UIToolkit} used to create the new {@link IpsSection}
     * @return the created {@link IpsSection}
     */
    IpsSection newSection(IIpsObjectPartContainer ipsObjectPart, Composite parent, UIToolkit toolkit);
}
