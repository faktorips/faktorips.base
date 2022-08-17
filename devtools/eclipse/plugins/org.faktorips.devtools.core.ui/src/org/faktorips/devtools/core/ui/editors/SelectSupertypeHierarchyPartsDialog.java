/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * This dialog lets the user select a set of <code>IIpsObjectPart</code>s from the supertype
 * hierarchy of a specified <code>IIpsObject</code> in a comfortable way.
 * <p>
 * The dialog is intended to be used by UI editors that are responsible for doing something useful
 * with the user's selection (e. g. the dialog may be used to let the user select a set of methods
 * from super classes of a <code>IPolicyCmptType</code> which are then overridden by the currently
 * edited <code>IPolicyCmptType</code>).
 * 
 * @param <T> The type of the {@link IIpsObjectPart}s that shall be selected by the user
 */
public abstract class SelectSupertypeHierarchyPartsDialog<T extends IIpsObjectPart> extends CheckedTreeSelectionDialog {

    /**
     * The width of the UI tree widget showing the available <code>IIpsObjectPartContainer</code>s.
     */
    private int width;

    /**
     * The height of the UI tree widget showing the available <code>IIpsObjectPartContainer</code>s.
     */
    private int height;

    /**
     * A short human-readable message to inform the user what type of <code>IIpsObjectPart</code>s
     * he is now selecting and why.
     * <p>
     * Example:<br />
     * <code>Select methods to override:</code>
     */
    private String selectLabelText;

    /**
     * @param parent The parent <code>Shell</code> to show this dialog in.
     * @param contentProvider A <code>SupertypeHierarchyPartsContentProvider</code> providing this
     *            dialog with available <code>IIpsObjectPart</code>s.
     */
    public SelectSupertypeHierarchyPartsDialog(Shell parent, SupertypeHierarchyPartsContentProvider contentProvider) {
        super(parent, new DefaultLabelProvider(), contentProvider);
        setSize(80, 30);
        setContainerMode(true);
        setMessage(null);
        setInput(new Object());
    }

    /**
     * Sets the size of the tree in unit of characters.
     * 
     * @param width the width of the tree.
     * @param height the height of the tree.
     */
    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        GridData gd = null;

        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);

        Label messageLabel = createMessageArea(composite);
        if (messageLabel != null) {
            gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            gd.horizontalSpan = 2;
            messageLabel.setLayoutData(gd);
        }

        Composite inner = new Composite(composite, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerLayout.marginHeight = 0;
        innerLayout.marginWidth = 0;
        inner.setLayout(innerLayout);
        inner.setFont(parent.getFont());

        CheckboxTreeViewer treeViewer = createTreeViewer(inner);

        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(width);
        gd.heightHint = convertHeightInCharsToPixels(height);
        treeViewer.getControl().setLayoutData(gd);

        Composite buttonComposite = createSelectionButtons(inner);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        buttonComposite.setLayoutData(gd);

        gd = new GridData(GridData.FILL_BOTH);
        inner.setLayoutData(gd);

        gd = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gd);

        applyDialogFont(composite);

        return composite;
    }

    /**
     * Sets the short human-readable message to inform the user what type of
     * <code>IIpsObjectPart</code> s he is now selecting and why.
     * <p>
     * Example:<br>
     * <code>Select methods to override:</code>
     * 
     * @param selectLabelText The short human-readable message to show.
     */
    protected void setSelectLabelText(String selectLabelText) {
        this.selectLabelText = selectLabelText;
    }

    @Override
    protected CheckboxTreeViewer createTreeViewer(Composite composite) {
        initializeDialogUnits(composite);
        ViewForm pane = new ViewForm(composite, SWT.BORDER | SWT.FLAT);
        CLabel label = new CLabel(pane, SWT.NONE);
        pane.setTopLeft(label);
        label.setText(selectLabelText);

        CheckboxTreeViewer treeViewer = super.createTreeViewer(pane);
        pane.setContent(treeViewer.getControl());
        GridLayout paneLayout = new GridLayout();
        paneLayout.marginHeight = 0;
        paneLayout.marginWidth = 0;
        paneLayout.numColumns = 1;
        pane.setLayout(paneLayout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(55);
        gd.heightHint = convertHeightInCharsToPixels(15);
        pane.setLayoutData(gd);

        treeViewer.expandAll();
        treeViewer.getTree().setFocus();

        return treeViewer;
    }

    @Override
    protected Composite createSelectionButtons(Composite composite) {
        Composite buttonComposite = super.createSelectionButtons(composite);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;
        buttonComposite.setLayout(layout);

        return buttonComposite;
    }

    /**
     * Returns the parts the user has selected.
     */
    @SuppressWarnings("unchecked")
    public List<T> getSelectedParts() {
        List<T> parts = new ArrayList<>();
        Object[] checked = getResult();
        for (Object element : checked) {
            if (element instanceof IIpsObjectPart) {
                parts.add((T)element);
            }
        }
        return parts;
    }

}
