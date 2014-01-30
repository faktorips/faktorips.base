/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasetype;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * @author Joerg Ortmann
 */
public class TestCaseTypePage extends IpsObjectPage {

    public TestCaseTypePage(IStructuredSelection selection) {
        super(IpsObjectType.TEST_CASE_TYPE, selection, Messages.TestCaseTypePage_title);
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
    }

    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
    }

}
