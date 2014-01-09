/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcase;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;

/**
 * Creates a new test case.
 * 
 * @author Joerg Ortmann
 */
public class NewTestCaseWizard extends NewIpsObjectWizard {

    private TestCasePage typePage;

    public NewTestCaseWizard() {
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewTestCaseWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        typePage = new TestCasePage(selection);
        return typePage;
    }
}
