/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
        super(new ArrayList<IIpsProject>(), parent, toolkit, Messages.BusinessFunctionRefControl_dialogMessage,
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
