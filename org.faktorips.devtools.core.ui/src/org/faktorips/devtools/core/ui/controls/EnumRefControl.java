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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

public class EnumRefControl extends IpsObjectRefControl {

    // TODO rg: superType
    public EnumRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, boolean chooseSuperEnumType) {
        super(project, parent, toolkit, "Title", "Message");
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject()==null) {
            return new IIpsSrcFile[0];
        }

        IIpsSrcFile[] contents = getIpsProject().findIpsSrcFiles(IpsObjectType.ENUM_CONTENT);            
        IIpsSrcFile[] types = getIpsProject().findIpsSrcFiles (IpsObjectType.ENUM_TYPE);
        if (contents.length == 0 || types.length == 0) {
            return contents.length == 0 ? types : contents;
        }
            
        IIpsSrcFile[] result = new IIpsSrcFile[contents.length + types.length];
        for (int i = 0; i < contents.length; i++) {
            result[i] = contents[i];
        }
        for (int i = 0; i < contents.length; i++) {
            result[i + contents.length] = types[i];
        }

        return result;
    }

    public IEnumValueContainer getEnum() {
            IEnumValueContainer enumType = null;
            try {
                enumType = (IEnumValueContainer)getIpsProject().findIpsObject(IpsObjectType.ENUM_TYPE, getText());
                if (enumType == null) {
                    return (IEnumValueContainer)getIpsProject().findIpsObject(IpsObjectType.ENUM_CONTENT, getText());
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return enumType;
    }
}