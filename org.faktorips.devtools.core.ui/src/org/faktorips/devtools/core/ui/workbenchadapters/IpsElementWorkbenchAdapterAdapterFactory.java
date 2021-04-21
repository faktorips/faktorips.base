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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.IpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class IpsElementWorkbenchAdapterAdapterFactory implements IAdapterFactory {

    private Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public IpsElementWorkbenchAdapterAdapterFactory() {
        super();
        workbenchAdapterMap = new HashMap<>();
        registerAdapters();
    }

    protected void registerAdapters() {
        List<IWorkbenchAdapterProvider> providers = IpsUIPlugin.getWorkbenchAdapterProviders();
        for (IWorkbenchAdapterProvider provider : providers) {
            workbenchAdapterMap.putAll(provider.getAdapterMap());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!adapterType.isAssignableFrom(IpsElementWorkbenchAdapter.class)) {
            return null;
        }

        if (adaptableObject instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptableObject;
            Class<? extends IpsObject> implementingClass = ipsSrcFile.getIpsObjectType().getImplementingClass();
            if (implementingClass != null) {
                return (T)getAdapterByClass(implementingClass);
            }
        }
        if (adaptableObject instanceof IpsElement) {
            return (T)getAdapterByClass((Class<? extends IpsElement>)adaptableObject.getClass());
        } else {
            return null;
        }
    }

    /**
     * Getting the workbench adapter by class.
     * 
     */
    public IpsElementWorkbenchAdapter getAdapterByClass(Class<? extends IIpsElement> adaptableClass) {
        IpsElementWorkbenchAdapter result = null;
        Class<? extends IIpsElement> classOrSuperclass = adaptableClass;
        while (result == null) {
            result = workbenchAdapterMap.get(classOrSuperclass);
            if (result == null) {
                Class<?> superClass = classOrSuperclass.getSuperclass();
                if (IpsElement.class.isAssignableFrom(superClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends IpsElement> castedClass = (Class<? extends IpsElement>)superClass;
                    classOrSuperclass = castedClass;
                } else {
                    return null;
                }
            }
        }
        return result;
    }

    protected void register(Class<? extends IIpsElement> adaptableClass, IpsElementWorkbenchAdapter adapter) {
        workbenchAdapterMap.put(adaptableClass, adapter);
    }

    protected void unregister(Class<? extends IIpsElement> adaptableClass) {
        workbenchAdapterMap.remove(adaptableClass);
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class<?>[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
    }

}
