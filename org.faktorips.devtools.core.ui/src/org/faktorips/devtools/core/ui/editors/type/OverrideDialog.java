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

package org.faktorips.devtools.core.ui.editors.type;

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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * Base class for override dialogs.
 * 
 * @author Jan Ortmann
 */
public abstract class OverrideDialog extends CheckedTreeSelectionDialog {
    
    private int width; 
    private int height;
    private String selectLabelText;
    
    /**
     * Creates a new dialog to select candidates for overwriting.
     * 
     * @param type The type to get the candidates for overwriting from.
     * @param parent The shell to show this dialog in.
     */
    public OverrideDialog(IType type, Shell parent, CandidatesContentProvider contentProvider) {
        super(parent, new DefaultLabelProvider(), contentProvider);
        setSize(80, 30);
        setContainerMode(true);
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
	
    protected void setSelectLabelText(String selectLabelText) {
        this.selectLabelText = selectLabelText;
    }
    
	/*
	 * @see CheckedTreeSelectionDialog#createTreeViewer(Composite)
	 */
	protected CheckboxTreeViewer createTreeViewer(Composite composite) {
		initializeDialogUnits(composite);
		ViewForm pane= new ViewForm(composite, SWT.BORDER | SWT.FLAT);
		CLabel label= new CLabel(pane, SWT.NONE);
		pane.setTopLeft(label);
		label.setText(selectLabelText);
	
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
        
        private IIpsObjectPart[] candidates;
        private Object[] supertypes;

        CandidatesContentProvider(IType type) {
            try {
                IIpsProject project = type.getIpsProject();
                SupertypesCollector collector = new SupertypesCollector(project);
                collector.start(type.findSupertype(project));
                supertypes = collector.supertypes.toArray();
                candidates = getCandidates(type);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        
        /** 
         * {@inheritDoc}
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IType) {
                IType type = (IType)parentElement;
                List<IIpsObjectPart> methods = new ArrayList<IIpsObjectPart>();
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
         * {@inheritDoc}
         */
        public Object getParent(Object element) {
            if (element instanceof IType) {
                return null;
            }
            if (element instanceof IIpsObjectPart) {
                return ((IIpsObjectPart)element).getParent();
            }
            throw new RuntimeException("Unknown element " + element); //$NON-NLS-1$
        }

        /** 
         * {@inheritDoc}
         */
        public boolean hasChildren(Object element) {
            return getChildren(element).length>0;
        }

        /** 
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            return supertypes;
        }

        /** 
         * {@inheritDoc}
         */
        public void dispose() {
            // nothing to do
        }

        /** 
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }
        
        public abstract IIpsObjectPart[] getCandidates(IType type);
	}
    
    static class SupertypesCollector extends TypeHierarchyVisitor {

        private List<IType> supertypes = new ArrayList<IType>();
        
        public SupertypesCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }
        
        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            supertypes.add(currentType);
            return true;
        }

    }
}
