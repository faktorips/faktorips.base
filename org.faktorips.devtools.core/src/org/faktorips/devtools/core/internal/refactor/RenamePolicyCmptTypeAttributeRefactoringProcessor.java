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

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

public final class RenamePolicyCmptTypeAttributeRefactoringProcessor extends RenameRefactoringProcessor {

    public RenamePolicyCmptTypeAttributeRefactoringProcessor(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        super(policyCmptTypeAttribute);
    }

    @Override
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)getIpsObjectPartContainer();

        // Change the name of the attribute.
        String oldAttributeName = policyCmptTypeAttribute.getName();
        policyCmptTypeAttribute.setName(getNewName());
        policyCmptTypeAttribute.getIpsSrcFile().save(true, pm);

        // Search and change all test case types dependent on the attribute.
        IIpsSrcFile[] testSrcFiles = policyCmptTypeAttribute.getIpsProject().findIpsSrcFiles(
                IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            ITestPolicyCmptTypeParameter[] testParameters = testCaseType.getTestPolicyCmptTypeParameters();
            for (ITestPolicyCmptTypeParameter parameter : testParameters) {
                ITestAttribute attribute = parameter.getTestAttribute(oldAttributeName);
                if (attribute != null) {
                    attribute.setAttribute(policyCmptTypeAttribute);
                }
            }
            if (ipsSrcFile.isDirty()) {
                ipsSrcFile.save(true, pm);
            }
        }
    }

    @Override
    public String getIdentifier() {
        return "RenamePolicyCmptTypeAttributeRefactoringProcessor";
    }

    @Override
    public String getProcessorName() {
        return "RenamePolicyCmptTypeAttributeRefactoringProcessor";
    }

}
