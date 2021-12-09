/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A control to edit policy component type references.
 */
public class TestCaseTypeRefControl extends IpsObjectRefControl {

    public TestCaseTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.TestCaseTypeRefControl_title,
                Messages.TestCaseTypeRefControl_description);
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreRuntimeException {
        ArrayList<IIpsSrcFile> srcFiles = new ArrayList<>();
        for (IIpsProject p : getIpsProjects()) {
            srcFiles.addAll(Arrays.asList(p.findIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE)));
        }
        return srcFiles.toArray(new IIpsSrcFile[srcFiles.size()]);
    }

    /**
     * Returns the test case type identified by the qualified name found in this control's text
     * value. Returns <code>null</code> if the text value does not identify a test case type.
     * 
     * @throws CoreRuntimeException if an error occurs while searching for the test case type.
     */
    public Object findTestCaseType() throws CoreRuntimeException {
        List<IIpsProject> ipsProjects = getIpsProjects();
        if (ipsProjects.isEmpty()) {
            return null;
        }
        return ipsProjects.get(0).findIpsObject(IpsObjectType.TEST_CASE_TYPE, getText());
    }
}
