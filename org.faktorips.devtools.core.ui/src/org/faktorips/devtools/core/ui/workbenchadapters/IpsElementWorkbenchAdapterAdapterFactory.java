/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class IpsElementWorkbenchAdapterAdapterFactory implements IAdapterFactory {

    private Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public IpsElementWorkbenchAdapterAdapterFactory() {
        super();
        workbenchAdapterMap = new HashMap<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter>();
        registerAdapters();
    }

    protected void registerAdapters() {
        List<IWorkbenchAdapterProvider> providers = IpsUIPlugin.getWorkbenchAdapterProviders();
        for (IWorkbenchAdapterProvider provider : providers) {
            workbenchAdapterMap.putAll(provider.getAdapterMap());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // eclipse api is not type safe
    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (!adapterType.isAssignableFrom(IpsElementWorkbenchAdapter.class)) {
            return null;
        }

        if (adaptableObject instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptableObject;
            try {
                if (ipsSrcFile.exists() && ipsSrcFile.isContentParsable()) {
                    Class<? extends IpsObject> implementingClass = ipsSrcFile.getIpsObjectType().getImplementingClass();
                    if (implementingClass != null) {
                        return getAdapterByClass(implementingClass);
                    }
                    /*
                     * Comment from old Code in IpsObject.getImage(): The IPS source file doesn't
                     * exists, thus the IPS object couldn't be linked to an IPS source file in the
                     * workspace, return the image of the IPS source file to decide between valid
                     * and invalid IPS objects.
                     */
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        if (adaptableObject instanceof IpsElement) {
            return getAdapterByClass((Class<? extends IpsElement>)adaptableObject.getClass());
        } else {
            return null;
        }
    }

    /**
     * Getting the workbench adapter by class.
     * 
     */
    public IpsElementWorkbenchAdapter getAdapterByClass(Class<? extends IpsElement> adaptableClass) {
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
    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
    }

}
