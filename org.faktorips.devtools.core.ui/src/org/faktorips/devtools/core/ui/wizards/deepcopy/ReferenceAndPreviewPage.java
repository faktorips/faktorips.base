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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Page to preview the changes to the names of copied products and to switch between a copy or a
 * reference.
 * 
 * @author Thorsten Guenther
 */
public class ReferenceAndPreviewPage extends WizardPage {

    // The ID to identify this page.
    private static final String PAGE_ID = "deepCopyWizard.preview"; //$NON-NLS-1$

    // The page where the source for the deep copy operation is defined
    private SourcePage sourcePage;

    // Structure to represent
    private IProductCmptTreeStructure structure;

    // The viewer to display the products to copy
    private CheckboxTreeViewer tree;

    // A list of selected objects to restore the state of the ceckboxes from.
    private Object[] checkState;

    // Collection of error messages indexed by product components.
    private Hashtable<IProductCmptStructureReference, String> errorElements;

    // Mapping of filenames to product references. Used for error-handling.
    private Hashtable<String, IProductCmptStructureReference> filename2productMap;

    // Mapping of product references to filenames. Used for error-handling.
    private Hashtable<IProductCmptStructureReference, String> product2filenameMap;

    // The type of the wizard displaying this page. Used to show different titles for different
    // types.
    private int type;

    // Listener to handle check-modifications
    private CheckStateListener checkStateListener;

    // Label shows the current working date
    private Label workingDateLabel;

    /*
     * @param type The type of the wizard displaying this page.
     * 
     * @return The title for this page - which depends on the given type.
     */
    private static String getTitle(int type) {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return Messages.ReferenceAndPreviewPage_title;
        } else {
            return NLS.bind(Messages.ReferenceAndPreviewPage_titleNewVersion, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        }
    }

    /**
     * Create a new page to show the previously selected products with new names and allow the user
     * to choose between copy and reference, select the target package, search- and replace-pattern.
     * 
     * @param structure The product component structure to copy.
     * @param sourcePage The page to get the objects selected for copy, the target package and the
     *            search and replace patterns.
     * @param type The type used to create the <code>DeepCopyWizard</code>.
     * 
     * @throws IllegalArgumentException if the given type is neither
     *             DeepCopyWizard.TYPE_COPY_PRODUCT nor DeepCopyWizard.TYPE_NEW_VERSION.
     */
    protected ReferenceAndPreviewPage(ProductCmptTreeStructure structure, SourcePage sourcePage, int type) {
        super(PAGE_ID, getTitle(type), null);

        if (type != DeepCopyWizard.TYPE_COPY_PRODUCT && type != DeepCopyWizard.TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }

        filename2productMap = new Hashtable<String, IProductCmptStructureReference>();
        product2filenameMap = new Hashtable<IProductCmptStructureReference, String>();
        this.type = type;

        this.sourcePage = sourcePage;
        this.structure = structure;
        setTitle(getTitle(type));
        setDescription(Messages.ReferenceAndPreviewPage_description);
        setPageComplete(false);
        errorElements = new Hashtable<IProductCmptStructureReference, String>();
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
            setControl(errormsg);
            return;
        }

        UIToolkit toolkit = new UIToolkit(null);

        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        workingDateLabel = toolkit.createFormLabel(inputRoot, ""); //$NON-NLS-1$
        updateWorkingDateLabel();

        tree = new CheckboxTreeViewer(root);
        tree.setUseHashlookup(true);
        tree.setLabelProvider(new LabelProvider(tree));
        tree.setContentProvider(new ContentProvider());
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        checkStateListener = new CheckStateListener(this);
        tree.addCheckStateListener(checkStateListener);
    }

    private void updateWorkingDateLabel() {
        workingDateLabel.setText(getDeepCopyWizard().getFormattedStructureDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        structure = getDeepCopyWizard().getStructure();
        if (visible) {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
            pmd.setOpenOnRun(true);
            try {
                getWizard().getContainer().run(false, false, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        updateWorkingDateLabel();

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
            } catch (InvocationTargetException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            } catch (InterruptedException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        } else {
            // if the page is invisible reset the page complete state (because the finishing state
            // must be check
            // if the page will be visible again (e.g. if the page was completed and finishing is
            // enabled and the version id has changed on the first page, we must perform the
            // finishing check here again,
            // otherwise the user can finish on the page before without the validating on this
            // page!)
            super.setPageComplete(false);
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
     * Resets the check state
     */
    void resetCheckState() {
        checkState = null;
    }

    /**
     * Checks for errors in the user input and sets page complete if no error was found.
     */
    void setPageComplete(IProductCmptReference modified, boolean checked) {
        super.setMessage(null);

        if (isValid(modified, checked)) {
            super.setPageComplete(true);
        } else {
            super.setPageComplete(false);
            if (getMessage() == null) {
                super.setMessage(Messages.ReferenceAndPreviewPage_msgCopyNotPossible, ERROR);
            }
        }

        checkState = tree.getCheckedElements();
        if (modified == null) {
            tree.update(sourcePage.getCheckedNodes(), new String[] { "label" }); //$NON-NLS-1$
        } else {
            tree.update(modified, new String[] { "label" }); //$NON-NLS-1$
        }
    }

    /**
     * Returns all product cmpt structure reference components to copy. References to product cmpts
     * and table contents are returned.
     */
    public IProductCmptStructureReference[] getProductCmptStructRefToCopy() {
        List<Object> allChecked = Arrays.asList(tree.getCheckedElements());
        List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();

        for (Iterator<Object> iter = allChecked.iterator(); iter.hasNext();) {
            IProductCmptStructureReference element = (IProductCmptStructureReference)iter.next();
            if (element instanceof IProductCmptReference) {
                result.add(element);
            } else if (element instanceof IProductCmptStructureTblUsageReference) {
                result.add(element);
            }
        }
        return result.toArray(new IProductCmptStructureReference[result.size()]);
    }

    /**
     * Returns all product components where a reference to has to be kept.
     */
    public IProductCmptStructureReference[] getProductsOrtTableContentsToRefer() {
        Object[] checked = sourcePage.getCheckedNodes();
        List<Object> toProcess = Arrays.asList(checked);
        List<Object> toCopy = Arrays.asList(tree.getCheckedElements());

        List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();

        for (Iterator<Object> iter = toProcess.iterator(); iter.hasNext();) {
            Object element = iter.next();

            if (!toCopy.contains(element) && element instanceof IProductCmptStructureTblUsageReference) {
                result.add((IProductCmptStructureReference)element);
            } else if (!toCopy.contains(element) && element instanceof IProductCmptReference) {
                result.add((IProductCmptStructureReference)element);
            }
        }

        return result.toArray(new IProductCmptStructureReference[result.size()]);
    }

    /**
     * Constructs the name of the target package
     */
    private String buildTargetPackageName(IIpsPackageFragment targetBase, IIpsObject source, int segmentsToIgnore) {
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
    private String getNewName(String oldName) {
        String newName = oldName;
        IProductCmptNamingStrategy namingStrategy = sourcePage.getNamingStrategy();
        String kindId = null;

        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList list = namingStrategy.validate(newName);
            if (!list.containsErrorMsg()) {
                kindId = namingStrategy.getKindId(newName);
            } else {
                // could't determine kind id, thus add copy of in front of the name
                // to get an unique new name
                newName = Messages.ReferenceAndPreviewPage_namePrefixCopyOf + newName;
            }
        }

        if (kindId != null && namingStrategy != null && namingStrategy.supportsVersionId()) {
            newName = namingStrategy.getProductCmptName(kindId, sourcePage.getVersion());
        }

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            // the copy product feature supports pattern replace
            String searchPattern = sourcePage.getSearchPattern();
            String replaceText = sourcePage.getReplaceText();
            if (!replaceText.equals("") && !searchPattern.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                newName = newName.replaceAll(searchPattern, replaceText);
            }
        }

        if (oldName.equals(newName)) {
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
        } else {
            checkForInvalidTargets();
        }

        if (getProductCmptStructRefToCopy().length == 0) {
            setMessage(Messages.ReferenceAndPreviewPage_msgSelectAtLeastOneProduct, WARNING);
            return false;
        }

        return errorElements.isEmpty();
    }

    private void checkForInvalidTarget(IProductCmptReference modified, boolean checked) {
        errorElements.remove(modified);
        String name = product2filenameMap.remove(modified);
        if (name != null) {
            filename2productMap.remove(name);
        }

        if (!checked) {
            return;
        }

        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(getProductCmptStructRefToCopy());
        IIpsPackageFragment base = sourcePage.getTargetPackage();
        validateTarget(modified, segmentsToIgnore, base);
    }

    /**
     * Checks for invalid targets (target names that does not allow to create a new product
     * component with this name) and refreshes the map of error messages.
     */
    private void checkForInvalidTargets() {
        errorElements.clear();
        filename2productMap.clear();
        product2filenameMap.clear();

        IProductCmptStructureReference[] toCopy = getProductCmptStructRefToCopy();
        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();

        for (int i = 0; i < toCopy.length; i++) {
            validateTarget(toCopy[i], segmentsToIgnore, base);
        }

        MessageList validationResult = new MessageList();
        new SameOperationValidator(tree, getDeepCopyWizard().getStructure()).validateSameOperation(validationResult);
        Message errorMsg = validationResult.getFirstMessage(Message.ERROR);
        if (errorMsg != null) {
            super.setMessage(Messages.ReferenceAndPreviewPage_msgCopyNotPossible, ERROR);
        }

        int noOfMessages = validationResult.getNoOfMessages();
        for (int i = 0; i < noOfMessages; i++) {
            Message currMessage = validationResult.getMessage(i);
            final IProductCmptStructureReference object = (IProductCmptStructureReference)currMessage
                    .getInvalidObjectProperties()[0].getObject();
            addMessage(object, currMessage.getText());
        }
    }

    private void validateTarget(IProductCmptStructureReference modified, int segmentsToIgnore, IIpsPackageFragment base) {
        IIpsObject correspondingIpsObject = sourcePage.getCorrespondingIpsObject(modified);

        StringBuffer message = new StringBuffer();
        String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
        IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
        if (targetPackage.exists()) {
            String newName = getNewName(correspondingIpsObject.getName());
            IpsObjectType ipsObjectType;
            if (modified instanceof IProductCmptReference) {
                ipsObjectType = IpsObjectType.PRODUCT_CMPT;
            } else if (modified instanceof IProductCmptStructureTblUsageReference) {
                ipsObjectType = IpsObjectType.TABLE_CONTENTS;
            } else {
                throw new RuntimeException("Not supported product cmpt structure reference: " + modified.getClass()); //$NON-NLS-1$
            }
            IIpsSrcFile file = targetPackage.getIpsSrcFile(ipsObjectType.getFileName(newName));
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
            IProductCmptStructureReference node = filename2productMap.get(name);
            if (node instanceof IProductCmptReference) {
                if (node != null && ((IProductCmptReference)node).getProductCmpt() != correspondingIpsObject) {
                    addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision);
                    addMessage(filename2productMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision);
                }
            } else if (node instanceof IProductCmptStructureTblUsageReference) {
                ITableContentUsage tableContentUsage = ((IProductCmptStructureTblUsageReference)node)
                        .getTableContentUsage();
                ITableContents tableContents;
                try {
                    tableContents = tableContentUsage.findTableContents(getDeepCopyWizard().getIpsProject());
                    if (node != null && (tableContents != correspondingIpsObject)) {
                        addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision);
                        addMessage(filename2productMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision);
                    }
                } catch (CoreException e) {
                    // should be displayed as validation error before
                    IpsPlugin.log(e);
                }
            } else {
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
        String oldMessage = errorElements.get(product);
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
    public Map<IProductCmptStructureReference, IIpsSrcFile> getHandles() throws CoreException {
        if (!isValid(null, true)) {
            StringBuffer message = new StringBuffer();
            Collection<String> errors = errorElements.values();
            for (Iterator<String> iter = errors.iterator(); iter.hasNext();) {
                String element = iter.next();
                message.append(element);
            }
            IpsStatus status = new IpsStatus(message.toString());
            throw new CoreException(status);
        }

        IProductCmptStructureReference[] toCopy = getProductCmptStructRefToCopy();
        Map<IProductCmptStructureReference, IIpsSrcFile> result = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();

        for (int i = 0; i < toCopy.length; i++) {
            IIpsObject correspondingIpsObject = sourcePage.getCorrespondingIpsObject(toCopy[i]);

            String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
            IIpsPackageFragment targetPackage = base.getRoot().getIpsPackageFragment(packageName);
            String newName = getNewName(correspondingIpsObject.getName());
            IIpsSrcFile file;
            if (IpsObjectType.TABLE_CONTENTS.equals(correspondingIpsObject.getIpsObjectType())) {
                file = targetPackage.getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName(newName));
            } else {
                file = targetPackage.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(newName));
            }
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
        return errorElements.get(object);
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
            } else if (in instanceof IProductCmptTypeRelationReference) {
                return ((IProductCmptTypeRelationReference)in).getRelation();
            } else if (in instanceof IProductCmptStructureTblUsageReference) {
                return ((IProductCmptStructureTblUsageReference)in).getTableContentUsage();
            }
            return null;
        }

        public Image getImage(Object element) {
            Object wrapped = getWrapped(element);
            Image image = ((IIpsObjectPartContainer)wrapped).getImage();
            if (wrapped instanceof IProductCmpt) {
                if (!tree.getChecked(element)) {
                    image = IpsPlugin.getDefault().getImage("LinkProductCmpt.gif"); //$NON-NLS-1$
                }
                if (isInError((IProductCmptStructureReference)element)) {
                    return IpsPlugin.getDefault().getImage("error_tsk.gif"); //$NON-NLS-1$
                }
                return image;
            } else if (wrapped instanceof ITableContentUsage) {
                if (!tree.getChecked(element)) {
                    image = IpsPlugin.getDefault().getImage("LinkTableContents.gif"); //$NON-NLS-1$
                }
                if (isInError((IProductCmptStructureReference)element)) {
                    return IpsPlugin.getDefault().getImage("error_tsk.gif"); //$NON-NLS-1$
                }
            }
            return image;
        }

        public String getText(Object element) {
            Object wrapped = getWrapped(element);
            if (wrapped instanceof IProductCmpt) {
                String name = ((IProductCmpt)wrapped).getName();
                if (tree.getChecked(element)) {
                    name = getNewName(name);
                }
                if (isInError((IProductCmptStructureReference)element)) {
                    name = name + Messages.ReferenceAndPreviewPage_errorLabelInsert
                            + getErrorMessage((IProductCmptStructureReference)element);
                }
                return name;
            } else if (wrapped instanceof ITableContentUsage) {
                String name = StringUtil.unqualifiedName(((ITableContentUsage)wrapped).getTableContentName());
                if (tree.getChecked(element)) {
                    name = getNewName(name);
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

        private Set<IProductCmptStructureReference> checkedNodes;

        public ContentProvider() {
            super(true);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            IProductCmptStructureReference[] children = (IProductCmptStructureReference[])super
                    .getChildren(parentElement);
            List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();
            for (int i = 0; i < children.length; i++) {
                if (isChecked(children[i])
                        || (!(children[i] instanceof IProductCmptReference) && !isUncheckedSubtree(new IProductCmptStructureReference[] { children[i] }))) {
                    result.add(children[i]);
                }
            }
            return result.toArray(new IProductCmptStructureReference[result.size()]);
        }

        private boolean isUncheckedSubtree(IProductCmptStructureReference[] children) {
            boolean unchecked = true;
            for (int i = 0; i < children.length && unchecked; i++) {
                if (children[i] instanceof IProductCmptReference) {
                    if (isChecked(children[i])) {
                        return false;
                    }
                } else if (children[i] instanceof IProductCmptTypeRelationReference) {
                    unchecked = unchecked && isUncheckedSubtree(structure.getChildProductCmptReferences(children[i]));
                }
            }
            return unchecked;
        }

        @Override
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

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProductCmptTreeStructure) {
                IProductCmptReference node = ((IProductCmptTreeStructure)inputElement).getRoot();
                if (isChecked(node)) {
                    return new Object[] { node };
                }
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
            checkedNodes = null;
        }

        public void setCheckedNodes(IProductCmptStructureReference[] checked) {
            checkedNodes = new HashSet<IProductCmptStructureReference>();
            for (int i = 0; i < checked.length; i++) {
                checkedNodes.add(checked[i]);
            }
        }

        private boolean isChecked(IProductCmptStructureReference node) {
            return checkedNodes != null && checkedNodes.contains(node);
        }
    }

    private DeepCopyWizard getDeepCopyWizard() {
        return (DeepCopyWizard)getWizard();
    }
}
