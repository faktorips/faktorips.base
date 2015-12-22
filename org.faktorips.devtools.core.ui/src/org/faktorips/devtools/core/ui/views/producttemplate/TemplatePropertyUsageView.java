/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.faktorips.devtools.core.ui.Messages;

public class TemplatePropertyUsageView {

    /*
     * Eclipse 4 constructor. Requires additional libraries.
     */
    // @Inject
    public TemplatePropertyUsageView(Composite parent) {
        createPartControl(parent);
    }

    private void createPartControl(Composite parent) {
        parent.setLayout(createDefaultLayout());
        SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        sash.setLayout(createDefaultLayout());

        Composite leftSide = new Composite(sash, SWT.NONE);
        leftSide.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        leftSide.setLayout(createTreeCompositeLayout());
        Composite rightSide = new Composite(sash, SWT.NONE);
        rightSide.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        rightSide.setLayout(createTreeCompositeLayout());

        new Label(leftSide, SWT.NONE).setText(Messages.TemplatePropertyUsageView_SameValue_label);
        Tree leftTree = new Tree(leftSide, SWT.BORDER);
        leftTree.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        new Label(rightSide, SWT.NONE).setText(Messages.TemplatePropertyUsageView_DifferingValues_Label);
        Tree rightTree = new Tree(rightSide, SWT.BORDER);
        rightTree.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    }

    private GridLayout createDefaultLayout() {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        return layout;
    }

    private GridLayout createTreeCompositeLayout() {
        GridLayout layout = createDefaultLayout();
        layout.marginTop = 2;
        return layout;
    }

    public void setFocus() {
        // to do
    }

}
