/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.deltapresentation.AbstractDeltaDialog;
import org.faktorips.devtools.model.testcase.ITestCaseTestCaseTypeDelta;

/**
 * Dialog to display differences between a test case and its test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseDeltaDialog extends AbstractDeltaDialog {

    private TreeViewer tree;

    /** The displayed delta in the dialog */
    private ITestCaseTestCaseTypeDelta delta;

    /**
     * Create a new dialog, showing all the differences given.
     */
    public TestCaseDeltaDialog(ITestCaseTestCaseTypeDelta delta, Shell parentShell) {
        super(parentShell);
        super.setShellStyle(getShellStyle() | SWT.RESIZE);
        this.delta = delta;

        toolkit = new UIToolkit(null);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.TestCaseDeltaDialog_Title);

        Composite root = (Composite)super.createDialogArea(parent);

        setMessage(Messages.TestCaseDeltaDialog_Message, IMessageProvider.INFORMATION);

        // create composite with margins
        Composite listParent = toolkit.createGridComposite(root, 1, false, true);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        listParent.setLayoutData(gridData);

        Label label = toolkit.createLabel(listParent, Messages.TestCaseDeltaDialog_Label_DifferencesTree, true);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        tree = new TreeViewer(listParent);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumHeight = 300;
        tree.getTree().setLayoutData(gridData);

        TestCaseDeltaContentProvider contentProvider = new TestCaseDeltaContentProvider(delta.getTestCase());
        contentProvider.enableTestCaseDeltaViewerFilter(tree);
        tree.setContentProvider(contentProvider);
        tree.setLabelProvider(new TestCaseDeltaLabelProvider(delta.getTestCase().getIpsProject()));
        tree.setInput(delta);

        updateDeltaView();

        return root;
    }

    private void updateDeltaView() {
        updateDeltaView(delta);
    }

    @Override
    protected TreeViewer getTreeViewer() {
        return tree;
    }

}
