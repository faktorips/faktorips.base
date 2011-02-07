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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
        filename2referenceMap = new HashMap<String, IProductCmptStructureReference>();
        oldObject2newNameMap = new HashMap<IIpsObject, String>();
        errorElements = new HashMap<IProductCmptStructureReference, String>();
    }

    /**
     * Checks for invalid targets (target names that does not allow to create a new product
     * component with this name) and refreshes the map of error messages.
     * 
     * @param progressMonitor a progress monitor to show state of work
     */
    public void checkForInvalidTargets(IProgressMonitor progressMonitor) {
        errorElements.clear();
        filename2referenceMap.clear();
        oldObject2newNameMap.clear();
        patternMatched = false;

        Set<IProductCmptStructureReference> toCopy = presentationModel.getAllCopyElements(false);
        progressMonitor.beginTask("", toCopy.size() + 5); //$NON-NLS-1$
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
                progressMonitor.done();
                return;
            } catch (IllegalArgumentException e) {
                errorElements.put(element, e.getMessage());
                progressMonitor.done();
                return;
            }
            progressMonitor.worked(1);
        }

        String searchInput = presentationModel.getSearchInput();
        String replaceInput = presentationModel.getReplaceInput();
        if (!searchInput.isEmpty() && !replaceInput.isEmpty() && !searchInput.equals(replaceInput) && !patternMatched) {
            addMessage(presentationModel.getStructure().getRoot(),
                    NLS.bind(Messages.SourcePage_msgPatternNotFound, searchInput));
        }

        MessageList validationResult = new MessageList();
        int noOfMessages = validationResult.size();
        SubProgressMonitor subProgress = new SubProgressMonitor(progressMonitor, 5);
        subProgress.beginTask("", noOfMessages); //$NON-NLS-1$
        for (int i = 0; i < noOfMessages; i++) {
            Message currMessage = validationResult.getMessage(i);
            final IProductCmptStructureReference object = (IProductCmptStructureReference)currMessage
                    .getInvalidObjectProperties()[0].getObject();
            addMessage(object, currMessage.getText());
            subProgress.worked(1);
        }
        subProgress.done();
        progressMonitor.done();
    }

    private void validateTarget(IProductCmptStructureReference modified,
            int segmentsToIgnore,
            IIpsPackageFragmentRoot root,
            IIpsPackageFragment base) {
        if (base == null || !base.getRoot().exists()) {
            return;
        }
        IIpsObject correspondingIpsObject = modified.getWrappedIpsObject();

        StringBuffer message = new StringBuffer();
        String packageName = buildTargetPackageName(base, correspondingIpsObject, segmentsToIgnore);
        IIpsPackageFragment targetPackage = root.getIpsPackageFragment(packageName);

        String newName = getNewName(targetPackage, correspondingIpsObject);
        if (isNameChanged(correspondingIpsObject, newName)) {
            // this only indicates, that there is at least one pattern matched
            patternMatched = true;
        }
        // we put all new names to this map to preview also names that have not changed
        oldObject2newNameMap.put(correspondingIpsObject, newName);
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
            IProductCmptStructureReference node = filename2referenceMap.get(name);
            if (node instanceof IProductCmptReference) {
                if (((IProductCmptReference)node).getProductCmpt() != correspondingIpsObject) {
                    addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision);
                    addMessage(filename2referenceMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision);
                }
            } else if (node instanceof IProductCmptStructureTblUsageReference) {
                ITableContentUsage tableContentUsage = ((IProductCmptStructureTblUsageReference)node)
                        .getTableContentUsage();
                ITableContents tableContents;
                try {
                    tableContents = tableContentUsage.findTableContents(presentationModel.getIpsProject());
                    if ((tableContents != correspondingIpsObject)) {
                        addMessage(modified, Messages.ReferenceAndPreviewPage_msgNameCollision);
                        addMessage(filename2referenceMap.get(name), Messages.ReferenceAndPreviewPage_msgNameCollision);
                    }
                } catch (CoreException e) {
                    // should be displayed as validation error before
                    IpsPlugin.log(e);
                }
            } else {
                filename2referenceMap.put(name, modified);
            }
        }
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

    public int getSegmentsToIgnore() {
        return getSegmentsToIgnore(presentationModel.getAllCopyElements(false));
    }

    /**
     * Calculate the number of <code>IPath</code>-segements which are equal for all product
     * component structure refences to copy.
     * 
     * @return 0 if no elements are contained in toCopy, number of all segments, if only one product
     *         component is contained in toCopy and the calculated value as described above for all
     *         other cases.
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
            return ""; //$NON-NLS-1$
        }
        IPath subPath = source.getIpsPackageFragment().getRelativePath().removeFirstSegments(segmentsToIgnore);
        String toAppend = subPath.toString().replace('/', '.');

        String base = targetBase.getName();

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
        String alreadyMappedName = oldObject2newNameMap.get(correspondingIpsObject);
        if (alreadyMappedName != null) {
            return alreadyMappedName;
        }
        String newName = getNewNameInternal(targetPackage, correspondingIpsObject, 0);
        return newName;
    }

    private String getNewNameInternal(IIpsPackageFragment targetPackage,
            IIpsObject correspondingIpsObject,
            int uniqueCopyOfCounter) {
        String oldName = correspondingIpsObject.getName();
        String newName = oldName;
        IProductCmptNamingStrategy namingStrategy = presentationModel.getIpsProject().getProductCmptNamingStrategy();
        String kindId = null;
        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList list = namingStrategy.validate(newName);
            if (!list.containsErrorMsg()) {
                kindId = namingStrategy.getKindId(newName);
            }
            if (kindId != null) {
                kindId = searchReplace(kindId);
                newName = namingStrategy.getProductCmptName(kindId, presentationModel.getVersionId());
            }
        }
        if (kindId == null && uniqueCopyOfCounter > 0) {
            // could't determine kind id, thus add copy of in front of the name
            // to get an unique new name
            newName = org.faktorips.devtools.core.util.StringUtils.computeCopyOfName(uniqueCopyOfCounter - 1, newName);
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

    private String searchReplace(String newName) {
        // the copy product feature supports pattern replace
        String searchPattern = presentationModel.getSearchInput();
        String replaceText = presentationModel.getReplaceInput();
        if (!"".equals(replaceText) && !"".equals(searchPattern)) { //$NON-NLS-1$ //$NON-NLS-2$

            Pattern pattern = presentationModel.getSearchPattern();
            try {
                Matcher matcher = pattern.matcher(newName);
                newName = matcher.replaceAll(replaceText);
            } catch (Exception e) {
                throw new IllegalArgumentException(NLS.bind(Messages.SourcePage_msgInvalidPattern, replaceText)
                        + e.getLocalizedMessage());
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
        checkForInvalidTargets(progressMonitor);
        return getErrorElements().isEmpty();
    }

    /**
     * Returns the handles for all files to be created to do the deep copy. Note that all handles
     * point to non-existing resources
     * 
     * @throws CoreException if any error exists (e.g. naming collisions).
     */
    public Map<IProductCmptStructureReference, IIpsSrcFile> getHandles(IProgressMonitor progressMonitor)
            throws CoreException {
        if (!isValid(progressMonitor)) {
            StringBuffer message = new StringBuffer();
            Collection<String> errors = getErrorElements().values();
            for (String element : errors) {
                message.append(element);
            }
            IpsStatus status = new IpsStatus(message.toString());
            throw new CoreException(status);
        }

        Set<IProductCmptStructureReference> toCopy = presentationModel.getAllCopyElements(true);
        Map<IProductCmptStructureReference, IIpsSrcFile> result = new HashMap<IProductCmptStructureReference, IIpsSrcFile>();

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
