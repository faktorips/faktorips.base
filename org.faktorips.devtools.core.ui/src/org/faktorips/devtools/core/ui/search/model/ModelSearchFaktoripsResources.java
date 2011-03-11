/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;

public class ModelSearchFaktoripsResources {

    public List<IIpsSrcFile> getIpsSourceFiles(List<IIpsProject> projects, IpsObjectType[] filter) throws CoreException {
        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();

        for (IIpsProject project : projects) {
            project.findAllIpsSrcFiles(srcFiles, filter);
        }

        return srcFiles;
    }

    public IType getType(IIpsSrcFile srcFile) throws CoreException {
        IType type = null;
        if (srcFile.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE
                || srcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE) {

            type = (IType)srcFile.getIpsObject();
        }
        return type;
    }
}
