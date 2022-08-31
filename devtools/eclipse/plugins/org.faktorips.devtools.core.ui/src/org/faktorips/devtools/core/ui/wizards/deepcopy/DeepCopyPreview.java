/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

public class DeepCopyPreview {

    // Collection of error messages indexed by product components.
    private Map<IProductCmptStructureReference, String> errorElements;
    // Mapping of filenames to product references. Used for error-handling.
    private Map<String, IProductCmptStructureReference> filename2referenceMap;
    private Map<IIpsObject, String> oldObject2newNameMap;

    private final DeepCopyPresentationModel presentationModel;

    private boolean patternMatched;

    public DeepCopyPreview(DeepCopyPresentationModel presentationModel) {
        this.presentationModel = presentationModel;
        initCaches();
    }

    private void initCaches() {
        filename2referenceMap = new HashMap<>();
        oldObject2newNameMap = new HashMap<>();
        errorElements = new HashMap<>();
    }

    /**
     * Checks for invalid targets (target names that does not allow to create a new product
     * component with this name) and refreshes the map of error messages.
     * 
     * @param progressMonitor a progress monitor to show state of work
     */
    public void createTargetNodes(IProgressMonitor progressMonitor) {
        errorElements.clear();
        filename2referenceMap.clear();
        oldObject2newNameMap.clear();
        patternMatched = false;

        Set<IProductCmptStructureReference> toCopy = presentationModel.getAllCopyElements(false);
        SubMonitor subProgressMonitor = SubMonitor.convert(progressMonitor, toCopy.size() + 5);
        int segmentsToIgnore = getSegmentsToIgnore(toCopy);
        IIpsPackageFragment base = presentationModel.getTargetPackage();
        IIpsPackageFragmentRoot root = presentationModel.getTargetPackageRoot();

        for (IProductCmptStructureReference element : toCopy) {
            try {
                validateTarget(element, segmentsToIgnore, root, base);
            } catch (PatternSyntaxException e) {
                errorElements.put(
                        element,
                        NLS.bind(Messages.SourcePage_msgInvalidPattern, getPresentationModel().getSearchInput())
                                + e.getLocalizedMessage());
                return;
            } catch (IllegalArgumentException e) {
                errorElements.put(element, e.getMessage());
                return;
            }
            subProgressMonitor.worked(1);
        }

        String searchInput = presentationModel.getSearchInput();
        String replaceInput = presentationModel.getReplaceInput();
        if (!searchInput.isEmpty() && !replaceInput.isEmpty() && !searchInput.equals(replaceInput) && !patternMatched) {
            addMessage(presentationModel.getStructure().getRoot(),
                    NLS.bind(Messages.SourcePage_msgPatternNotFound, searchInput));
        }

        MessageList validationResult = new MessageList();
        int noOfMessages = validationResult.size();
        SubMonitor subProgress = subProgressMonitor.split(5);
        subProgress.beginTask("", noOfMessages); //$NON-NLS-1$
        for (int i = 0; i < noOfMessages; i++) {
            Message currMessage = validationResult.getMessage(i);
            final IProductCmptStructureReference object = (IProductCmptStructureReference)currMessage
                    .getInvalidObjectProperties().get(0).getObject();
            addMessage(object, currMessage.getText());
            subProgress.worked(1);
        }
    }

    private void validateTarget(IProductCmptStructureReference modified,
            int segmentsToIgnore,
            IIpsPackageFragmentRoot root,
            IIpsPackageFragment base) {
        if (isNullOrNotExists(root, base)) {
            return;
        }
        IIpsObject correspondingIpsObject = modified.getWrappedIpsObject();

        String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
        IIpsPackageFragment targetPackage = root.getIpsPackageFragment(packageName);

        String newName = getNewName(targetPackage, correspondingIpsObject);
        if (isNameChanged(correspondingIpsObject, newName)) {
            // this only indicates, that there is at least one pattern matched
            patternMatched = true;
        }
        // we put all new names to this map to preview also names that have not changed
        oldObject2newNameMap.put(correspondingIpsObject, newName);
        IpsObjectType ipsObjectType = getIpsObjectType(modified);
        validateAlreadyExistingFile(packageName, newName, ipsObjectType.getFileExtension(), modified);
        if (targetPackage.exists()) {
            IIpsSrcFile newIpsSrcFile = targetPackage.getIpsSrcFile(ipsObjectType.getFileName(newName));
            String newFileName = newIpsSrcFile.getEnclosingResource().getWorkspaceRelativePath().toString();
            IProductCmptStructureReference node = filename2referenceMap.get(newFileName);
            if (node != null) {
                IIpsObject wrappedIpsObject = node.getWrappedIpsObject();
                if (wrappedIpsObject != correspondingIpsObject) {
                    addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision);
                    addMessage(filename2referenceMap.get(newFileName),
                            Messages.ReferenceAndPreviewPage_msgNameCollision);
                }
            } else {
                filename2referenceMap.put(newFileName, modified);
            }
        }
    }

    private boolean isNullOrNotExists(IIpsPackageFragmentRoot root, IIpsPackageFragment base) {
        return root == null || base == null || !base.getRoot().exists();
    }

    private IpsObjectType getIpsObjectType(IProductCmptStructureReference modified) {
        IIpsObject wrappedIpsObject = modified.getWrappedIpsObject();
        return wrappedIpsObject.getIpsObjectType();
    }

    /* private */void validateAlreadyExistingFile(String packageName,
            String newName,
            String fileExtension,
            IProductCmptStructureReference modified) {
        if (isExistingIpsSrcFile(packageName, newName, fileExtension)) {
            StringBuilder message = new StringBuilder();
            message.append(Messages.ReferenceAndPreviewPage_msgCanNotCreateFile).append(packageName);
            if (!packageName.isEmpty()) {
                message.append(QualifiedNameType.FILE_EXTENSION_SEPERATOR);
            }
            message.append(newName).append(Messages.ReferenceAndPreviewPage_msgFileAllreadyExists);
            addMessage(modified, message.toString());
        }

    }

    private boolean isExistingIpsSrcFile(String packageName, String ipsSrcFileName, String fileExtension) {
        String qualifiedName = packageName + IIpsPackageFragment.SEPARATOR + ipsSrcFileName
                + QualifiedNameType.FILE_EXTENSION_SEPERATOR + fileExtension;
        QualifiedNameType qualifedNameType = QualifiedNameType.newQualifedNameType(qualifiedName);
        IIpsProject ipsProject = presentationModel.getIpsProject();
        IIpsSrcFile fileInProjects = ipsProject.findIpsSrcFile(qualifedNameType);
        return fileInProjects != null && fileInProjects.exists();
    }

    private boolean isNameChanged(IIpsObject correspondingIpsObject, String newName) {
        String oldName = correspondingIpsObject.getName();
        if (correspondingIpsObject instanceof IProductCmpt) {
            IProductCmptNamingStrategy namingStrategy = presentationModel.getIpsProject()
                    .getProductCmptNamingStrategy();
            String oldKindId = namingStrategy.getKindId(oldName);
            String newKindId = namingStrategy.getKindId(newName);
            return !oldKindId.equals(newKindId);
        } else {
            return !oldName.equals(newName);
        }
    }

    /**
     * Calculate the number of <code>IPath</code>-segements which are equal for all product
     * component structure refences to copy.
     * 
     * @return 0 if no elements are contained in toCopy, number of all segments, if only one product
     *             component is contained in toCopy and the calculated value as described above for
     *             all other cases.
     */
    int getSegmentsToIgnore(Set<IProductCmptStructureReference> toCopy) {
        if (toCopy.size() == 0) {
            return 0;
        }

        IPath refPath = null;
        int ignore = Integer.MAX_VALUE;
        for (IProductCmptStructureReference reference : toCopy) {
            IIpsObject ipsObject = reference.getWrappedIpsObject();
            if (refPath == null) {
                refPath = ipsObject.getIpsPackageFragment().getRelativePath();
            }
            int tmpIgnore;
            if (ipsObject == null) {
                continue;
            }
            IPath nextPath = ipsObject.getIpsPackageFragment().getRelativePath();
            tmpIgnore = nextPath.matchingFirstSegments(refPath);
            ignore = Math.min(ignore, tmpIgnore);
        }

        return ignore;
    }

    /**
     * Constructs the name of the target package
     */
    String buildTargetPackageName(IIpsPackageFragment targetBase, IIpsObject source, int segmentsToIgnore) {
        if (targetBase == null || !targetBase.getRoot().exists()) {
            return IpsStringUtils.EMPTY;
        }
        IPath subPath = source.getIpsPackageFragment().getRelativePath().removeFirstSegments(segmentsToIgnore);
        String toAppend = subPath.toString().replace('/', IIpsPackageFragment.SEPARATOR);

        String base = targetBase.getName();

        if (!base.isEmpty() && !toAppend.isEmpty()) {
            base = base + QualifiedNameType.FILE_EXTENSION_SEPERATOR;
        }

        return base + toAppend;
    }

    /**
     * Constructs the new name. If at least one of search pattern and replace text is empty, the new
     * name is the old name.
     */
    public String getNewName(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        String alreadyMappedName = oldObject2newNameMap.get(correspondingIpsObject);
        if (alreadyMappedName != null) {
            return alreadyMappedName;
        }
        return getNewNameInternal(targetPackage, correspondingIpsObject);
    }

    private String getNewNameInternal(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        IpsObjectType ipsObjectType = correspondingIpsObject.getIpsObjectType();
        String oldName = correspondingIpsObject.getName();
        String newName = getNameAfterSearchReplace(oldName);
        if (IpsObjectType.TABLE_CONTENTS.equals(ipsObjectType) && targetPackage != null) {
            newName = getUniqueCopyOfName(targetPackage, ipsObjectType, newName, 0);
        }
        return newName;
    }

    private String getNameAfterSearchReplace(String newName) {
        IProductCmptNamingStrategy namingStrategy = presentationModel.getIpsProject().getProductCmptNamingStrategy();
        String kindId = getKindId(newName, namingStrategy);
        boolean validKindId = kindId != null;
        String nameForReplace;
        if (validKindId) {
            nameForReplace = kindId;
        } else {
            nameForReplace = newName;
        }
        String replacedString = searchReplace(nameForReplace);
        if (validKindId) {
            return namingStrategy.getProductCmptName(replacedString, presentationModel.getVersionId());
        } else {
            return replacedString;
        }
    }

    private String getKindId(String newName, IProductCmptNamingStrategy namingStrategy) {
        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList list = namingStrategy.validate(newName);
            if (!list.containsErrorMsg()) {
                return namingStrategy.getKindId(newName);
            }
        }
        return null;
    }

    private String getUniqueCopyOfName(IIpsPackageFragment targetPackage,
            IpsObjectType ipsObjectType,
            String newName,
            int uniqueCopyOfCounter) {
        IIpsSrcFile ipsSrcFile = targetPackage.getIpsSrcFile(ipsObjectType.getFileName(newName));
        if (ipsSrcFile.exists()) {
            String copyOfName = org.faktorips.devtools.model.internal.util.StringUtils.computeCopyOfName(
                    uniqueCopyOfCounter,
                    newName);
            return getUniqueCopyOfName(targetPackage, ipsObjectType, copyOfName, uniqueCopyOfCounter + 1);
        } else {
            return newName;
        }
    }

    private String searchReplace(String newName) {
        // the copy product feature supports pattern replace
        String searchPattern = presentationModel.getSearchInput();
        String replaceText = presentationModel.getReplaceInput();
        String newNameReplace = newName;
        if (!IpsStringUtils.EMPTY.equals(replaceText) && !IpsStringUtils.EMPTY.equals(searchPattern)) {

            Pattern pattern = presentationModel.getSearchPattern();
            try {
                Matcher matcher = pattern.matcher(newName);
                newNameReplace = matcher.replaceAll(replaceText);
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                throw new IllegalArgumentException(NLS.bind(Messages.SourcePage_msgInvalidPattern, replaceText)
                        + e.getLocalizedMessage());
            }
        }
        return newNameReplace;
    }

    /**
     * Adds an error message for the given product. If a message allready exists, the new message is
     * appended.
     */
    private void addMessage(IProductCmptStructureReference product, String msg) {
        if (msg == null || msg.length() == 0) {
            return;
        }

        StringBuilder newMessage = new StringBuilder();
        String oldMessage = errorElements.get(product);
        if (oldMessage != null) {
            newMessage.append(oldMessage);
        }
        newMessage.append(msg);

        errorElements.put(product, newMessage.toString());
    }

    public Map<IProductCmptStructureReference, String> getErrorElements() {
        return errorElements;
    }

    public String getNewName(IIpsObject ipsObject) {
        return oldObject2newNameMap.get(ipsObject);
    }

    public Collection<String> getNewNames() {
        return oldObject2newNameMap.values();
    }

    private boolean isValid(IProgressMonitor progressMonitor) {
        createTargetNodes(progressMonitor);
        return getErrorElements().isEmpty();
    }

    /**
     * Returns the handles for all files to be created to do the deep copy. Note that all handles
     * point to non-existing resources
     * 
     * @throws IpsException if any error exists (e.g. naming collisions).
     */
    public Map<IProductCmptStructureReference, IIpsSrcFile> getHandles(IProgressMonitor progressMonitor,
            Set<IProductCmptStructureReference> toCopy) {
        if (!isValid(progressMonitor)) {
            StringBuilder message = new StringBuilder();
            Collection<String> errors = getErrorElements().values();
            for (String element : errors) {
                message.append(element);
            }
            IpsStatus status = new IpsStatus(message.toString());
            throw new IpsException(status);
        }

        Map<IProductCmptStructureReference, IIpsSrcFile> result = new HashMap<>();

        int segmentsToIgnore = getSegmentsToIgnore(toCopy);
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = presentationModel.getTargetPackageRoot();
        IIpsPackageFragment base = presentationModel.getTargetPackage();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject correspondingIpsObject = element.getWrappedIpsObject();

            String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
            IIpsPackageFragment targetPackage = ipsPackageFragmentRoot.getIpsPackageFragment(packageName);

            IIpsSrcFile file = getNewIpsSrcFile(targetPackage, correspondingIpsObject);

            result.put(element, file);
        }

        return result;
    }

    private IIpsSrcFile getNewIpsSrcFile(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        String newName = getNewName(targetPackage, correspondingIpsObject);
        return targetPackage.getIpsSrcFile(correspondingIpsObject.getIpsObjectType().getFileName(newName));
    }

    /**
     * Returns the error text either of the root element or of the 'first' error found. As far the
     * error messages are stored in a map there is not really any order.
     */
    String getFirstErrorText() {
        if (errorElements.size() == 0) {
            return null;
        }
        String rootMessage = errorElements.get(presentationModel.getStructure().getRoot());
        if (rootMessage != null) {
            return rootMessage;
        }
        return errorElements.get(errorElements.keySet().iterator().next());
    }

    public DeepCopyPresentationModel getPresentationModel() {
        return presentationModel;
    }
}
