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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;

public class IpsObjectPathContainerChildrenProvider implements IChildrenProvider<IIpsObjectPathContainer> {

    @Override
    public Object[] getChildren(IIpsObjectPathContainer element) {
        List<Object> children = new ArrayList<>();
        List<IIpsObjectPathEntry> resolveEntries = element.resolveEntries();
        for (IIpsObjectPathEntry entry : resolveEntries) {
            if (entry instanceof IIpsProjectRefEntry) {
                IIpsProject referencedIpsProject = ((IIpsProjectRefEntry)entry).getReferencedIpsProject();
                children.add(new ReferencedIpsProjectViewItem(referencedIpsProject));
            } else {
                children.add(entry.getIpsPackageFragmentRoot());
            }
        }

        return children.toArray();
    }

}
