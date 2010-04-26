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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A control to edit policy component type references.
 */
public class TestCaseTypeRefControl extends IpsObjectRefControl {

    public TestCaseTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.TestCaseTypeRefControl_title,
                Messages.TestCaseTypeRefControl_description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        return getIpsProject().findIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
    }

    /**
     * Returns the test case type identified by the qualified name found in this control's text
     * value. Returns <code>null</code> if the text value does not identify a test case type.
     * 
     * @throws CoreException if an error occurs while searching for the test case type.
     */
    public Object findTestCaseType() throws CoreException {
        IIpsProject project = getIpsProject();
        if (project == null) {
            return null;
        }
        return project.findIpsObject(IpsObjectType.TEST_CASE_TYPE, getText());
    }
}
