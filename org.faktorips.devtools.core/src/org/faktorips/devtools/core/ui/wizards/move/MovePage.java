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

package org.faktorips.devtools.core.ui.wizards.move;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to let the user select the target package for the move. 
 * 
 * @author Thorsten Guenther
 */
public class MovePage extends WizardPage implements ModifyListener {

	/**
	 * The input field for the target package. 
	 */
	private TreeViewer targetInput;

	/**
	 * the package root used by the target input.
	 */
	private IIpsProject project;

	/**
	 * The page-id to identify this page.
	 */
	private static final String PAGE_ID = "MoveWizard.move"; //$NON-NLS-1$

	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected MovePage(IIpsElement[] selectedObjects) {
		super(PAGE_ID, Messages.MovePage_title, null);

		// find the package root
		if (selectedObjects[0] instanceof IIpsElement) {
			project = ((IIpsElement) selectedObjects[0]).getIpsProject();
		}

		super.setDescription(Messages.MovePage_description);
		setPageComplete();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		UIToolkit toolkit = new UIToolkit(null);

		Composite root = toolkit.createComposite(parent);
		root.setLayout(new GridLayout(1, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(root);

		toolkit.createFormLabel(root, Messages.MovePage_targetLabel);

		Tree tree = new Tree(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		targetInput = new TreeViewer(tree);
		targetInput.setLabelProvider(new MoveLabelProvider());
		targetInput.setContentProvider(new MoveContentProvider());
		targetInput.setInput(project);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	}

	/**
	 * Set the current completion state (and, if neccessary, messages for the user
	 * to help him to get the page complete).
	 */
	private void setPageComplete() {
		boolean complete = true;

		setMessage(null);

		if (targetInput == null) {
			// page not yet created, so do nothing.
			return;
		}

		Object selected = ((IStructuredSelection) targetInput.getSelection())
				.getFirstElement();
		IIpsPackageFragment pack = (IIpsPackageFragment) selected;

		String name = pack.getFolderName();

		IStatus val = JavaConventions.validatePackageName(name);
		if (val.getSeverity() == IStatus.ERROR) {
			String msg = Messages.bind(Messages.errorNameNotValid, name);
			setMessage(msg, ERROR);
			complete = false;
		} else if (pack != null && !pack.exists()) {
			setMessage(Messages.MovePage_infoPackageWillBeCreated, INFORMATION);
		}

		super.setPageComplete(complete);
	}

	/**
	 * Returns the package selected as target. The returned package is neither guaranteed to exist nor
	 * that it can be created.
	 */
	public IIpsPackageFragment getTarget() {
		Object selected = ((IStructuredSelection) targetInput.getSelection())
				.getFirstElement();
		return (IIpsPackageFragment) selected;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyText(ModifyEvent e) {
		setPageComplete();
	}

	/**
	 * Label provider for the package selection tree used by this move page.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MoveLabelProvider extends LabelProvider {

		/**
		 * {@inheritDoc}
		 */
		public Image getImage(Object element) {
			Image image = null;
			if (element instanceof IIpsPackageFragment) {
				image = IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$
			} else if (element instanceof IIpsElement) {
				image = ((IIpsElement) element).getImage();
			}

			return image;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getText(Object element) {
			String text = null;
			if (element instanceof IIpsPackageFragment) {
				if (((IIpsPackageFragment) element).isDefaultPackage()) {
					return Messages.MovePage_labelDefaultPackage;
				}
				text = ((IIpsPackageFragment) element).getFolderName();
			} else if (element instanceof IIpsElement) {
				text = ((IIpsElement) element).getName();
			}

			return text;
		}

	}

	/**
	 * Content provider for the package selection tree used by this move page.
	 * All packages (including the default package) of one project are examind.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MoveContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parentElement) {

			if (parentElement instanceof IIpsPackageFragment) {
				if (((IIpsPackageFragment) parentElement).isDefaultPackage()) {
					return new Object[0];
				}
				try {
					return ((IIpsPackageFragment) parentElement)
							.getIpsChildPackageFragments();
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object element) {
			if (element instanceof IIpsPackageFragment) {
				return ((IIpsPackageFragment) element).getParent();
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IIpsProject) {
				try {
					IIpsPackageFragmentRoot[] roots = ((IIpsProject) inputElement)
							.getIpsPackageFragmentRoots();
					ArrayList result = new ArrayList();

					for (int i = 0; i < roots.length; i++) {
						IIpsPackageFragment def = roots[i]
								.getIpsDefaultPackageFragment();
						result.add(def);
						result.addAll(Arrays.asList(def
								.getIpsChildPackageFragments()));
					}
					return result.toArray();

				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
			return new Object[0];
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

	}
}
