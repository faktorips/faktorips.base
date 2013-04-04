/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
