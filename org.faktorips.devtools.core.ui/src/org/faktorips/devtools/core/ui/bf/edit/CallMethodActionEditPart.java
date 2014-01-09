/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * This specialization of {@link ActionEditPart} is only necessary because the tabbed property view
 * framework needs different classes to distinguish the kind of objects for which it provides editor
 * views.
 * 
 * @author Peter Erzberger
 */
public class CallMethodActionEditPart extends ActionEditPart {

    public CallMethodActionEditPart() {
        super(IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/CallOperationAction.gif")); //$NON-NLS-1$
    }

}
