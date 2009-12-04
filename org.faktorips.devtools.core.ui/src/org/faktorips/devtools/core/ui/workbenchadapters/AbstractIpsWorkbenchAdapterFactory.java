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
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public abstract class AbstractIpsWorkbenchAdapterFactory implements IAdapterFactory {

    Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public AbstractIpsWorkbenchAdapterFactory() {
        super();
        workbenchAdapterMap = new HashMap<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter>();
        registerAdapters();
    }

    protected abstract void registerAdapters();

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptableObject;
            Class<? extends IIpsObject> implementingClass = ipsSrcFile.getIpsObjectType().getImplementingClass();
            if (implementingClass != null) {
                return getAdapter(implementingClass, adapterType);
            } else {
                // must return null so the adapter manager searches in other factories
                return null;
            }
        }
        return getAdapter(adaptableObject.getClass(), adapterType);
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adaptableClass, Class adapterType) {
        IpsElementWorkbenchAdapter result = null;
        while (result == null) {
            result = workbenchAdapterMap.get(adaptableClass);
            if (result == null) {
                adaptableClass = adaptableClass.getSuperclass();
                if (adaptableClass == null) {
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
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
    }

}