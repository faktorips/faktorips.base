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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * This dialog lets the user select a set of <tt>IIpsObjectPart</tt>s from the super type hierarchy
 * of a specified <tt>IIpsObject</tt> in a comfortable way.
 * <p>
 * The dialog is intended to be used by UI editors that are responsible for doing something useful
 * with the user's selection (e. g. the dialog may be used to let the user select a set of methods
 * from super classes of a <tt>IPolicyCmptType</tt> which are then overridden by the currently
 * edited <tt>IPolicyCmptType</tt>).
 * 
 * @author Alexander Weickmann raised abstraction level from former <tt>OverrideDialog</tt> to
 *         <tt>SelectSupertypeHierarchyPartsDialog</tt>.
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
     * Creates a new <tt>SelectSupertypeHierarchyPartsDialog</tt> where <tt>IIpsObjectPart</tt>s
     * available for selection are parts of an <tt>IPolicyCmptType</tt>.
     * 
     * @param pcType The <tt>IPolicyCmptType</tt> to select <tt>IIpsObjectPart</tt>s from.
     * @param parent The parent UI shell to show this dialog in.
     * @param contentProvider The content provider providing this dialog with the available
     *            <tt>IIpsObjectPart</tt>s.
     */
    public SelectSupertypeHierarchyPartsDialog(IPolicyCmptType pcType, Shell parent,
            PartsContentProvider contentProvider) {

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

    /**
     * The <tt>PartsContentProvider</tt> provides the <tt>IIpsObjectPart</tt>s that are available
     * for selection.
     */
    static abstract class PartsContentProvider implements ITreeContentProvider {

        /** The <tt>IIpsObjectPart</tt>s available for selection. */
        private IIpsObjectPart[] availableParts;

        /** The super types from which. */
        private IIpsObject[] supertypes;

        /**
         * Creates a new <tt>PartsContentProvider</tt>.
         * 
         * @param ipsObject The <tt>IIpsObject</tt> the <tt>IIpsObjectPart</tt>s available for
         *            selection belong to.
         */
        PartsContentProvider(IIpsObject ipsObject) {
            try {
                supertypes = getSupertypes(ipsObject);
                availableParts = getAvailableParts(ipsObject);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract IIpsObject[] getSupertypes(IIpsObject ipsObject) throws CoreException;

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IIpsObject) {
                IIpsObject ipsObject = (IIpsObject)parentElement;
                List<IIpsObjectPart> methods = new ArrayList<IIpsObjectPart>();
                for (int i = 0; i < availableParts.length; i++) {
                    if (availableParts[i].getIpsObject().equals(ipsObject)) {
                        methods.add(availableParts[i]);
                    }
                }
                return methods.toArray();
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof IIpsObject) {
                return null;
            }
            if (element instanceof IIpsObjectPart) {
                return ((IIpsObjectPart)element).getParent();
            }
            throw new RuntimeException("Unknown element " + element);
        }

        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        public Object[] getElements(Object inputElement) {
            return supertypes;
        }

        public void dispose() {
            // Nothing to do.
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do.
        }

        protected abstract IIpsObjectPart[] getAvailableParts(IIpsObject ipsObject);

    }

}
