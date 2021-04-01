/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * This specialization of {@link ActionEditPart} is only necessary because the tabbed property view
 * framework needs different classes to distinguish the kind of objects for which it provides editor
 * views.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
public class InlineActionEditPart extends ActionEditPart {

    public InlineActionEditPart() {
        super(IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/OpaqueAction.gif")); //$NON-NLS-1$
    }

}
