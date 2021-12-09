/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A control consisting of a text field and a browse button by means of which business functions
 * within the provided ips project and referenced projects can be selected from.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
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
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreRuntimeException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        IIpsSrcFile[] bfSrcFiles = getIpsProject().findIpsSrcFiles(BusinessFunctionIpsObjectType.getInstance());
        if (currentBusinessFunction == null) {
            return bfSrcFiles;
        }
        ArrayList<IIpsSrcFile> remaining = new ArrayList<>();
        IIpsSrcFile currentBfSrcFile = currentBusinessFunction.getIpsSrcFile();
        for (IIpsSrcFile ipsSrcFile : bfSrcFiles) {
            if (!currentBfSrcFile.equals(ipsSrcFile)) {
                remaining.add(ipsSrcFile);
            }
        }
        return remaining.toArray(new IIpsSrcFile[remaining.size()]);
    }

}
