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

package org.faktorips.devtools.core.ui.bf.properties;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;

/**
 * A control consisting of a text field and a browse button by means of which business functions
 * within the provided ips project and referenced projects can be selected from.
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionRefControl extends IpsObjectRefControl {

    private IBusinessFunction currentBusinessFunction;

    public BusinessFunctionRefControl(Composite parent, UIToolkit toolkit) {
        super(null, parent, toolkit, Messages.BusinessFunctionRefControl_dialogMessage,
                Messages.BusinessFunctionRefControl_title);
    }

    public void setCurrentBusinessFunction(IBusinessFunction businessFunction) {
        currentBusinessFunction = businessFunction;
    }

    @Override
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        IIpsSrcFile[] bfSrcFiles = getIpsProject().findIpsSrcFiles(BusinessFunctionIpsObjectType.getInstance());
        if (currentBusinessFunction == null) {
            return bfSrcFiles;
        }
        ArrayList<IIpsSrcFile> remaining = new ArrayList<IIpsSrcFile>();
        IIpsSrcFile currentBfSrcFile = currentBusinessFunction.getIpsSrcFile();
        for (IIpsSrcFile ipsSrcFile : bfSrcFiles) {
            if (!currentBfSrcFile.equals(ipsSrcFile)) {
                remaining.add(ipsSrcFile);
            }
        }
        return remaining.toArray(new IIpsSrcFile[remaining.size()]);
    }

}
