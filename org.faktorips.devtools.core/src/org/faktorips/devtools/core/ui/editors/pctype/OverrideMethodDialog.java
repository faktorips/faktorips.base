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
import org.eclipse.jdt.internal.ui.util.ViewerPane;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;


public class OverrideMethodDialog extends CheckedTreeSelectionDialog {
    
    private int width; 
    private int height;
    
    public OverrideMethodDialog(IPolicyCmptType pcType, Shell parent) {
        super(parent, new DefaultLabelProvider(), new CandidatesContentProvider(pcType));
        setTitle(Messages.OverrideMethodDialog_title);
        setSize(80, 30);
        setContainerMode(true);
        setEmptyListMessage(Messages.OverrideMethodDialog_msgEmpty);
		setMessage(null);			
        setInput(new Object());
        selectAbstractMethods(pcType);
    }
    
    private void selectAbstractMethods(IPolicyCmptType pcType) {
        try {
            // select abstract mehods
            List selected = new ArrayList();
            IMethod[] method = pcType.findOverrideCandidates(false);
            for (int i=0; i<method.length; i++) {
                if (method[i].isAbstract()) {
                    selected.add(method[i]);
                }
            }
            setInitialElementSelections(selected);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
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
	
	/**
	 * Returns the methods the user has selected to override. 
	 */
	public IMethod[] getSelectedMethods() {
	    List methods = new ArrayList();
	    Object[] checked = getResult();
	    for (int i=0; i<checked.length; i++) {
	        if (checked[i] instanceof IMethod) {
	            methods.add(checked[i]);
	        }
	    }
	    return (IMethod[])methods.toArray(new IMethod[methods.size()]);
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
		ViewerPane pane= new ViewerPane(composite, SWT.BORDER | SWT.FLAT);
		pane.setText("Select methods to overrider or implement:"); //$NON-NLS-1$
	
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

		ToolBarManager tbm= pane.getToolBarManager();
		// tbm.add(new OverrideFlatTreeAction()); // create after tree is created
		tbm.update(true);
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
    
    private static class CandidatesContentProvider implements ITreeContentProvider {
        
        private IMethod[] candidates;
        private IPolicyCmptType[] supertypes;

        CandidatesContentProvider(IPolicyCmptType pcType) {
            try {
                supertypes = pcType.getSupertypeHierarchy().getAllSupertypes(pcType);
                candidates = pcType.findOverrideCandidates(false);
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
                    if (candidates[i].getPolicyCmptType().equals(type)) {
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
            if (element instanceof IMethod) {
                return ((IMethod)element).getParent();
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
	}
}
