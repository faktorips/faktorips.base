/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class IpsWorkbenchAdapterFactory implements IAdapterFactory {

    Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public IpsWorkbenchAdapterFactory() {
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

    @SuppressWarnings("unchecked")
    // IWorkbenchAdapter is not generic
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptableObject;
            try {
                if (ipsSrcFile.exists() && ipsSrcFile.isContentParsable()) {
                    Class<? extends IpsObject> implementingClass = ipsSrcFile.getIpsObjectType().getImplementingClass();
                    if (implementingClass != null) {
                        return getAdapterByClass(implementingClass, adapterType);
                    }
                    /*
                     * Comment from old Code in IpsObject.getImage(): The IPS source file doesn't
                     * exists, thus the IPS object couldn't be linked to an IPS source file in the
                     * workspace, return the image of the IPS source file to decide between valid
                     * and invalid IPS objects.
                     */

                }
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
        if (adaptableObject instanceof IpsElement) {
            return getAdapterByClass((Class<? extends IpsElement>)adaptableObject.getClass(), adapterType);
        } else {
            return null;
        }
    }

    /**
     * Getting the workbench adapter by class.
     * 
     * @param adaptableClass
     * @param adapterType
     * @return
     */
    public IpsElementWorkbenchAdapter getAdapterByClass(Class<? extends IpsElement> adaptableClass, Class<?> adapterType) {
        IpsElementWorkbenchAdapter result = null;
        while (result == null) {
            result = workbenchAdapterMap.get(adaptableClass);
            if (result == null) {
                Class<?> superClass = adaptableClass.getSuperclass();
                if (IpsElement.class.isAssignableFrom(superClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends IpsElement> castedClass = (Class<? extends IpsElement>)superClass;
                    adaptableClass = castedClass;
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

    @SuppressWarnings("unchecked")
    // IWorkbenchAdapter is not generic
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
    }

}