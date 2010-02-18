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

package org.faktorips.devtools.core.ui.editors;

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

/**
 * This dialog lets the user select a set of <tt>IIpsObjectPart</tt>s from the supertype hierarchy
 * of a specified <tt>IIpsObject</tt> in a comfortable way.
 * <p>
 * The dialog is intended to be used by UI editors that are responsible for doing something useful
 * with the user's selection (e. g. the dialog may be used to let the user select a set of methods
 * from super classes of a <tt>IPolicyCmptType</tt> which are then overridden by the currently
 * edited <tt>IPolicyCmptType</tt>).
 */
public abstract class SelectSupertypeHierarchyPartsDialog extends CheckedTreeSelectionDialog {

    /** The width of the UI tree widget showing the available <tt>IIpsObjectPartContainer</tt>s. */
    private int width;

    /** The height of the UI tree widget showing the available <tt>IIpsObjectPartContainer</tt>s. */
    private int height;

    /**
     * A short human-readable message to inform the user what type of <tt>IIpsObjectPart</tt>s he is
     * now selecting and why.
     * <p>
     * Example:<br />
     * <tt>Select methods to override:</tt>
     */
    private String selectLabelText;

    /**
     * @param parent The parent <tt>Shell</tt> to show this dialog in.
     * @param contentProvider A <tt>SupertypeHierarchyPartsContentProvider</tt> providing this
     *            dialog with available <tt>IIpsObjectPart</tt>s.
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
     * Sets the short human-readable message to inform the user what type of <tt>IIpsObjectPart</tt>
     * s he is now selecting and why.
     * <p>
     * Example:<br />
     * <tt>Select methods to override:</tt>
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

}
