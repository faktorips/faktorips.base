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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class DeepCopyPreview {

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

    private void initCaches() {
        filename2productMap = new Hashtable<String, IProductCmptStructureReference>();
        product2filenameMap = new Hashtable<IProductCmptStructureReference, String>();
        oldObject2newNameMap = new Hashtable<Object, String>();
        errorElements = new Hashtable<IProductCmptStructureReference, String>();
    }

    public void checkForInvalidTarget(IProductCmptReference modified, boolean checked) {
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
    public void checkForInvalidTargets() {
        errorElements.clear();
        filename2productMap.clear();
        product2filenameMap.clear();
        oldObject2newNameMap.clear();

        IProductCmptStructureReference[] toCopy = getProductCmptStructRefToCopy();
        int segmentsToIgnore = sourcePage.getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = sourcePage.getTargetPackage();

        for (int i = 0; i < toCopy.length; i++) {
            validateTarget(toCopy[i], segmentsToIgnore, base);
        }

        MessageList validationResult = new MessageList();
        new SameOperationValidator(this, deepCopyWizard.getStructure()).validateSameOperation(validationResult);
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
                    tableContents = tableContentUsage.findTableContents(deepCopyWizard.getIpsProject());
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
     * Returns all product cmpt structure reference components to copy. References to product cmpts
     * and table contents are returned.
     */
    IProductCmptStructureReference[] getProductCmptStructRefToCopy() {
        List<IProductCmptStructureReference> result = getProductCmptStructRefToCopyInternal();
        return result.toArray(new IProductCmptStructureReference[result.size()]);
    }

    /**
     * Returns all product cmpt structure reference components to copy. References to product cmpts
     * and table contents are returned.
     */
    private List<IProductCmptStructureReference> getProductCmptStructRefToCopyInternal() {
        List<Object> allChecked = Arrays.asList(getNonLinkElements());
        List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();

        for (Iterator<Object> iter = allChecked.iterator(); iter.hasNext();) {
            IProductCmptStructureReference element = (IProductCmptStructureReference)iter.next();
            if (element instanceof IProductCmptReference) {
                result.add(element);
            } else if (element instanceof IProductCmptStructureTblUsageReference) {
                result.add(element);
            }
        }
        return result;
    }

    private Object[] getNonLinkElements() {
        List<Object> result = new ArrayList<Object>();
        Set<Object> linkedElements = sourcePage.getLinkedElements();
        List<Object> allChecked = Arrays.asList(sourcePage.getTree().getCheckedElements());
        for (Iterator<Object> iter = allChecked.iterator(); iter.hasNext();) {
            Object currElement = iter.next();
            if (linkedElements.contains(currElement)) {
                continue;
            }
            result.add(currElement);
        }
        return result.toArray(new Object[result.size()]);
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
    private String getNewName(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        return getNewName(targetPackage, correspondingIpsObject, 0);
    }

    private String getNewName(IIpsPackageFragment targetPackage,
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
                newName = namingStrategy.getProductCmptName(namingStrategy.getKindId(newName), sourcePage.getVersion());
            } else {
                // could't determine kind id, thus add copy of in front of the name
                // to get an unique new name
                if (targetPackage != null) {
                    newName = org.faktorips.devtools.core.util.StringUtils.computeCopyOfName(uniqueCopyOfCounter,
                            newName);
                }
            }
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
                return getNewName(targetPackage, correspondingIpsObject, ++uniqueCopyOfCounter);
            }
        }

        return newName;
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
     * @throws CoreException if any error exists (e.g. naming collisions).
     */
    public Map<IProductCmptStructureReference, IIpsSrcFile> getHandles() throws CoreException {
        if (!isValid()) {
            StringBuffer message = new StringBuffer();
            Collection<String> errors = getErrorElements().values();
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

            IIpsSrcFile file = getNewIpsSrcFile(targetPackage, correspondingIpsObject);

            result.put(toCopy[i], file);
        }
        return result;
    }

    public String getPackageName(IProductCmptStructureReference toCopy) {
        return buildTargetPackageName(sourcePage.getTargetPackage(), sourcePage.getCorrespondingIpsObject(toCopy),
                sourcePage.getSegmentsToIgnore(getProductCmptStructRefToCopy()));
    }

    private IIpsSrcFile getNewIpsSrcFile(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        String newName = getNewName(targetPackage, correspondingIpsObject);
        return targetPackage.getIpsSrcFile(correspondingIpsObject.getIpsObjectType().getFileName(newName));
    }

    public boolean isLinked(Object element) {
        return sourcePage.getLinkedElements().contains(element);
    }

    public boolean isCopy(Object element) {
        return getProductCmptStructRefToCopyInternal().contains(element);
    }

    public IProductCmptStructureReference[] getProductsOrtTableContentsToRefer() {
        Set<Object> linkedElements = sourcePage.getLinkedElements();
        IProductCmptStructureReference[] result = new IProductCmptStructureReference[linkedElements.size()];
        int idx = 0;
        for (Iterator<Object> iterator = linkedElements.iterator(); iterator.hasNext();) {
            result[idx++] = (IProductCmptStructureReference)iterator.next();

        }
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
