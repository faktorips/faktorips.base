/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.deltapresentation.AbstractDeltaDialog;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;

/**
 * Dialog to display differences between a product component and its type.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptDeltaDialog extends AbstractDeltaDialog {
    /**
     * Make sure the dialog's initial size is never greater than this. The user can resize, though.
     */
    protected static final int MAX_INITIAL_HEIGHT = 400;

    private IFixDifferencesComposite deltaComposite;
    private TreeViewer tree;

    /**
     * Create a new dialog, showing all the differences given. The first delta found in the given
     * deltaComposite has to be for the first generation and so on.
     * 
     * @param deltaComposite All deltaComposite for the generations.
     * @param parentShell The SWT parent-shell
     */
    public ProductCmptDeltaDialog(IFixDifferencesComposite deltaComposite, Shell parentShell) {
        super(parentShell);
        super.setShellStyle(getShellStyle() | SWT.RESIZE);
        this.deltaComposite = deltaComposite;
    }

    @Override
    protected Point getInitialSize() {
        Point p = super.getInitialSize();
        p.y = Math.min(MAX_INITIAL_HEIGHT, p.y);
        return p;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        String genTextPlural = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNamePlural();
        Composite root = (Composite)super.createDialogArea(parent);

        // layouting

        // create composite with margins
        Composite listParent = toolkit.createGridComposite(root, 3, false, false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 100;
        listParent.setLayoutData(gridData);

        // line 1 (label for tree)
        toolkit.createVerticalSpacer(listParent, 1);
        String text = NLS.bind(Messages.ProductCmptDeltaDialog_labelDifferencesHeader, genTextPlural);
        Label label = toolkit.createLabel(listParent, text, false);
        ((GridData)label.getLayoutData()).minimumHeight = 10;
        toolkit.createVerticalSpacer(listParent, 1);

        // line 2 (tree)
        toolkit.createVerticalSpacer(listParent, 1);
        tree = new TreeViewer(listParent);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 100;
        tree.getTree().setLayoutData(gridData);
        toolkit.createVerticalSpacer(listParent, 1);

        // adding data and behaviour
        getShell().setText(Messages.ProductCmptDeltaDialog_title);

        setMessage(Messages.ProductCmptDeltaDialog_message, IMessageProvider.INFORMATION);

        tree.setContentProvider(new DeltaContentProvider());
        tree.setLabelProvider(new DeltaLabelProvider());

        // initialize view
        // need to set any element different form the deltaComposite itself because the content
        // provider does return the deltaComposite as root element. The root element could not be
        // the same as the input element @see IStructuredContentProvider#getElemement(Object)
        tree.setInput(deltaComposite);
        tree.expandAll();
        tree.setSelection(new StructuredSelection(deltaComposite));

        return root;
    }

    public IFixDifferencesComposite getDeltaComposite() {
        return deltaComposite;
    }

    @Override
    protected TreeViewer getTreeViewer() {
        return tree;
    }

}
