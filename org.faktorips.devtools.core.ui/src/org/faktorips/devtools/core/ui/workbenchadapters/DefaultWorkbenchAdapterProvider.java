/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;

public class DefaultWorkbenchAdapterProvider implements IWorkbenchAdapterProvider {

    // private DefaultWorkbenchAdapter defaultWorkbenchAdapter;

    private final Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public DefaultWorkbenchAdapterProvider() {
        workbenchAdapterMap = new HashMap<>();
        registerAdapters();
    }

    @Override
    public Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> getAdapterMap() {
        return workbenchAdapterMap;
    }

    private void register(Class<? extends IIpsElement> adaptableClass) {
        workbenchAdapterMap.put(adaptableClass, new DecoratedWorkbenchAdapter(adaptableClass));
    }

    protected void registerAdapters() {

        for (Class<? extends IIpsElement> ipsFiles : IIpsDecorators.get().getDecoratedClasses()) {
            register(ipsFiles);
        }
    }
}
