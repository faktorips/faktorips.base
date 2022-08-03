/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.faktorips.devtools.model.ipsproject.IIpsProject;

class ReferencedIpsProjectViewItem {

    private final IIpsProject ipsProject;

    ReferencedIpsProjectViewItem(IIpsProject project) {
        ipsProject = project;
    }

    IIpsProject getIpsProject() {
        return ipsProject;
    }

    String getName() {
        return ipsProject.getName();
    }
}
