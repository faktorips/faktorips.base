/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
import org.faktorips.devtools.core.model.pctype.IMember;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;


public abstract class OverrideDialog extends CheckedTreeSelectionDialog {
    
	
    private int width; 
    private int height;
    
    /**
     * Creates a new dialog to select candidates for overwriting.
     * 
     * @param pcType The type to get the candidates for overwriting from.
     * @param parent The shell to show this dialog in.
     */
    public OverrideDialog(IPolicyCmptType pcType, Shell parent, CandidatesContentProvider contentProvider) {
        super(parent, new DefaultLabelProvider(), contentProvider);
        setTitle(Messages.OverrideMethodDialog_title);
        setSize(80, 30);
        setContainerMode(true);
        setEmptyListMessage(Messages.OverrideMethodDialog_msgEmpty);
		setMessage(null);			
        setInput(new Object());
    }
    
	/**
	 * Sets the size of the tree in unit of characters.
	 * @param width  the width of the tree.
	 * @param height the height of the tree.
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
			
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		GridData gd= null;
		
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing=	convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);			
		composite.setLayout(layout);
						
		Label messageLabel = createMessageArea(composite);			
		if (messageLabel != null) {
			gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan= 2;
			messageLabel.setLayoutData(gd);	
		}
			
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout innerLayout = new GridLayout();
		innerLayout.numColumns= 2;
		innerLayout.marginHeight= 0;
		innerLayout.marginWidth= 0;
		inner.setLayout(innerLayout);
		inner.setFont(parent.getFont());		
			
		CheckboxTreeViewer treeViewer= createTreeViewer(inner);
		
		gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(width);
		gd.heightHint = convertHeightInCharsToPixels(height);
		treeViewer.getControl().setLayoutData(gd);
					
		Composite buttonComposite= createSelectionButtons(inner);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		buttonComposite.setLayoutData(gd);
			
		gd= new GridData(GridData.FILL_BOTH);
		inner.setLayoutData(gd);
		
		gd= new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		applyDialogFont(composite);
					
		return composite;
	}				
	
	/*
	 * @see CheckedTreeSelectionDialog#createTreeViewer(Composite)
	 */
	protected CheckboxTreeViewer createTreeViewer(Composite composite) {
		initializeDialogUnits(composite);
		ViewForm pane= new ViewForm(composite, SWT.BORDER | SWT.FLAT);
		CLabel label= new CLabel(pane, SWT.NONE);
		pane.setTopLeft(label);
		label.setText(Messages.OverrideMethodDialog_labelSelectMethods);
	
		CheckboxTreeViewer treeViewer= super.createTreeViewer(pane);
		pane.setContent(treeViewer.getControl());
		GridLayout paneLayout= new GridLayout();
		paneLayout.marginHeight= 0;
		paneLayout.marginWidth= 0;
		paneLayout.numColumns= 1;
		pane.setLayout(paneLayout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(55);
		gd.heightHint = convertHeightInCharsToPixels(15);
		pane.setLayoutData(gd);

		treeViewer.expandAll();
		treeViewer.getTree().setFocus();
					
		return treeViewer;		
	}		
	
	protected Composite createSelectionButtons(Composite composite) {
		Composite buttonComposite= super.createSelectionButtons(composite);
		GridLayout layout = new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;						
		layout.numColumns= 1;
		buttonComposite.setLayout(layout);						
		return buttonComposite;
	}	
    
    static abstract class CandidatesContentProvider implements ITreeContentProvider {
        
        private IMember[] candidates;
        private IPolicyCmptType[] supertypes;

        CandidatesContentProvider(IPolicyCmptType pcType) {
            try {
                supertypes = pcType.getSupertypeHierarchy().getAllSupertypes(pcType);
                candidates = getCandidates(pcType);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        
        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IPolicyCmptType) {
                IPolicyCmptType type = (IPolicyCmptType)parentElement;
                List methods = new ArrayList();
                for (int i=0; i<candidates.length; i++) {
                    if (candidates[i].getIpsObject().equals(type)) {
                        methods.add(candidates[i]);
                    }
                }
                return methods.toArray();
            }
            return new Object[0];
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            if (element instanceof IPolicyCmptType) {
                return null;
            }
            if (element instanceof IMember) {
                return ((IMember)element).getParent();
            }
            throw new RuntimeException("Unkown element " + element); //$NON-NLS-1$
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            return getChildren(element).length>0;
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            return supertypes;
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            // nothing to do
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }
        
        public abstract IMember[] getCandidates(IPolicyCmptType pcType);
	}
}
