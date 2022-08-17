/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.IWorkingSet;

/**
 * scope for working sets
 * 
 * @author dicker
 */
public class IpsSearchWorkingSetScope extends AbstractIpsSearchScope {

    private final IWorkingSet[] workingSets;

    public IpsSearchWorkingSetScope(IWorkingSet[] workingSets) {
        this.workingSets = workingSets;
    }

    @Override
    protected List<?> getSelectedObjects() {
        List<Object> selectedObjects = new ArrayList<>();

        for (IWorkingSet workingSet : workingSets) {
            selectedObjects.addAll(Arrays.asList(workingSet.getElements()));
        }

        return selectedObjects;
    }

    @Override
    protected String getScopeTypeLabel(boolean singular) {
        return singular ? Messages.IpsSearchWorkingSetScope_scopeTypeLabelSingular
                : Messages.IpsSearchWorkingSetScope_scopeTypeLabelPlural;
    }

    @Override
    protected List<String> getNamesOfSelectedObjects() {
        List<String> namesOfSelectedObjects = new ArrayList<>();

        for (IWorkingSet workingSet : workingSets) {
            namesOfSelectedObjects.add(workingSet.getName());
        }

        return namesOfSelectedObjects;
    }

}
