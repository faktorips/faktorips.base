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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to preview the changes to the names of copied products and to switch between a copy or a
 * reference.
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
     * Collection of error messages indexed by product components.
     */
    private Hashtable errorElements;
    
    /**
     * Mapping of filenames to product references. Used for error-handling.
     */
    private Hashtable filename2productMap;
    
    /**
     * Mapping of product references to filenames. Used for error-handling.
     */
    private Hashtable product2filenameMap;

    /**
     * the type of the wizard displaying this page. Used to show different titles for different types.
     */
    private int type;

    /**
     * Listener to handle check-modifications
     */
    private CheckStateListener checkStateListener;

    /**
     * @param type The type of the wizard displaying this page.
     * @return The title for this page - which depends on the given type.
     */
    private static String getTitle(int type) {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return Messages.ReferenceAndPreviewPage_title;
        }
        else {
            return NLS.bind(Messages.ReferenceAndPreviewPage_titleNewVersion, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        }
    }

    /**
     * Create a new page to show the previously selected products with new names and allow the user
     * to choose between copy and reference, select the target package, search- and replac-pattern.
     * 
     * @param structure The product component structure to copy.
     * @param sourcePage The page to get the objects selected for copy, the target package and the
     *            search and replace patterns.
     * @param type The type used to create the <code>DeepCopyWizard</code>.
     * 
     * @throws IllegalArgumentException if the given type is neither
     *             DeepCopyWizard.TYPE_COPY_PRODUCT nor DeepCopyWizard.TYPE_NEW_VERSION.
     */
    protected ReferenceAndPreviewPage(ProductCmptStructure structure, SourcePage sourcePage, int type) {
        super(PAGE_ID, getTitle(type), null);

        if (type != DeepCopyWizard.TYPE_COPY_PRODUCT && type != DeepCopyWizard.TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }

        this.filename2productMap = new Hashtable();
        this.product2filenameMap = new Hashtable();
        this.type = type;

        this.sourcePage = sourcePage;
        this.structure = structure;
        this.setTitle(getTitle(type));
        this.setDescription(Messages.ReferenceAndPreviewPage_description);
        setPageComplete(false);
        this.errorElements = new Hashtable();
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

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        toolkit.createFormLabel(inputRoot, IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate());

        tree = new CheckboxTreeViewer(root);
        tree.setUseHashlookup(true);
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
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
            pmd.setOpenOnRun(true);
            try {
                getWizard().getContainer().run(false, false, new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        if (monitor == null) {
                            monitor = new NullProgressMonitor();
                        }
                        monitor.beginTask(Messages.ReferenceAndPreviewPage_msgValidateCopy, 6);
                        ((ContentProvider)tree.getContentProvider()).setCheckedNodes(sourcePage.getCheckedNodes());
                        monitor.worked(1);
                        tree.setInput(structure);
                        monitor.worked(1);
                        tree.expandAll();
                        monitor.worked(1);
                        checkAll();
                        monitor.worked(1);
                        restoreCheckState();
                        monitor.worked(1);
                        setPageComplete(null, true);
                        monitor.worked(1);
                    }

                });
            }
            catch (InvocationTargetException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            catch (InterruptedException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    private void checkAll() {
        tree.removeCheckStateListener(checkStateListener);
        setCheckedAll(tree.getTree().getItems(), true);
        tree.addCheckStateListener(checkStateListener);
    }

    private void setCheckedAll(TreeItem[] items, boolean checked) {
        for (int i = 0; i < items.length; i++) {
            items[i].setChecked(checked);
            setCheckedAll(items[i].getItems(), checked);
        }
    }

    private void restoreCheckState() {
        tree.removeCheckStateListener(checkStateListener);
        if (checkState == null) {
            checkState = sourcePage.getCheckedNodes();
        }

        tree.setCheckedElements(checkState);

        IProductCmptReference root = structure.getRoot();
        checkStateListener.updateCheckState(tree, root, tree.getChecked(root));

        // tree.update(sourcePage.getCheckedNodes(), new String[] {"label"}); //$NON-NLS-1$
        tree.addCheckStateListener(checkStateListener);
    }

    /**
     * Checks for errors in the user input and sets page complete if no error was found.
     */
    void setPageComplete(IProductCmptReference modified, boolean checked) {
        if (isValid(modified, checked)) {
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
        if (modified == null) {
            tree.update(sourcePage.getCheckedNodes(), new String[] { "label" }); //$NON-NLS-1$
        }
        else {
            tree.update(modified, new String[] { "label" }); //$NON-NLS-1$
        }
    }

    /**
     * Returns all product components to copy.
     */
    public IProductCmptReference[] getProductsToCopy() {
        List allChecked = Arrays.asList(tree.getCheckedElements());
        ArrayList result = new ArrayList();

        for (Iterator iter = allChecked.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element instanceof IProductCmptReference) {
                result.add(element);
            }
        }
        return (IProductCmptReference[])result.toArray(new IProductCmptReference[result.size()]);
    }

    /**
     * Returns all product components where a reference to has to be kept.
     */
    public IProductCmptReference[] getProductsToRefer() {
        Object[] checked = sourcePage.getCheckedNodes();
        List toProcess = Arrays.asList(checked);
        List toCopy = Arrays.asList(tree.getCheckedElements());

        ArrayList result = new ArrayList();

        for (Iterator iter = toProcess.iterator(); iter.hasNext();) {
            Object element = iter.next();

            if (!toCopy.contains(element) && element instanceof IProductCmptReference) {
                result.add(element);
            }
        }

        return (IProductCmptReference[])result.toArray(new IProductCmptReference[result.size()]);
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

        return base + toAppend;
    }

    /**
     * Constructs the new name. If at least one of search pattern and replace text is empty, the new
     * name is the old name.
     */
    private String getNewName(String oldName, IProductCmpt productCmpt) {
        String newName = oldName;
        IProductCmptNamingStrategy namingStrategy = sourcePage.getNamingStrategy();

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            if (namingStrategy != null && namingStrategy.supportsVersionId()) {
                newName = namingStrategy.getKindId(newName);
            }
            String searchPattern = sourcePage.getSearchPattern();
            String replaceText = sourcePage.getReplaceText();
            if (!replaceText.equals("") && !searchPattern.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                newName = newName.replaceAll(searchPattern, replaceText);
            }

            if (namingStrategy != null && namingStrategy.supportsVersionId()) {
                newName = namingStrategy.getProductCmptName(newName, sourcePage.getVersion());
            }
        }
        else if (namingStrategy != null) {
            newName = namingStrategy.getNextName(productCmpt);
        } else {
            // programming error, should be assert before this page will be displayed
            throw new RuntimeException(
                    "No naming strategy exists, therefore the new product components couldn't be copied with the same name in the same directory!"); //$NON-NLS-1$
        }

        return newName;
    }

    /**
     * Checks for errors in user input. If no erros found, <code>true</code> is returned.
     */
    private boolean isValid(IProductCmptReference modified, boolean checked) {
        if (modified != null) {
            checkForInvalidTarget(modified, checked);
        }
        else {
            checkForInvalidTargets();
        }

        if (getProductsToCopy().length == 0) {
            setMessage(Messages.ReferenceAndPreviewPage_msgSelectAtLeastOneProduct, WARNING);
            return false;
        }

        return errorElements.isEmpty();
    }

    private void checkForInvalidTarget(IProductCmptReference modified, boolean checked) {
        errorElements.remove(modified);
        String name = (String)product2filenameMap.remove(modified);
        if (name != null) {
            filename2productMap.remove(name);
        }

        if (!checked) {
            return;
        }

        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(getProductsToCopy());
        IIpsPackageFragment base = sourcePage.getTargetPackage();
        validateTarget(modified, segmentsToIgnore, base);
    }

    /**
     * Checks for invalid targets (target names that does not allow to create a new product
     * component with this name) and refreshes the map of error messages.
     */
    private void checkForInvalidTargets() {
        errorElements.clear();
        filename2productMap = new Hashtable();
        product2filenameMap = new Hashtable();

        IProductCmptReference[] toCopy = getProductsToCopy();
        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();

        for (int i = 0; i < toCopy.length; i++) {
            validateTarget(toCopy[i], segmentsToIgnore, base);
        }
    }
    
    private void validateTarget(IProductCmptReference modified, int segmentsToIgnore, IIpsPackageFragment base) {
        StringBuffer message = new StringBuffer();
        String packageName = buildTargetPackageName(base, modified.getProductCmpt(), segmentsToIgnore);
        IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
        if (targetPackage.exists()) {
            String newName = getNewName(modified.getProductCmpt().getName(), modified.getProductCmpt());
            IIpsSrcFile file = targetPackage.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(newName));
            if (file.exists()) {
                message = new StringBuffer();
                message.append(Messages.ReferenceAndPreviewPage_msgCanNotCreateFile).append(packageName);
                if (!packageName.equals("")) { //$NON-NLS-1$
                    message.append("."); //$NON-NLS-1$
                }
                message.append(newName).append(Messages.ReferenceAndPreviewPage_msgFileAllreadyExists);
                addMessage(modified, message.toString());
            }
            String name = file.getEnclosingResource().getFullPath().toString();
            IProductCmptReference node = (IProductCmptReference)filename2productMap.get(name);
            if (node != null && node.getProductCmpt() != modified.getProductCmpt()) {
                addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision);
                addMessage((IProductCmptReference)filename2productMap.get(name),
                        Messages.ReferenceAndPreviewPage_msgNameCollision);
            }
            else {
                filename2productMap.put(name, modified);
                product2filenameMap.put(modified, name);
            }
        }
    }

    /**
     * Adds an error message for the given product. If a message allready exists, the new message is
     * appended.
     */
    private void addMessage(IProductCmptStructureReference product, String msg) {
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
     * Returns the handles for all files to be created to do the deep copy. Note that all handles
     * point to non-existing resources or, if this condition can not be fullfilled, a CoreException
     * is thrown.
     * 
     * @throws CoreException if any error exists (e.g. naming collisions).
     */
    public Map getHandles() throws CoreException {
        if (!isValid(null, true)) {
            StringBuffer message = new StringBuffer();
            Collection errors = errorElements.values();
            for (Iterator iter = errors.iterator(); iter.hasNext();) {
                String element = (String)iter.next();
                message.append(element);
            }
            IpsStatus status = new IpsStatus(message.toString());
            throw new CoreException(status);
        }

        IProductCmptReference[] toCopy = getProductsToCopy();
        Hashtable result = new Hashtable();

        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();

        for (int i = 0; i < toCopy.length; i++) {
            String packageName = buildTargetPackageName(base, toCopy[i].getProductCmpt(), segmentsToIgnore);
            IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
            String newName = getNewName(toCopy[i].getProductCmpt().getName(), toCopy[i].getProductCmpt());
            IIpsSrcFile file = targetPackage.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(newName));
            result.put(toCopy[i], file);
        }
        return result;
    }

    /**
     * Returns whether an error message exists for the given object or not.
     */
    private boolean isInError(IProductCmptStructureReference object) {
        return errorElements.containsKey(object);
    }

    /**
     * Returns the error message for the given object or <code>null</code>, if no message exists.
     */
    private String getErrorMessage(IProductCmptStructureReference object) {
        return (String)errorElements.get(object);
    }

    // #################################################################################

    /**
     * Provides the new names (for selected nodes) and icons showing if a reference is created (for
     * deselected nodes).
     * 
     * @author Thorsten Guenther
     */
    private class LabelProvider implements ILabelProvider {
        private CheckboxTreeViewer tree;

        public LabelProvider(CheckboxTreeViewer tree) {
            this.tree = tree;
        }

        private Object getWrapped(Object in) {
            if (in instanceof IProductCmptReference) {
                return ((IProductCmptReference)in).getProductCmpt();
            }
            else if (in instanceof IProductCmptTypeRelationReference) {
                return ((IProductCmptTypeRelationReference)in).getRelation();
            }
            return null;
        }

        public Image getImage(Object element) {
            Object wrapped = getWrapped(element);
            if (wrapped instanceof IProductCmpt) {

                if (!tree.getChecked(element)) {
                    return IpsPlugin.getDefault().getImage("LinkProductCmpt.gif"); //$NON-NLS-1$
                }
                if (isInError((IProductCmptStructureReference)element)) {
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
                if (isInError((IProductCmptStructureReference)element)) {
                    name = name + Messages.ReferenceAndPreviewPage_errorLabelInsert
                            + getErrorMessage((IProductCmptStructureReference)element);
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
     * Does only show the nodes which where selected on the source page. As input, an array of all
     * the selected nodes of the source page is expected.
     * 
     * @author Thorsten Guenther
     */
    private class ContentProvider extends DeepCopyContentProvider {

        private Hashtable checkedNodes;

        public ContentProvider() {
            super(true);
        }

        public Object[] getChildren(Object parentElement) {
            IProductCmptStructureReference[] children = (IProductCmptStructureReference[])super
                    .getChildren(parentElement);
            ArrayList result = new ArrayList();
            for (int i = 0; i < children.length; i++) {
                if (isChecked(children[i])
                        || (!(children[i] instanceof IProductCmptReference) && !isUncheckedSubtree(new IProductCmptStructureReference[] { children[i] }))) {
                    result.add(children[i]);
                }
            }
            return (IProductCmptStructureReference[])result.toArray(new IProductCmptStructureReference[result.size()]);
        }

        private boolean isUncheckedSubtree(IProductCmptStructureReference[] children) {
            boolean unchecked = true;
            for (int i = 0; i < children.length && unchecked; i++) {
                if (children[i] instanceof IProductCmptReference) {
                    if (isChecked(children[i])) {
                        return false;
                    }
                }
                else if (children[i] instanceof IProductCmptTypeRelationReference) {
                    unchecked = unchecked && isUncheckedSubtree(structure.getChildProductCmptReferences(children[i]));
                }
            }
            return unchecked;
        }

        public Object getParent(Object element) {
            Object parent = super.getParent(element);

            if (parent == null) {
                return null;
            }

            while (!isChecked((IProductCmptStructureReference)parent)) {
                parent = super.getParent(parent);
            }
            return parent;
        }

        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProductCmptStructure) {
                IProductCmptReference node = ((IProductCmptStructure)inputElement).getRoot();
                if (isChecked(node)) {
                    return new Object[] { node };
                }
            }
            return new Object[0];
        }

        public void dispose() {
            checkedNodes = null;
        }

        public void setCheckedNodes(IProductCmptStructureReference[] checked) {
            checkedNodes = new Hashtable();
            for (int i = 0; i < checked.length; i++) {
                checkedNodes.put(checked[i], checked[i]);
            }
        }

        private boolean isChecked(IProductCmptStructureReference node) {
            return checkedNodes.get(node) != null;
        }
    }
}
