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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
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
import org.faktorips.devtools.core.internal.model.product.ProductCmptStructure;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptStructure.IStructureNode;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureContentProvider;
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

	private CheckStateListener checkStateListener;
	
	private static String getTitle(int type) {
		if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
			return Messages.ReferenceAndPreviewPage_title;
		} else {
			return NLS.bind(Messages.ReferenceAndPreviewPage_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
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
	protected ReferenceAndPreviewPage(ProductCmptStructure structure, SourcePage sourcePage, int type) {
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
		
		int ignore = getSegmentsToIgnore(structure.toArray(true));
		IIpsPackageFragment pack = structure.getRoot().getIpsPackageFragment();
		int segments = pack.getRelativePath().segmentCount();
		
		if (segments - ignore >= 0) {
			IPath path = pack.getRelativePath().removeLastSegments(segments-ignore);
			pack = pack.getRoot().getIpsPackageFragment(path.toString().replace('/', '.'));
			targetInput.setPdPackageFragment(pack);
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
				String label = NLS.bind(Messages.ReferenceAndPreviewPage_labelVersionId, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
				toolkit.createFormLabel(inputRoot, label);
				versionId = toolkit.createText(inputRoot);
				versionId.addModifyListener(listener);
			} 
		}
		
		tree = new CheckboxTreeViewer(root);
		tree.setLabelProvider(new LabelProvider(tree));
		tree.setContentProvider(new ContentProvider());
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		checkStateListener = new CheckStateListener(this);
		tree.addCheckStateListener(checkStateListener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			((ContentProvider)tree.getContentProvider()).setCheckedNodes(sourcePage.getCheckedNodes());
			tree.setInput(structure);
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
	
	private void restoreCheckState() {
		if (checkState == null) {
			checkState = sourcePage.getCheckedNodes();
		}

		// first uncheck all
		Object checked[] = tree.getCheckedElements();
		for (int i = 0; i < checked.length; i++) {
			tree.setChecked(checked[i], false);
		}

		// than check all items which where remembered as checked.
		for (int i = 0; i < checkState.length; i++) {
			tree.setChecked(checkState[i], true);
		}
		
		IStructureNode root = structure.getRootNode();
		checkStateListener.updateCheckState(tree, root, tree.getChecked(root));
		
		tree.update(sourcePage.getCheckedNodes(), new String[] {"label"}); //$NON-NLS-1$
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
		tree.update(sourcePage.getCheckedNodes(), new String[] {"label"}); //$NON-NLS-1$
	}

	/**
	 * Returns all product components to copy.
	 */
	public IStructureNode[] getProductsToCopy() {
		List allChecked = Arrays.asList(tree.getCheckedElements());
		ArrayList result = new ArrayList();
		
		for (Iterator iter = allChecked.iterator(); iter.hasNext();) {
			IStructureNode element = (IStructureNode)iter.next();
			if (((IStructureNode)element).getWrappedElement() instanceof IProductCmpt) {
				result.add(element);
			}
		}
		return (IStructureNode[])result.toArray(new IStructureNode[result.size()]);
	}

	/**
	 * Returns all product components where a reference to has to be kept.
	 */
	public IStructureNode[] getProductsToRefer() {
		Object[] checked = sourcePage.getCheckedNodes();
		List toProcess = Arrays.asList(checked);
		List toCopy = Arrays.asList(tree.getCheckedElements());
		
		ArrayList result = new ArrayList();
		
		for (Iterator iter = toProcess.iterator(); iter.hasNext();) {
			IStructureNode element = (IStructureNode) iter.next();
			
			if (!toCopy.contains(element) && element.getWrappedElement() instanceof IProductCmpt) {
				result.add(element);
			}
		}
		
		return (IStructureNode[])result.toArray(new IStructureNode[result.size()]);
	}

	/**
	 * Calculate the number of <code>IPath</code>-segements which are equal for all product components
	 * to copy.
	 *  
	 * @return 0 if no elements are contained in toCopy, number of all segments, if only one product
	 * component is contained in toCopy and the calculated value as described above for all other cases.
	 */
	private int getSegmentsToIgnore(IStructureNode[] toCopy) {
		if (toCopy.length == 0) {
			return 0;
		}
		
		IPath refPath = ((IProductCmpt)toCopy[0].getWrappedElement()).getIpsPackageFragment().getRelativePath();
		if (toCopy.length == 1) {
			return refPath.segmentCount();
		}
		
		int ignore = Integer.MAX_VALUE;
		for (int i = 1; i < toCopy.length; i++) {
			int tmpIgnore;
			IPath nextPath = ((IProductCmpt)toCopy[i].getWrappedElement()).getIpsPackageFragment().getRelativePath();
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

		if (!base.equals("") && !toAppend.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
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

		IStructureNode[] toCopy = getProductsToCopy();
		
		int segmentsToIgnore = getSegmentsToIgnore(toCopy);
		IIpsPackageFragment base = getTargetPackage();

		Hashtable filenameMap = new Hashtable();
		
		for (int i = 0; i < toCopy.length; i++) {
			String packageName = buildTargetPackageName(base, (IProductCmpt)toCopy[i].getWrappedElement(), segmentsToIgnore);
			IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
			if (targetPackage.exists()) {
				String newName = getNewName(toCopy[i].getWrappedElement().getName(), (IProductCmpt)toCopy[i].getWrappedElement());
				IIpsSrcFile file = targetPackage.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(newName));
				if (file.exists()) {
					message = new StringBuffer();
					message.append(Messages.ReferenceAndPreviewPage_msgCanNotCreateFile).append(packageName);
					if (!packageName.equals("")) {
						message.append("."); //$NON-NLS-1$
					}
					message.append(newName).append(Messages.ReferenceAndPreviewPage_msgFileAllreadyExists);
					addMessage(toCopy[i], message.toString());
				}
				String name = file.getEnclosingResource().getFullPath().toString();
				IStructureNode node = (IStructureNode)filenameMap.get(name);
				if (node != null && node.getWrappedElement() != toCopy[i].getWrappedElement()) {
					addMessage(toCopy[i], Messages.ReferenceAndPreviewPage_msgNameCollision);
					addMessage((IStructureNode)filenameMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision);
				} else {
					filenameMap.put(name, toCopy[i]);
				}
			}
		}
		
	}
	
	/**
	 * Adds an error message for the given product. If a message allready exists, the
	 * new message is appended.
	 */
	private void addMessage(IStructureNode product, String msg) {
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
	 * all handles point to non-existing resources or, if this condition can not be fullfilled,
	 * a CoreException is thrown.
	 * 
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
		
		IStructureNode[] toCopy = getProductsToCopy();
		Hashtable result = new Hashtable();
		
		int segmentsToIgnore = getSegmentsToIgnore(toCopy);
		IIpsPackageFragment base = getTargetPackage();

		for (int i = 0; i < toCopy.length; i++) {
			String packageName = buildTargetPackageName(base, (IProductCmpt)toCopy[i].getWrappedElement(), segmentsToIgnore);
			IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
			String newName = getNewName(toCopy[i].getWrappedElement().getName(), (IProductCmpt)toCopy[i].getWrappedElement());
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
	private boolean isInError(IStructureNode object) {
		return errorElements.containsKey(object);
	}
	
	/**
	 * Returns the error message for the given object or <code>null</code>, if
	 * no message exists.
	 */
	private String getErrorMessage(IStructureNode object) {
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
		
		private Object getWrapped(Object in) {
			if (in instanceof IStructureNode) {
				return ((IStructureNode)in).getWrappedElement();
			}
			return null;
		}
		
		public Image getImage(Object element) {
			Object wrapped = getWrapped(element);
			if (wrapped instanceof IProductCmpt) {
				
				if (!tree.getChecked(element)) {
					return IpsPlugin.getDefault().getImage("LinkProductCmpt.gif"); //$NON-NLS-1$
				}
				if (isInError((IStructureNode)element)) {
					return IpsPlugin.getDefault().getImage("error_tsk.gif"); //$NON-NLS-1$
				}
			}
			
			return ((IIpsObjectPartContainer)wrapped).getImage();
		}

		public String getText(Object element) {
			Object wrapped = getWrapped(element);
			if (wrapped instanceof IProductCmpt) {
				String name = ((IProductCmpt)wrapped).getName();
				if (tree.getChecked(element)) {
					name = getNewName(name, (IProductCmpt)wrapped);
				}
				if (isInError((IStructureNode)element)) {
					name = name + Messages.ReferenceAndPreviewPage_errorLabelInsert + getErrorMessage((IStructureNode)element);
				}
				return name;
			}
			return ((IIpsObjectPartContainer)wrapped).getName();
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
	private class ContentProvider extends ProductStructureContentProvider {

		private Hashtable checkedNodes;
		
		public ContentProvider() {
			super(true);
		}

		public Object[] getChildren(Object parentElement) {
			IStructureNode[] children = (IStructureNode[])super.getChildren(parentElement);
			ArrayList result = new ArrayList();
			for (int i = 0; i < children.length; i++) {
				if (isChecked(children[i])
						|| (!(children[i].getWrappedElement() instanceof IProductCmpt) && !isUncheckedSubtree(new IStructureNode[] { children[i] }))) {
					result.add(children[i]);
				}
			}
			return (IStructureNode[])result.toArray(new IStructureNode[result.size()]);
		}

		private boolean isUncheckedSubtree(IStructureNode[] children) {
			boolean unchecked = true;
			for (int i = 0; i < children.length && unchecked; i++) {
				if (children[i].getWrappedElement() instanceof IProductCmpt) {
					if (isChecked(children[i])) {
						return false;
					}
				} else if (children[i].getWrappedElement() instanceof IProductCmptTypeRelation) {
					unchecked = unchecked && isUncheckedSubtree(children[i].getChildren());
				}
			}
			return unchecked;
		}
		
		public Object getParent(Object element) {
			if (!(element instanceof IStructureNode)) {
				return null;
			}
			
			IStructureNode parent = ((IStructureNode)element).getParent();

			while (!isChecked(parent)) {
				parent = parent.getParent();
			}
			return parent;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IProductCmptStructure) {
				IStructureNode node = ((IProductCmptStructure)inputElement).getRootNode();
				if (isChecked(node)) {
					return new Object[] {node};
				}
			}
			return new Object[0];
		}

		public void dispose() {
			checkedNodes = null;
		}

		public void setCheckedNodes(IStructureNode[] checked) {
			checkedNodes = new Hashtable();
			for (int i = 0; i < checked.length; i ++) {
				checkedNodes.put(checked[i], checked[i]);
			}
		}

		private boolean isChecked(IStructureNode node) {
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

