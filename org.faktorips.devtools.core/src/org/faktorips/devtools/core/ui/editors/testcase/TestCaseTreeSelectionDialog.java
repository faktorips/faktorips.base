/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Tree selection dialog to select a element with filter functionality.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTreeSelectionDialog extends SelectionStatusDialog {
	private UIToolkit toolkit;
	
	private TreeViewer treeViewer;
	
	private ITestCase testCase;
	
	private String targetFilter;
	
    private int fWidth = 60;
    private int fHeight = 18;
    
    private boolean isEmpty;
    
    private TestCaseContentProvider contentProvider;
    private TestCaseLabelProvider labelProvider;
    private ViewerFilter filter;

	public TestCaseTreeSelectionDialog(Shell parentShell, UIToolkit toolkit, ITestCase testCase, int contentType, String targetFilter) {
		super(parentShell);
		
		this.toolkit = toolkit;
		this.testCase = testCase;
		this.targetFilter = targetFilter;
		
		this.contentProvider = new TestCaseContentProvider(contentType, testCase);
		this.labelProvider = new TestCaseLabelProvider();
		this.filter = new TestPolicyCmptFilter();
		
        setTitle("Select assoziation target");
        
        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
	}
	
	/**
	 * {@inheritDoc}
	 */
    public int open() {
        isEmpty = evaluateIfTreeEmpty(testCase);
        super.open();
        return getReturnCode();
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        
        Label messageLabel = createMessageArea(composite);

        Composite treeComposite = toolkit.createLabelEditColumnComposite(composite);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        layout.verticalSpacing = 1;
        layout.horizontalSpacing = 1;
        treeComposite.setLayout(layout);
        treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
        TreeViewer treeViewer = createTreeViewer(treeComposite);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(fWidth);
        data.heightHint = convertHeightInCharsToPixels(fHeight);
        Tree treeWidget = treeViewer.getTree();
        treeWidget.setLayoutData(data);
        treeWidget.setFont(parent.getFont());
        if (isEmpty) {
            messageLabel.setEnabled(false);
            treeWidget.setEnabled(false);
        }
        return composite;
    }
    
    /**
     * Creates the tree viewer.
     */
    protected TreeViewer createTreeViewer(Composite parent) {
    	Tree tree = toolkit.getFormToolkit().createTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
    	treeViewer = new TreeViewer(tree);
    	treeViewer.setContentProvider(contentProvider);
    	treeViewer.setLabelProvider(labelProvider);

        if (filter != null)
           treeViewer.addFilter(filter);
        
        treeViewer.setInput(testCase);
        treeViewer.expandAll();
        
        return treeViewer;
    }
    
    /**
     * Returns <code>true</code> if the tree is empty, otherwise <code>false</code>.
     */
    private boolean evaluateIfTreeEmpty(Object input) {
        Object[] elements = contentProvider.getElements(input);
        if (elements.length > 0) {
            if (filter != null) {
            	elements = filter.filter(treeViewer, input, elements);
            }
        }
        return elements.length == 0;
    }
    
	/**
	 * {@inheritDoc}
	 */
	protected void computeResult() {
		ArrayList result = new ArrayList(1);
		if (treeViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection() ;
			result.add(selection.getFirstElement());
		}
		setResult(result);
	}
	
    /**
     * Inner class of filter implementation.
     */
	private class TestPolicyCmptFilter extends ViewerFilter{

		/**
		 * {@inheritDoc}
		 */
		public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
	        int size = elements.length;
	        ArrayList out = new ArrayList(size);
	        for (int i = 0; i < size; ++i) {
	            Object element = elements[i];
	            if (select(viewer, parent, element))
	                out.add(element);
	        }
	        return out.toArray();
		}

		/**
		 * The filter is always active.
		 * 
		 * {@inheritDoc}
		 */
		public boolean isFilterProperty(Object element, String property) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			try {
				if (element instanceof ITestPolicyCmpt){
					return isFilterChildOf((ITestPolicyCmpt)element, targetFilter);
				}else if (element instanceof TestCaseTypeRelation){
					TestCaseTypeRelation dummyRelation = (TestCaseTypeRelation) element;
					ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) dummyRelation.getParentTestPolicyCmpt();
					ITestPolicyCmptRelation childs[] = testPolicyCmpt.getTestPcTypeRelations();
					boolean found = false;
					for (int i = 0; i < childs.length; i++) {
						// because of grouping the relations, get the relations by using the parent test policy component
						ITestPolicyCmptRelation elem = childs[i];
						String relationName = "";
						if (elem.findTarget() != null){
							relationName = elem.findTarget().getTestPolicyCmptType();
							if (relationName.equals(dummyRelation.getName())){
								found = isFilterChildOfRelation(elem, targetFilter);
								if (found)
									return found;
							}
						}
					}
				}
			} catch (CoreException e) {
				// ignore exception and don't display the element
			}
			return false;
		}
	}
	
	/**
	 * Returns <code>true</code> if the to be filtered object is a child of the given test policy component.
	 * If there is no such child object return <code>false</code>.
	 * 
	 * @throws CoreException if an error occurs
	 */
	private boolean isFilterChildOf(ITestPolicyCmpt testPolicyCmpt, String filter)  throws CoreException{
		boolean found = false;
		ITestPolicyCmptRelation[] realtions = testPolicyCmpt.getTestPcTypeRelations();
		for (int i = 0; i < realtions.length; i++) {
			ITestPolicyCmptRelation relation = realtions[i];
			found = isFilterChildOfRelation(relation, filter);
			if (found)
				// exit, at least one relation contains the filtered element
				break;
		}
		ITestPolicyCmptTypeParameter param = null;
		try {
			param = (ITestPolicyCmptTypeParameter) testPolicyCmpt.findTestPolicyCmptType();
			if (param.getPolicyCmptType().equals(targetFilter))
				found = true;
		} catch (CoreException e) {
			// ignored exception and don't display the element
		}

		return found;
	}
	
	/**
	 * Returns <code>true</code> if the to be filtered object is a child of the given relation.
	 * If there is no such child object return <code>false</code>.
	 * 
	 * @throws CoreException if an error occurs
	 */
	private boolean isFilterChildOfRelation(ITestPolicyCmptRelation relation, String filter) throws CoreException{
		boolean found = false;
		ITestPolicyCmpt testPolicyCmpt = relation.findTarget();
		if (!relation.isAccociation() && testPolicyCmpt!=null){
			found = isFilterChildOf(testPolicyCmpt, filter);
		}
		return found;
	}
}
