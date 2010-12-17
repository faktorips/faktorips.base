/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class DeepCopyPreview {

    private Integer segmentsToIgnoreCached = null;
    private Object[] nonLinkElementsCached = null;

    private DeepCopyWizard deepCopyWizard;
    private SourcePage sourcePage;

    // Collection of error messages indexed by product components.
    private Hashtable<IProductCmptStructureReference, String> errorElements;
    // Mapping of filenames to product references. Used for error-handling.
    private Hashtable<String, IProductCmptStructureReference> filename2productMap;
    // Mapping of product references to filenames. Used for error-handling.
    private Hashtable<IProductCmptStructureReference, String> product2filenameMap;
    private Map<Object, String> oldObject2newNameMap;

    public DeepCopyPreview(DeepCopyWizard deepCopyWizard, SourcePage sourcePage) {
        this.deepCopyWizard = deepCopyWizard;
        this.sourcePage = sourcePage;
        initCaches();
    }

    public void resetCacheAfterValueChange() {
        segmentsToIgnoreCached = null;
        nonLinkElementsCached = null;
    }

    private void initCaches() {
        filename2productMap = new Hashtable<String, IProductCmptStructureReference>();
        product2filenameMap = new Hashtable<IProductCmptStructureReference, String>();
        oldObject2newNameMap = new Hashtable<Object, String>();
        errorElements = new Hashtable<IProductCmptStructureReference, String>();
    }

    public int getSegmentsToIgnore() {
        if (segmentsToIgnoreCached != null) {
            return segmentsToIgnoreCached;
        }
        segmentsToIgnoreCached = sourcePage.getSegmentsToIgnore(getProductCmptStructRefToCopy());
        return segmentsToIgnoreCached;
    }

    public boolean newTargetHasError(IProductCmptStructureReference ref) {
        IProductCmptStructureReference[] toCopy = getProductCmptStructRefToCopy();
        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();
        IIpsPackageFragmentRoot root = sourcePage.getIIpsPackageFragmentRoot();

        Hashtable<IProductCmptStructureReference, String> errors = new Hashtable<IProductCmptStructureReference, String>(
                1);
        validateTarget(ref, segmentsToIgnore, root, base, errors);
        return errors.size() == 1;
    }

    /**
     * Checks for invalid targets (target names that does not allow to create a new product
     * component with this name) and refreshes the map of error messages.
     */
    public void checkForInvalidTargets() {
        deepCopyWizard.logTraceStart("checkForInvalidTargets"); //$NON-NLS-1$

        errorElements.clear();
        filename2productMap.clear();
        product2filenameMap.clear();
        oldObject2newNameMap.clear();

        IProductCmptStructureReference[] toCopy = getProductCmptStructRefToCopy();
        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();
        IIpsPackageFragmentRoot root = sourcePage.getIIpsPackageFragmentRoot();

        for (IProductCmptStructureReference element : toCopy) {
            validateTarget(element, segmentsToIgnore, root, base, errorElements);
        }

        MessageList validationResult = new MessageList();
        new SameOperationValidator(this, deepCopyWizard.getStructure()).validateSameOperation(validationResult);
        int noOfMessages = validationResult.size();
        for (int i = 0; i < noOfMessages; i++) {
            Message currMessage = validationResult.getMessage(i);
            final IProductCmptStructureReference object = (IProductCmptStructureReference)currMessage
                    .getInvalidObjectProperties()[0].getObject();
            addMessage(object, currMessage.getText(), errorElements);
        }

        deepCopyWizard.logTraceEnd("checkForInvalidTargets"); //$NON-NLS-1$
    }

    private void validateTarget(IProductCmptStructureReference modified,
            int segmentsToIgnore,
            IIpsPackageFragmentRoot root,
            IIpsPackageFragment base,
            Hashtable<IProductCmptStructureReference, String> errorElements) {
        if (base == null || !base.getRoot().exists()) {
            return;
        }
        IIpsObject correspondingIpsObject = sourcePage.getCorrespondingIpsObject(modified);

        StringBuffer message = new StringBuffer();
        String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
        IIpsPackageFragment targetPackage = root.getIpsPackageFragment(packageName);
        String newName = getNewName(targetPackage, correspondingIpsObject);
        oldObject2newNameMap.put(modified, newName);
        if (targetPackage.exists()) {
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
                addMessage(modified, message.toString(), errorElements);
            }
            String name = file.getEnclosingResource().getFullPath().toString();
            IProductCmptStructureReference node = filename2productMap.get(name);
            if (node instanceof IProductCmptReference) {
                if (((IProductCmptReference)node).getProductCmpt() != correspondingIpsObject) {
                    addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision, errorElements);
                    addMessage(filename2productMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision,
                            errorElements);
                }
            } else if (node instanceof IProductCmptStructureTblUsageReference) {
                ITableContentUsage tableContentUsage = ((IProductCmptStructureTblUsageReference)node)
                        .getTableContentUsage();
                ITableContents tableContents;
                try {
                    tableContents = tableContentUsage.findTableContents(deepCopyWizard.getIpsProject());
                    if ((tableContents != correspondingIpsObject)) {
                        addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision, errorElements);
                        addMessage(filename2productMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision,
                                errorElements);
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
     * Returns all product cmpt structure reference components to copy. References to product cmpts
     * and table contents are returned.
     */
    IProductCmptStructureReference[] getProductCmptStructRefToCopy() {
        deepCopyWizard.logTraceStart("getProductCmptStructRefToCopy"); //$NON-NLS-1$
        List<IProductCmptStructureReference> result = getProductCmptStructRefToCopyInternal();
        deepCopyWizard.logTraceEnd("getProductCmptStructRefToCopy"); //$NON-NLS-1$
        return result.toArray(new IProductCmptStructureReference[result.size()]);
    }

    /**
     * Returns all product cmpt structure reference components to copy. References to product cmpts
     * and table contents are returned.
     */
    private List<IProductCmptStructureReference> getProductCmptStructRefToCopyInternal() {
        List<Object> allChecked = Arrays.asList(getNonLinkElements());
        List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();

        for (Object object : allChecked) {
            IProductCmptStructureReference element = (IProductCmptStructureReference)object;
            if (element instanceof IProductCmptReference) {
                result.add(element);
            } else if (element instanceof IProductCmptStructureTblUsageReference) {
                result.add(element);
            }
        }
        return result;
    }

    private Object[] getNonLinkElements() {
        deepCopyWizard.logTraceStart("getNonLinkElements"); //$NON-NLS-1$
        if (nonLinkElementsCached != null) {
            return nonLinkElementsCached;
        }
        List<Object> result = new ArrayList<Object>();
        Set<Object> linkedElements = sourcePage.getLinkedElements();
        List<Object> allChecked = Arrays.asList(sourcePage.getTree().getCheckedElements());
        for (Object currElement : allChecked) {
            if (linkedElements.contains(currElement)) {
                continue;
            }
            result.add(currElement);
        }
        nonLinkElementsCached = result.toArray(new Object[result.size()]);
        deepCopyWizard.logTraceEnd("getNonLinkElements"); //$NON-NLS-1$
        return nonLinkElementsCached;
    }

    /**
     * Constructs the name of the target package
     */
    private String buildTargetPackageName(IIpsPackageFragment targetBase, IIpsObject source, int segmentsToIgnore) {
        if (targetBase == null || !targetBase.getRoot().exists()) {
            return ""; //$NON-NLS-1$
        }
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
    public String getNewName(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        String newName = getNewNameInternal(targetPackage, correspondingIpsObject, 0);
        return newName;
    }

    private String getNewNameInternal(IIpsPackageFragment targetPackage,
            IIpsObject correspondingIpsObject,
            int uniqueCopyOfCounter) {
        String oldName = correspondingIpsObject.getName();
        String newName = oldName;
        IProductCmptNamingStrategy namingStrategy = sourcePage.getNamingStrategy();
        String kindId = null;
        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList list = namingStrategy.validate(newName);
            if (!list.containsErrorMsg()) {
                kindId = namingStrategy.getKindId(newName);
            }
            if (kindId != null) {
                newName = namingStrategy.getProductCmptName(namingStrategy.getKindId(newName), sourcePage
                        .getVersionId());
            }
        }
        if (kindId == null && uniqueCopyOfCounter > 0) {
            // could't determine kind id, thus add copy of in front of the name
            // to get an unique new name
            newName = org.faktorips.devtools.core.util.StringUtils.computeCopyOfName(uniqueCopyOfCounter - 1, newName);
        }

        if (deepCopyWizard.getType() == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            // the copy product feature supports pattern replace
            String searchPattern = sourcePage.getSearchPattern();
            String replaceText = sourcePage.getReplaceText();
            if (!replaceText.equals("") && !searchPattern.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                newName = newName.replaceAll(searchPattern, replaceText);
            }
        }

        if (namingStrategy == null && oldName.equals(newName)) {
            // programming error, should be assert before this page will be displayed
            throw new RuntimeException(
                    "No naming strategy exists, therefore the new product components couldn't be copied with the same name in the same directory!"); //$NON-NLS-1$
        }

        // if no kind is was found check and avoid duplicate names
        // because a copyOf was added in front of the new name
        if (kindId == null && targetPackage != null) {
            IIpsSrcFile ipsSrcFile = targetPackage.getIpsSrcFile(correspondingIpsObject.getIpsObjectType().getFileName(
                    newName));
            if (ipsSrcFile.exists()) {
                return getNewNameInternal(targetPackage, correspondingIpsObject, uniqueCopyOfCounter + 1);
            }
        }

        return newName;
    }

    /**
     * Adds an error message for the given product. If a message allready exists, the new message is
     * appended.
     */
    private void addMessage(IProductCmptStructureReference product,
            String msg,
            Hashtable<IProductCmptStructureReference, String> errorElements) {
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

    public Hashtable<IProductCmptStructureReference, String> getErrorElements() {
        return errorElements;
    }

    public Hashtable<String, IProductCmptStructureReference> getFilename2productMap() {
        return filename2productMap;
    }

    public Hashtable<IProductCmptStructureReference, String> getProduct2filenameMap() {
        return product2filenameMap;
    }

    public Map<Object, String> getOldObject2newNameMap() {
        return oldObject2newNameMap;
    }

    private boolean isValid() {
        checkForInvalidTargets();

        if (getProductCmptStructRefToCopy().length == 0) {
            ((WizardPage)deepCopyWizard.getContainer().getCurrentPage()).setMessage(
                    Messages.ReferenceAndPreviewPage_msgSelectAtLeastOneProduct, IMessageProvider.WARNING);
            return false;
        }

        return getErrorElements().isEmpty();
    }

    /**
     * Returns the handles for all files to be created to do the deep copy. Note that all handles
     * point to non-existing resources or, if this condition can not be performed, a CoreException
     * is thrown.
     * 
     * @param ipsPackageFragmentRoot the {@link IIpsPackageFragmentRoot} in which the new
     *            {@link IpsSrcFile} should be created.
     * 
     * @throws CoreException if any error exists (e.g. naming collisions).
     */
    public Map<IProductCmptStructureReference, IIpsSrcFile> getHandles(IIpsPackageFragmentRoot ipsPackageFragmentRoot)
            throws CoreException {
        deepCopyWizard.logTraceStart("getHandles"); //$NON-NLS-1$

        if (!isValid()) {
            StringBuffer message = new StringBuffer();
            Collection<String> errors = getErrorElements().values();
            for (String element : errors) {
                message.append(element);
            }
            IpsStatus status = new IpsStatus(message.toString());
            throw new CoreException(status);
        }

        IProductCmptStructureReference[] toCopy = getProductCmptStructRefToCopy();
        Map<IProductCmptStructureReference, IIpsSrcFile> result = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject correspondingIpsObject = sourcePage.getCorrespondingIpsObject(element);

            String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
            IIpsPackageFragment targetPackage = ipsPackageFragmentRoot.getIpsPackageFragment(packageName);

            IIpsSrcFile file = getNewIpsSrcFile(targetPackage, correspondingIpsObject);

            result.put(element, file);
        }

        deepCopyWizard.logTraceEnd("getHandles"); //$NON-NLS-1$
        return result;
    }

    public String getPackageName(IProductCmptStructureReference toCopy) {
        return buildTargetPackageName(sourcePage.getTargetPackage(), sourcePage.getCorrespondingIpsObject(toCopy),
                getSegmentsToIgnore());
    }

    private IIpsSrcFile getNewIpsSrcFile(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        String newName = getNewName(targetPackage, correspondingIpsObject);
        return targetPackage.getIpsSrcFile(correspondingIpsObject.getIpsObjectType().getFileName(newName));
    }

    public boolean isLinked(Object element) {
        return sourcePage.getLinkedElements().contains(element);
    }

    public boolean isCopy(Object element) {
        // we don't use: getProductCmptStructRefToCopyInternal().contains(element)
        // because of performance reason
        return !isLinked(element);
    }

    public boolean isChecked(Object element) {
        return sourcePage.isChecked(element);
    }

    public IProductCmptStructureReference[] getProductsOrtTableContentsToRefer() {
        deepCopyWizard.logTraceStart("getProductsOrtTableContentsToRefer"); //$NON-NLS-1$

        Set<Object> linkedElements = sourcePage.getLinkedElements();
        IProductCmptStructureReference[] result = new IProductCmptStructureReference[linkedElements.size()];
        int idx = 0;
        for (Object object : linkedElements) {
            result[idx++] = (IProductCmptStructureReference)object;

        }

        deepCopyWizard.logTraceEnd("getProductsOrtTableContentsToRefer"); //$NON-NLS-1$
        return result;
    }

    /**
     * Returns the error text of the first error element, returns <code>null</code> if no error
     * exists.
     */
    String getFirstErrorText() {
        if (errorElements.size() == 0) {
            return null;
        }
        return errorElements.get(errorElements.keys().nextElement());
    }

    public IIpsPackageFragment getTargetPackage() {
        return sourcePage.getTargetPackage();
    }
}
