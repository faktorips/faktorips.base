/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;


/**
 *
 */
public class TestIpsObjectFactory {

    public final static IIpsObject createPdObject(IpsObjectType type, String name) throws CoreException {
        IIpsSrcFile file = new IpsSrcFile(null, type.getFileName(name));
        IpsPlugin.getDefault().getManager().putSrcFileContents(file, new IpsSourceFileContents(file, "", "UTF-8"));
        file.markAsClean();
        return type.newObject(file); 
    }
    
    /**
     * 
     */
    private TestIpsObjectFactory() {
    }
    

}
