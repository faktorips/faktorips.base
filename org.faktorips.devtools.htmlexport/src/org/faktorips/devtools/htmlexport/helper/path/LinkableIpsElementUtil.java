/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper.path;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.Description;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

public class LinkableIpsElementUtil {
    private final static Collection<Class<? extends Object>> notLinkableClasses = Arrays
            .asList(new Class<?>[] { String.class });

    public IIpsSrcFile getLinkableSrcFile(Object object, DocumentationContext context) {
        if (notLinkableClasses.contains(object.getClass())) {
            return null;
        }
        if (object instanceof IIpsSrcFile) {
            return (IIpsSrcFile)object;
        }
        if (object instanceof Description) {
            return getLinkableSrcFile(((Description)object).getParent(), context);
        }
        if (object instanceof IIpsObjectPartContainer) {
            return ((IIpsObjectPartContainer)object).getIpsSrcFile();
        }

        context.addStatus(new IpsStatus(IStatus.WARNING, "No IpsSrcFile for class " + object.getClass())); //$NON-NLS-1$
        return null;
    }
}
