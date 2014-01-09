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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;

public class IpsObjectPathContainerChildrenProvider implements IChildrenProvider<IIpsObjectPathContainer> {

    @Override
    public Object[] getChildren(IIpsObjectPathContainer element) throws CoreException {
        List<Object> children = new ArrayList<Object>();
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
