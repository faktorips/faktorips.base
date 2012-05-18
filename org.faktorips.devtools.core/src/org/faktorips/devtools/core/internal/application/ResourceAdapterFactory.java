/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapter factory for adapting <code>IResource</code>s to <code>IWorbenchAdapter</code>s.
 * <p>
 * To show the CVS History menu item, we must implement an IActionFilter, this code is based on the
 * WorkbenchAdapterFactory.
 * 
 * @author Thorsten Guenther
 */
class ResourceAdapterFactory implements IAdapterFactory {
    // Note: instead of using the internal class WorkbenchFile, we implement an own IActionFilter,
    // this is necessary to display the CVS revision history action in the context menu of any
    // resource
    private Object fileAdapter = new IActionFilter() {

        @Override
        public boolean testAttribute(Object target, String name, String value) {
            // see org.eclipse.ui.internal.ide.model.WorkbenchResource.testAttribute(Object, String,
            // String)
            if (name.equals(org.eclipse.ui.IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY)) {
                return testProperty((IResource)target, true, true, value);
            }
            return false;
        }

    };

    /**
     * Returns the IActionFilter for an object.
     */
    protected Object getActionFilter(Object o) {
        if (o instanceof IResource) {
            switch (((IResource)o).getType()) {
                case IResource.FILE:
                    return fileAdapter;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    public Object getAdapter(Object o, Class adapterType) {
        if (adapterType.isInstance(o)) {
            return o;
        } else if (adapterType == IWorkbenchAdapter.class) {
            return new IpsWorkbenchAdapter();
        } else if (adapterType == IActionFilter.class) {
            return getActionFilter(o);
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    // The Eclipse API uses raw type
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class, IActionFilter.class };
    }

    /**
     * This code is based on the internal method
     * org.eclipse.ui.internal.ide.model.WorkbenchResource#testProperty(IResource, boolean, boolean,
     * String)
     * 
     * Tests whether a session or persistent property on the resource or its project matches the
     * given value.
     * 
     * @param resource the resource to check
     * @param persistentFlag <code>true</code> for a persistent property, <code>false</code> for a
     *            session property
     * @param projectFlag <code>true</code> to check the resource's project, <code>false</code> to
     *            check the resource itself
     * @param value the attribute value, which has either the form "propertyName" or
     *            "propertyName=propertyValue"
     * @return whether there is a match
     */
    private boolean testProperty(IResource resource, boolean persistentFlag, boolean projectFlag, String value) {
        String propertyName;
        String expectedVal;
        int i = value.indexOf('=');
        if (i != -1) {
            propertyName = value.substring(0, i).trim();
            expectedVal = value.substring(i + 1).trim();
        } else {
            propertyName = value.trim();
            expectedVal = null;
        }
        try {
            QualifiedName key;
            int dot = propertyName.lastIndexOf('.');
            if (dot != -1) {
                key = new QualifiedName(propertyName.substring(0, dot), propertyName.substring(dot + 1));
            } else {
                key = new QualifiedName(null, propertyName);
            }
            IResource resToCheck = projectFlag ? resource.getProject() : resource;
            // getProject() on workspace root can be null
            if (resToCheck == null) {
                return false;
            }
            if (persistentFlag) {
                String actualVal = resToCheck.getPersistentProperty(key);
                if (actualVal == null) {
                    return false;
                }
                return expectedVal == null || expectedVal.equals(actualVal);
            }

            Object actualVal = resToCheck.getSessionProperty(key);
            if (actualVal == null) {
                return false;
            }

            return expectedVal == null || expectedVal.equals(actualVal.toString());

        } catch (CoreException e) {
            // ignore
        }
        return false;
    }
}
