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

package org.faktorips.devtools.core.ui.search.scope;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * scope for workspace
 * 
 * @author dicker
 */
public class IpsSearchWorkspaceScope extends AbstractIpsSearchScope {

    @Override
    protected List<?> getSelectedObjects() {
        return Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
    }

    @Override
    public String getScopeDescription() {
        return getScopeTypeLabel(true);
    }

    @Override
    protected String getScopeTypeLabel(boolean singular) {
        return Messages.IpsSearchWorkspaceScope_scopeTypeLabel;
    }
}
