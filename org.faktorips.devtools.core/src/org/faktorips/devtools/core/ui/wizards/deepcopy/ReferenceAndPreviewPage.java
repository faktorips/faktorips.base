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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.util.message.MessageList;

/**
 * Page to preview the changes to the names of copied products and to switch between
 * a copy or a reference.
 * 
 * @author Thorsten Guenther
 */
public class ReferenceAndPreviewPage extends WizardPage {
	/**
	 * The ID to identify this page.
	 */
	private static final String PAGE_ID = "deepCopyWizard.preview"; //$NON-NLS-1$
	
	/**
	 * The page where the source for the deep copy operation is defined
	 */
	private SourcePage sourcePage;
	
	/**
	 * Structure to represent
	 */
	private IProductCmptStructure structure;
	
	/**
	 * The viewer to display the products to copy
	 */
	private CheckboxTreeViewer tree;
	
	/**
	 * A list of selected objects to restore the state of the ceckboxes from.
	 */
	private Object[] checkState;

	/**
	 * Control for target package.
	 */
	private IpsPckFragmentRefControl targetInput;
	
	/**
	 * Cotnrol for search pattern
	 */
	private Text searchInput;
	
	/**
	 * Control for replace text
	 */
	private Text replaceInput;
	
	/**
	 * Collection of error messages indexed by product components.
	 */
	private Hashtable errorElements;
	
	/**
	 * the type used to create the wizard
	 */
	private int type;
	
	/**
	 * The naming strategy which is to be used to find the correct new names of the
	 * product components to create.
	 */
	private IProductCmptNamingStrategy namingStrategy;
	
	/**
	 * The input field for the user to enter a version id to be used for all newly
	 * created product components.
	 */
	private Text versionId;
	
	private static String getTitle(int type) {
		if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
			return Messages.ReferenceAndPreviewPage_title;
		} else {
			return Messages.ReferenceAndPreviewPage_titleNewVersion;
		}
	}

	/**
	 * Create a new page to show the previously selected products with new names and allow the user
	 * to choose between copy and reference, select the target package, search- and replac-pattern.
	 * 
	 * @param structure The product component structure to copy.
	 * @param sourcePage The page to get the objects selected for copy, the target package and the 
	 * search and replace patterns.
	 * @param type The type used to create the <code>DeepCopyWizard</code>.
	 * 
	 * @throws IllegalArgumentException if the given type is neither DeepCopyWizard.TYPE_COPY_PRODUCT
	 * nor DeepCopyWizard.TYPE_NEW_VERSION.
	 */
	protected ReferenceAndPreviewPage(IProductCmptStructure structure, SourcePage sourcePage, int type) {
		super(PAGE_ID, getTitle(type), null);
		
		if (type != DeepCopyWizard.TYPE_COPY_PRODUCT && type != DeepCopyWizard.TYPE_NEW_VERSION) {
			throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
		}
		this.type = type;

		this.sourcePage = sourcePage;
		this.structure = structure;
		this.setTitle(getTitle(type));
		this.setDescription(Messages.ReferenceAndPreviewPage_description);
		setPageComplete(false);
		this.errorElements = new Hashtable();
		try {
			this.namingStrategy = structure.getRoot().getIpsProject().getProductCmptNamingStratgey();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {

        if (structure == null) {
			Label errormsg = new Label(parent, SWT.WRAP);
			GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
			errormsg.setLayoutData(layoutData);
			errormsg.setText(Messages.ReferenceAndPreviewPage_msgCircleDetected);
			this.setControl(errormsg);
			return;
		}

        UIToolkit toolkit = new UIToolkit(null);

		Composite root = toolkit.createComposite(parent);
		root.setLayout(new GridLayout(1, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(root);

		Composite inputRoot = toolkit.createLabelEditColumnComposite(root);
		
		ChangeListener listener = new ChangeListener(this);
		toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
		toolkit.createFormLabel(inputRoot, IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate());
		toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelTargetPackage);
		targetInput = toolkit.createPdPackageFragmentRefControl(structure.getRoot().getIpsPackageFragment().getRoot(), inputRoot);
		IIpsPackageFragment pack = structure.getRoot().getIpsPackageFragment();
		if (pack.getIpsParentPackageFragment() != null) {
			targetInput.setPdPackageFragment(pack.getIpsParentPackageFragment());
		}
		targetInput.getTextControl().addModifyListener(listener);
		
		
		if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
			toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelSearchPattern);
			searchInput = toolkit.createText(inputRoot);
			searchInput.addModifyListener(listener);
			
			toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelReplacePattern);
			replaceInput = toolkit.createText(inputRoot);
			replaceInput.addModifyListener(listener);

			if (namingStrategy != null && namingStrategy.supportsVersionId()) {
				toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelVersionId);
				versionId = toolkit.createText(inputRoot);
				versionId.addModifyListener(listener);
			} 
		}
		
		tree = new CheckboxTreeViewer(root);
		tree.setLabelProvider(new LabelProvider(tree));
		tree.setContentProvider(new ContentProvider());
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree.addCheckStateListener(new CheckStateListener(this));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			tree.setInput(sourcePage.getCheckedProducts());
			tree.expandAll();
			setCheckedAll(tree.getTree().getItems(), true);
			restoreCheckState();
			setPageComplete();
		}
	}

	private void setCheckedAll(TreeItem[] items, boolean checked) {
		for (int i = 0; i < items.length; i++) {
			items[i].setChecked(checked);
			setCheckedAll(items[i].getItems(), checked);
		}
	}
	
	/**
	 * Due to a bug in ContainerCheckedTreeViewer, we have to set only the leaves
	 * checked.
	 */
	private void restoreCheckState() {
		if (checkState == null) {
			checkState = sourcePage.getCheckedProducts();
		}

		for (int i = 0; i < checkState.length; i++) {
			if (structure.getChildren((IIpsObjectPartContainer)checkState[i]).length == 0)
				tree.setChecked(checkState[i], true);
		}
		
		tree.update(sourcePage.getCheckedProducts(), new String[] {"label"}); //$NON-NLS-1$
	}

	/**
	 * Checks for errors in the user input and sets page complete if no error was found.
	 */
	void setPageComplete() {
		if (isValid()) {
			super.setPageComplete(true);
			super.setMessage(null);
		}
		else {
			super.setPageComplete(false);
			if (getMessage() == null) {
				super.setMessage(Messages.ReferenceAndPreviewPage_msgCopyNotPossible, ERROR);
			}
		}
		
		checkState = tree.getCheckedElements();
		tree.update(sourcePage.getCheckedProducts(), new String[] {"label"}); //$NON-NLS-1$
	}

	/**
	 * Returns all product components to copy.
	 */
	public IProductCmpt[] getProductsToCopy() {
		List allChecked = Arrays.asList(tree.getCheckedElements());
		ArrayList result = new ArrayList();
		
		for (Iterator iter = allChecked.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof IProductCmpt) {
				result.add(element);
			}
		}
		return (IProductCmpt[])result.toArray(new IProductCmpt[result.size()]);
	}

	/**
	 * Returns all product components where a reference to has to be kept.
	 */
	public IProductCmpt[] getProductsToRefer() {
		Object[] checked = sourcePage.getCheckedProducts();
		List toProcess = Arrays.asList(checked);
		List toCopy = Arrays.asList(tree.getCheckedElements());
		ArrayList result = new ArrayList();
		
		for (Iterator iter = toProcess.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			
			if (!toCopy.contains(element) && element instanceof IProductCmpt) {
				result.add(element);
			}
		}
		
		return (IProductCmpt[])result.toArray(new IProductCmpt[result.size()]);
	}

	/**
	 * Calculate the number of <code>IPath</code>-segements which are equal for all product components
	 * to copy.
	 *  
	 * @return 0 if no elements are contained in toCopy, number of all segments, if only one product
	 * component is contained in toCopy and the calculated value as described above for all other cases.
	 */
	private int getSegmentsToIgnore(IProductCmpt[] toCopy) {
		if (toCopy.length == 0) {
			return 0;
		}
		
		IPath refPath = toCopy[0].getIpsPackageFragment().getRelativePath();
		if (toCopy.length == 1) {
			return refPath.segmentCount();
		}
		
		int ignore = Integer.MAX_VALUE;
		for (int i = 1; i < toCopy.length; i++) {
			int tmpIgnore;
			IPath nextPath = toCopy[i].getIpsPackageFragment().getRelativePath();
			tmpIgnore = nextPath.matchingFirstSegments(refPath);
			if (tmpIgnore < ignore) {
				ignore = tmpIgnore;
			}
		}

		return ignore;
	}
	
	/**
	 * Constructs the name of the target package 
	 */
	private String buildTargetPackageName(IIpsPackageFragment targetBase, IProductCmpt source, int segmentsToIgnore) {
		IPath subPath = source.getIpsPackageFragment().getRelativePath().removeFirstSegments(segmentsToIgnore);
		String toAppend = subPath.toString().replace('/', '.');
		
		String base = targetBase.getRelativePath().toString().replace('/', '.');

		if (!base.equals("")) { //$NON-NLS-1$
			base = base + "."; //$NON-NLS-1$
		}
		
		return base +  toAppend;
	}

	/**
	 * Constructs the new name. If at least one of search pattern and replace text is empty, 
	 * the new name is the old name.
	 */
	private String getNewName(String oldName, IProductCmpt productCmpt) {
		String newName = oldName;
		
		if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
			if (namingStrategy != null && namingStrategy.supportsVersionId()) {
				newName = namingStrategy.getKindId(newName);
			}
			String searchPattern = getSearchPattern();
			String replaceText = getReplaceText();
			if (!replaceText.equals("") && !searchPattern.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				newName = newName.replaceAll(searchPattern, replaceText);
			}

			if (namingStrategy != null && namingStrategy.supportsVersionId()) {
				newName = namingStrategy.getProductCmptName(newName, versionId.getText());
			}
		} else if (namingStrategy != null && namingStrategy.supportsVersionId()) {
			newName = namingStrategy.getNextName(productCmpt);
		}
		
		return newName;
	}
	
	/**
	 * Checks for errors in user input. If no erros found, <code>true</code> is returned.
	 */
	private boolean isValid() {
		checkForInvalidTargets();

		if (namingStrategy != null && namingStrategy.supportsVersionId() && type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
			 MessageList ml = namingStrategy.validateVersionId(versionId.getText());
			 if (!ml.isEmpty()) {
				 setMessage(ml.getMessage(0).getText(), ERROR);
				 return false;
			 }
		}
		
		if (getProductsToCopy().length == 0) {
			setMessage(Messages.ReferenceAndPreviewPage_msgSelectAtLeastOneProduct, WARNING);
			return false; 
		}
		
		return errorElements.isEmpty();
	}

	/**
	 * Checks for invalid targets (target names that does not allow to create 
	 * a new product component with this name) and refreshes the map of error messages.
	 */
	private void checkForInvalidTargets() {
		StringBuffer message = new StringBuffer();
		this.errorElements.clear();

		IProductCmpt[] toCopy = getProductsToCopy();
		
		int segmentsToIgnore = getSegmentsToIgnore(toCopy);
		IIpsPackageFragment base = getTargetPackage();

		Hashtable filenameMap = new Hashtable();
		
		for (int i = 0; i < toCopy.length; i++) {
			String packageName = buildTargetPackageName(base, toCopy[i], segmentsToIgnore);
			IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
			if (targetPackage.exists()) {
				String newName = getNewName(toCopy[i].getName(), toCopy[i]);
				IIpsSrcFile file = targetPackage.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(newName));
				if (file.exists()) {
					message = new StringBuffer();
					message.append(Messages.ReferenceAndPreviewPage_msgCanNotCreateFile).append(packageName).append("."); //$NON-NLS-1$
					message.append(newName).append(Messages.ReferenceAndPreviewPage_msgFileAllreadyExists);
					addMessage(toCopy[i], message.toString());
				}
				String name = file.getEnclosingResource().getFullPath().toString();
				if (filenameMap.containsKey(name)) {
					addMessage(toCopy[i], Messages.ReferenceAndPreviewPage_msgNameCollision);
					addMessage((IProductCmpt)filenameMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision);
				}
				else {
					filenameMap.put(name, toCopy[i]);
				}
			}
		}
		
	}
	
	/**
	 * Adds an error message for the given product. If a message allready exists, the
	 * new message is appended.
	 */
	private void addMessage(IProductCmpt product, String msg) {
		if (msg == null || msg.length() == 0) {
			return;
		}
		
		StringBuffer newMessage = new StringBuffer();
		String oldMessage = (String)errorElements.get(product);
		if (oldMessage != null) {
			newMessage.append(oldMessage);
		}
		newMessage.append(msg);

		errorElements.put(product, newMessage.toString());
	}

	/**
	 * Returns the handles for all files to be created to do the deep copy. Note that 
	 * all handles point to non-existing resources.

	 * @throws CoreException if any error exists (e.g. naming collisions).
	 */
	public Map getHandles() throws CoreException {
		checkForInvalidTargets();
		if (!isValid()) {
			StringBuffer message = new StringBuffer();
			Collection errors = errorElements.values();
			for (Iterator iter = errors.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				message.append(element);
			}
			IpsStatus status = new IpsStatus(message.toString());
			throw new CoreException(status);
		}
		
		IProductCmpt[] toCopy = getProductsToCopy();
		Hashtable result = new Hashtable();
		
		int segmentsToIgnore = getSegmentsToIgnore(toCopy);
		IIpsPackageFragment base = getTargetPackage();

		for (int i = 0; i < toCopy.length; i++) {
			String packageName = buildTargetPackageName(base, toCopy[i], segmentsToIgnore);
			IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
			String newName = getNewName(toCopy[i].getName(), toCopy[i]);
			IIpsSrcFile file = targetPackage.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(newName));
			result.put(toCopy[i], file);
		}
		return result;
	}

	/**
	 * Returns the package fragment which is to be used as target package for the copy.
	 */
	public IIpsPackageFragment getTargetPackage() {
		return targetInput.getPdPackageFragment();
	}
	
	/**
	 * Returns the pattern used to find the text to replace. This string is guaranteed to 
	 * be either empty or a valid pattern for java.util.regex.Pattern.
	 */
	public String getSearchPattern() {
		String result = searchInput.getText(); 
		try {
			Pattern.compile(result);
		} catch (PatternSyntaxException e) {
			result = ""; //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * Returns the text to replace the text found by the search pattern.
	 */
	public String getReplaceText() {
		return replaceInput.getText();
	}
	
	/**
	 * Returns whether an error message exists for the given object or not.
	 */
	private boolean isInError(Object object) {
		return errorElements.containsKey(object);
	}
	
	/**
	 * Returns the error message for the given object or <code>null</code>, if
	 * no message exists.
	 */
	private String getErrorMessage(Object object) {
		return (String)errorElements.get(object);
	}

	//#################################################################################
	
	/**
	 * Provides the new names (for selected nodes) and icons showing if a
	 * reference is created (for deselected nodes). 
	 * 
	 * @author Thorsten Guenther
	 */
	private class LabelProvider implements ILabelProvider {
		private CheckboxTreeViewer tree;
		
		public LabelProvider(CheckboxTreeViewer tree) {
			this.tree = tree;
		}
		
		public Image getImage(Object element) {
			if (element instanceof IProductCmpt) {
				if (!tree.getChecked(element)) {
					return IpsPlugin.getDefault().getImage("LinkProductCmpt.gif"); //$NON-NLS-1$
				}
				if (isInError(element)) {
					return IpsPlugin.getDefault().getImage("error_tsk.gif"); //$NON-NLS-1$
				}
			}
			
			return ((IIpsObjectPartContainer)element).getImage();
		}

		public String getText(Object element) {
			if (element instanceof IProductCmpt) {
				String name = ((IProductCmpt)element).getName();
				if (tree.getChecked(element)) {
					name = getNewName(name, (IProductCmpt)element);
				}
				if (isInError(element)) {
					name = name + Messages.ReferenceAndPreviewPage_errorLabelInsert + getErrorMessage(element);
				}
				return name;
			}
			return ((IIpsObjectPartContainer)element).getName();
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	/**
	 * Does only show the nodes which where selected on the source page. As input, an array of 
	 * all the selected nodes of the source page is expected.
	 * 
	 * @author Thorsten Guenther
	 */
	private class ContentProvider implements ITreeContentProvider {

		private Hashtable checkedNodes;
		
		public Object[] getChildren(Object parentElement) {
			IIpsObjectPartContainer[] children = structure.getChildren((IIpsObjectPartContainer)parentElement);
			ArrayList result = new ArrayList();
			for (int i = 0; i < children.length; i++) {
				if (isChecked(children[i]) || !(children[i] instanceof IProductCmpt)) {
					result.add(children[i]);
				}
			}
			return (IIpsObjectPartContainer[])result.toArray(new IIpsObjectPartContainer[result.size()]);
		}

		public Object getParent(Object element) {
			IIpsObjectPartContainer parent = structure.getParent((IIpsObjectPartContainer)element);

			while (!isChecked(parent)) {
				parent = structure.getParent(parent);
			}
			return parent;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IProductCmpt[]) {
				Object[] input = (IProductCmpt[])inputElement;
				if (input.length > 0 && input[0] instanceof IIpsObjectPartContainer && isChecked((IIpsObjectPartContainer)input[0])) {
					return new Object[] {input[0]};
				}
			}
			return new Object[0];
		}

		public void dispose() {
			checkedNodes = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof IProductCmpt[]) {
				IProductCmpt[] selected = (IProductCmpt[])newInput;
				checkedNodes = new Hashtable();
				for (int i = 0; i < selected.length; i ++) {
					checkedNodes.put(selected[i], selected[i]);
				}
			}
		}

		private boolean isChecked(IIpsObjectPartContainer node) {
			return checkedNodes.get(node) != null;
		}
	}

	/**
	 * Listener to update the current completion state of this page.
	 * 
	 * @author Thorsten Guenther
	 */
	private class ChangeListener implements ModifyListener {
		private ReferenceAndPreviewPage page;

		public ChangeListener(ReferenceAndPreviewPage page) {
			this.page = page;
		}

		public void modifyText(ModifyEvent e) {
			page.setPageComplete(); 
		}
	}
	
}

