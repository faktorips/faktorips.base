/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.tablecontents.ITableContents;

/**
 * The deep copy operation creates the new product components and creates the links to existing
 * generations. It handles all operations needed to perform a product copy or the creation of a new
 * product version.
 */
public class DeepCopyOperation implements ICoreRunnable {

    private final Set<IProductCmptStructureReference> copyElements;
    private final Set<IProductCmptStructureReference> linkElements;
    private final Map<IProductCmptStructureReference, IIpsSrcFile> handleMap;
    private boolean createEmptyTableContents = false;
    private boolean copyExistingGenerations = false;
    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;
    private IIpsPackageFragment sourceIpsPackageFragment;
    private IIpsPackageFragment targetIpsPackageFragment;
    private final IProductCmptReference structureRoot;
    private final GregorianCalendar oldValidFrom;
    private final GregorianCalendar newValidFrom;

    /**
     * Creates a new operation to copy the given product components.
     * 
     * @param copyElements All product components and table contents that should be copied.
     * @param linkElements All product components and table contents which should be referred from
     *            the copied ones.
     * @param handleMap All <code>IIpsSrcFiles</code> (which are all handles to non-existing
     *            resources!). Keys are the nodes given in <code>toCopy</code>.
     */
    public DeepCopyOperation(IProductCmptReference structureRoot, Set<IProductCmptStructureReference> copyElements,
            Set<IProductCmptStructureReference> linkElements,
            Map<IProductCmptStructureReference, IIpsSrcFile> handleMap, GregorianCalendar oldValidFrom,
            GregorianCalendar newValidFrom) {
        this.structureRoot = structureRoot;
        this.copyElements = copyElements;
        this.linkElements = linkElements;
        this.handleMap = handleMap;
        this.oldValidFrom = oldValidFrom;
        this.newValidFrom = newValidFrom;
    }

    public void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot ipsPackageFragmentRoot) {
        this.ipsPackageFragmentRoot = ipsPackageFragmentRoot;
    }

    public void setSourceIpsPackageFragment(IIpsPackageFragment ipsPackageFragment) {
        sourceIpsPackageFragment = ipsPackageFragment;
    }

    public void setTargetIpsPackageFragment(IIpsPackageFragment ipsPackageFragment) {
        targetIpsPackageFragment = ipsPackageFragment;
    }

    /**
     * If <code>true</code> table contents will be created as empty files, otherwise the table
     * contents will be copied.
     */
    public void setCreateEmptyTableContents(boolean createEmptyTableContents) {
        this.createEmptyTableContents = createEmptyTableContents;
    }

    public void setCopyExistingGenerations(boolean copyExistingGenerations) {
        this.copyExistingGenerations = copyExistingGenerations;
    }

    @Override
    public void run(IProgressMonitor progressMonitor) {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask(Messages.DeepCopyOperation_taskTitle, 2 + copyElements.size() * 2 + linkElements.size());

        monitor.worked(1);

        // stores all objects which refers old targets instead of changing to the new one
        final Set<Object> objectsToRefer = collectObjectsToRefer();

        // maps used to fix the targets (table usages or links) on the new productCmpt
        final Map<TblContentUsageData, String> tblContentData2newTableContentQName = new HashMap<>();
        final Map<LinkData, String> linkData2newProductCmptQName = new HashMap<>();

        Hashtable<IProductCmpt, IProductCmpt> productNew2ProductOld = new Hashtable<>();
        List<IIpsObject> newIpsObjects = new ArrayList<>();
        for (IProductCmptStructureReference element : copyElements) {
            IIpsObject newIpsObject = createNewIpsObjectIfNecessary(element, productNew2ProductOld, oldValidFrom,
                    newValidFrom, monitor);
            newIpsObjects.add(newIpsObject);

            // stores the link or tableUsage which indicates the creation of the new object
            // will be used later to fix the new link target or new table contents if necessary
            if (element instanceof IProductCmptReference productCmptReference) {
                storeLinkToNewNewProductCmpt(productCmptReference, newIpsObject.getQualifiedName(),
                        linkData2newProductCmptQName);
            } else if (element instanceof IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference) {
                storeTableUsageToNewTableContents(productCmptStructureTblUsageReference,
                        newIpsObject.getQualifiedName(), tblContentData2newTableContentQName);
            }

            monitor.worked(1);
        }

        List<IDeepCopyOperationFixup> additionalFixups = getAdditionalFixups();
        // fix links, on of the following options:
        // a) change target to new (copied) productCmpt's: if the target should also be copied
        // ->
        // checked on the 1st wizard page and checked on the 2nd page
        // b) leave old target: if the target shouldn't be changed -> check on 1st wizard page
        // and
        // unchecked on the 2nd page
        // c) delete link: if target are not copied -> not check on 1st wizard page
        // fix tableContentUsages, on of the following options:
        // a) change table usage to the new (copied) tableContents -> see a) above
        // b) leave the table usage to the old tableContents -> see b) above
        // C) clear table usage (leave empty) -> see c) above
        for (Entry<IProductCmpt, IProductCmpt> entry : productNew2ProductOld.entrySet()) {
            final IProductCmpt productCmptNew = entry.getKey();
            final IProductCmpt productCmptTemplate = entry.getValue();

            fixLinks(productCmptNew, productCmptTemplate, linkData2newProductCmptQName,
                    tblContentData2newTableContentQName, objectsToRefer);
            for (final IDeepCopyOperationFixup fixup : additionalFixups) {
                ISafeRunnable runnable = new ISafeRunnable() {
                    @Override
                    public void handleException(Throwable exception) {
                        IpsLog.log(exception);
                    }

                    @Override
                    public void run() throws Exception {
                        fixup.fix(productCmptNew, productCmptTemplate);
                    }
                };
                SafeRunner.run(runnable);
            }
        }

        // save all ipsSource files
        for (IIpsObject iIpsObject : newIpsObjects) {
            IIpsSrcFile ipsSrcFile = (iIpsObject).getIpsSrcFile();
            ipsSrcFile.save(monitor);
            monitor.worked(1);
        }
        if (newIpsObjects.size() == 0) {
            throw new RuntimeException("No copied root found!"); //$NON-NLS-1$
        }

        // copy all existing sort order files (if any)
        copySortOrder(productNew2ProductOld, monitor);

        monitor.done();
    }

    /* private */ void copySortOrder(Map<IProductCmpt, IProductCmpt> productNew2ProductOld, IProgressMonitor monitor) {
        Map<IIpsSrcFile, IIpsSrcFile> old2NewSrcFile = new HashMap<>(
                productNew2ProductOld.size());
        for (Entry<IProductCmpt, IProductCmpt> entry : productNew2ProductOld.entrySet()) {
            old2NewSrcFile.put(entry.getValue().getIpsSrcFile(), entry.getKey().getIpsSrcFile());
        }
        try {
            copySortOrder(sourceIpsPackageFragment, targetIpsPackageFragment, old2NewSrcFile, monitor);
        } catch (IpsException e) {
            throw new IpsException("Exception occured during sort order copying.", e); //$NON-NLS-1$
        }
    }

    private void copySortOrder(IIpsPackageFragment sourceParent,
            IIpsPackageFragment targetParent,
            Map<IIpsSrcFile, IIpsSrcFile> old2NewSrcFile,
            IProgressMonitor monitor) {
        Comparator<IIpsElement> sourceComparator = sourceParent.getChildOrderComparator();
        if (sourceComparator instanceof DefinedOrderComparator) {
            Comparator<IIpsElement> targetComparator = targetParent.getChildOrderComparator();
            if (!(targetComparator instanceof DefinedOrderComparator)) {
                copySortOrder(((DefinedOrderComparator)sourceComparator).getElements(), old2NewSrcFile, targetParent);
            }
        }
        for (IIpsPackageFragment fragment : sourceParent.getChildIpsPackageFragments()) {
            IIpsPackageFragment destination = targetParent.getSubPackage(fragment.getLastSegmentName());
            if (destination.exists()) {
                copySortOrder(fragment, destination, old2NewSrcFile, monitor);
            }
        }
    }

    protected void copySortOrder(IIpsElement[] elements,
            Map<IIpsSrcFile, IIpsSrcFile> old2NewSrcFile,
            IIpsPackageFragment targetParent) {
        List<IIpsElement> copiedElements = new ArrayList<>(elements.length);
        for (IIpsElement element : elements) {
            if (element instanceof IIpsSrcFile) {
                IIpsElement newElement = old2NewSrcFile.get(element);
                if (newElement != null) {
                    copiedElements.add(newElement);
                }
            } else if (element instanceof IIpsPackageFragment) {
                IIpsPackageFragment subPackage = targetParent
                        .getSubPackage(((IIpsPackageFragment)element).getLastSegmentName());
                if (subPackage.exists()) {
                    copiedElements.add(subPackage);
                }
            }
        }
        if (!copiedElements.isEmpty()) {
            ((IpsPackageFragment)targetParent).setChildOrderComparator(
                    new DefinedOrderComparator(copiedElements.toArray(new IIpsElement[copiedElements.size()])));
        }
    }

    /**
     * Returns the {@link IDeepCopyOperationFixup DeepCopyOperationFixups} defined in the extension
     * point {@code org.faktorips.devtools.model.deepCopyOperation}.
     * 
     * @return the {@link IDeepCopyOperationFixup DeepCopyOperationFixups}
     */
    public List<IDeepCopyOperationFixup> getAdditionalFixups() {
        return IIpsModelExtensions.get().getDeepCopyOperationFixups();
    }

    private IIpsObject createNewIpsObjectIfNecessary(
            final IProductCmptStructureReference toCopyProductCmptStructureReference,
            Hashtable<IProductCmpt, IProductCmpt> productNew2ProductOld,
            GregorianCalendar oldValidFrom,
            GregorianCalendar newValidFrom,
            IProgressMonitor monitor) {

        IIpsObject templateObject = toCopyProductCmptStructureReference.getWrappedIpsObject();
        IIpsSrcFile file = handleMap.get(toCopyProductCmptStructureReference);

        // if the file already exists, we can do nothing because the file was created already
        // caused by another reference to the same product component.
        if (!file.exists()) {
            IIpsPackageFragment targetPackage = createTargetPackage(file, monitor);
            String newName = file.getName().substring(0, file.getName().lastIndexOf('.'));

            boolean createEmptyFile = false;

            if (createEmptyTableContents && IpsObjectType.TABLE_CONTENTS.equals(templateObject.getIpsObjectType())) {
                createEmptyFile = true;
            }

            if (!createEmptyFile) {
                // try to create the file as copy
                try {
                    file = templateObject.createCopy(targetPackage, newName, false, monitor);
                    if (templateObject instanceof ITimedIpsObject) {
                        if (copyExistingGenerations) {
                            ((ITimedIpsObject)file.getIpsObject()).reassignGenerations(newValidFrom);
                        } else {
                            ((ITimedIpsObject)file.getIpsObject()).retainOnlyGeneration(oldValidFrom, newValidFrom);
                        }
                    }
                } catch (IpsException e) {
                    // exception occurred thus create empty file below
                    createEmptyFile = true;
                }
            } else {
                // if table contents should be created empty or
                // if the file could not be created from template then create an empty file
                file = targetPackage.createIpsFile(templateObject.getIpsObjectType(), newName, false, monitor);
                TableContents ipsObject = (TableContents)file.getIpsObject();
                setPropertiesFromTemplate(templateObject, ipsObject);
            }
        }

        IIpsObject newIpsObject = file.getIpsObject();
        if (newIpsObject instanceof IProductCmpt) {
            productNew2ProductOld.put((IProductCmpt)newIpsObject, (IProductCmpt)templateObject);
        }
        return newIpsObject;
    }

    private void storeTableUsageToNewTableContents(IProductCmptStructureTblUsageReference productCmptStructureReference,
            String newTableContentsQName,
            Map<TblContentUsageData, String> tblContentData2newTableContentQName) {

        ITableContentUsage tblContentUsageOld = productCmptStructureReference.getTableContentUsage();
        tblContentData2newTableContentQName.put(new TblContentUsageData(tblContentUsageOld), newTableContentsQName);
    }

    private void storeLinkToNewNewProductCmpt(IProductCmptReference productCmptStructureReference,
            String newProductCmptQName,
            Map<LinkData, String> linkData2newProductCmptQName) {

        IProductCmptStructureReference parent = productCmptStructureReference.getParent();
        IProductCmpt productCmpt = (productCmptStructureReference).getProductCmpt();
        if (parent instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociation productCmptTypeAssociationOld = ((IProductCmptTypeAssociationReference)parent)
                    .getAssociation();
            IProductCmpt parentProductCmpt = ((IProductCmptReference)parent
                    .getParent()).getProductCmpt();
            linkData2newProductCmptQName.put(
                    new LinkData(parentProductCmpt, productCmpt, productCmptTypeAssociationOld), newProductCmptQName);
        }
    }

    private Set<Object> collectObjectsToRefer() {
        Set<Object> tblContentUsageAndLinkDataRefer = new HashSet<>();
        for (IProductCmptStructureReference productCmptStructureReference : linkElements) {
            if (productCmptStructureReference instanceof IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference) {
                IProductCmptStructureReference parent = productCmptStructureTblUsageReference.getParent();
                if (linkElements.contains(parent)) {
                    /*
                     * the tableContents should be linked, check if the productCmpt for this
                     * tableContentUsage is also a link and if true, don't store this table because
                     * the parent productCmpt must not be fixed
                     */
                    continue;
                }
                tblContentUsageAndLinkDataRefer
                        .add(new TblContentUsageData((productCmptStructureTblUsageReference).getTableContentUsage()));
            } else {
                IProductCmptTypeAssociationReference parentTypeRel = (IProductCmptTypeAssociationReference)productCmptStructureReference
                        .getParent();
                IProductCmptStructureReference parent = parentTypeRel.getParent();
                tblContentUsageAndLinkDataRefer.add(new LinkData((IProductCmpt)parent.getWrappedIpsObject(),
                        (IProductCmpt)productCmptStructureReference.getWrappedIpsObject(),
                        parentTypeRel.getAssociation()));
            }
        }
        return tblContentUsageAndLinkDataRefer;
    }

    /**
     * Fixes all table content usages and all links. Calls the appropriate fix methods with the
     * correct generation(s).
     * 
     * @param productCmptNew the new productCmpt which will be fixed
     * @param productCmptTemplate the template (old) productCmpt which was used to copy the new
     *            productCmpt
     * @param linkData2newProductCmptQName A map containing the link-identifier as key and the new
     *            created productCmpt which was initiated by the key link
     * @param tblContentData2newTableContentQName A map containing the tableContentUsage-identifier
     *            as key and the new created tableContent which was initiated by the key
     *            tableContentUsage
     * @param objectsToRefer A set containing the not copied objects
     */
    private void fixLinks(IProductCmpt productCmptNew,
            IProductCmpt productCmptTemplate,
            Map<LinkData, String> linkData2newProductCmptQName,
            Map<TblContentUsageData, String> tblContentData2newTableContentQName,
            Set<Object> objectsToRefer) {
        if (copyExistingGenerations) {
            for (IIpsObjectGeneration objectGeneration : productCmptNew.getGenerationsOrderedByValidDate()) {
                IProductCmptGeneration generation = (IProductCmptGeneration)objectGeneration;
                fixLinksToTableContents(productCmptNew, productCmptTemplate, tblContentData2newTableContentQName,
                        objectsToRefer, generation.getTableContentUsages());
                fixLinksToProductCmpt(productCmptNew, productCmptTemplate, linkData2newProductCmptQName, objectsToRefer,
                        generation);
            }
        } else {
            IProductCmptGeneration generation = (IProductCmptGeneration)productCmptNew
                    .getGenerationsOrderedByValidDate()[0];
            fixLinksToTableContents(productCmptNew, productCmptTemplate, tblContentData2newTableContentQName,
                    objectsToRefer, generation.getTableContentUsages());
            fixLinksToProductCmpt(productCmptNew, productCmptTemplate, linkData2newProductCmptQName, objectsToRefer,
                    generation);
        }
        fixLinksToTableContents(productCmptNew, productCmptTemplate, tblContentData2newTableContentQName,
                objectsToRefer, productCmptNew.getTableContentUsages());
    }

    /**
     * Fix all tableContentUsages
     * 
     * @param productCmptNew the new productCmpt which will be fixed
     * @param productCmptTemplate the template (old) productCmpt which was used to copy the new
     *            productCmpt
     * @param tblContentData2newTableContentQName A map containing the tableContentUsage-identifier
     *            as key and the new created tableContent which was initiated by the key
     *            tableContentUsage
     * @param objectsToRefer A set containing the not copied objects
     * @param tableContentUsages the {@link TableContentUsage TableContentUsages}
     */
    private void fixLinksToTableContents(IProductCmpt productCmptNew,
            IProductCmpt productCmptTemplate,
            Map<TblContentUsageData, String> tblContentData2newTableContentQName,
            Set<Object> objectsToRefer,
            ITableContentUsage[] tableContentUsages) {
        for (ITableContentUsage tableContentUsage : tableContentUsages) {
            TblContentUsageData tblContentsData = new TblContentUsageData(productCmptTemplate,
                    tableContentUsage.getTableContentName());
            if (objectsToRefer.contains(tblContentsData)) {
                // keep table usage to old table contents
                continue;
            }

            String newTarget = tblContentData2newTableContentQName.get(tblContentsData);
            tableContentUsage.setTableContentName(newTarget == null ? "" : newTarget); //$NON-NLS-1$
        }
    }

    /**
     * Fix all links
     * 
     * @param productCmptNew the new productCmpt which will be fixed
     * @param productCmptTemplate the template (old) productCmpt which was used to copy the new
     *            productCmpt
     * @param linkData2newProductCmptQName A map containing the link-identifier as key and the new
     *            created productCmpt which was initiated by the key link
     * @param objectsToRefer A set containing the not copied objects
     * @param generation the generation which is affected
     */
    private void fixLinksToProductCmpt(IProductCmpt productCmptNew,
            IProductCmpt productCmptTemplate,
            Map<LinkData, String> linkData2newProductCmptQName,
            Set<Object> objectsToRefer,
            IProductCmptGeneration generation) {
        List<IProductCmptLink> links = generation.getLinksIncludingProductCmpt();
        for (IProductCmptLink link : links) {
            LinkData linkData;
            try {
                final IIpsProject ipsProject = productCmptTemplate.getIpsProject();
                IProductCmpt oldTargetProductCmpt = link.findTarget(ipsProject);
                linkData = new LinkData(productCmptTemplate, oldTargetProductCmpt, link.findAssociation(ipsProject));
            } catch (IpsException e) {
                IpsLog.logAndShowErrorDialog(e);
                return;
            }

            String newTarget = linkData2newProductCmptQName.get(linkData);
            if (newTarget != null) {
                link.setTarget(newTarget);
            } else {
                if (objectsToRefer.contains(linkData)) {
                    // keep link to old component
                    continue;
                }
                link.delete();
            }
        }
    }

    private void setPropertiesFromTemplate(IIpsObject template, IIpsObject newObject) {
        if (template instanceof IProductCmpt) {
            ((IProductCmpt)newObject).setProductCmptType(((IProductCmpt)template).getProductCmptType());
        } else if (template instanceof ITableContents) {
            ((ITableContents)newObject).setTableStructure(((ITableContents)template).getTableStructure());
            ((TableContents)newObject).setNumOfColumnsInternal(((ITableContents)template).getNumOfColumns());
        }
    }

    /**
     * Creates a new package, based on the target package. To this base package, the path of the
     * source is appended, after the given number of segments to ignore is cut off.
     */
    private IIpsPackageFragment createTargetPackage(IIpsSrcFile file, IProgressMonitor monitor) {
        String path = file.getIpsPackageFragment().getRelativePath().toString().replace('/', '.');
        return ipsPackageFragmentRoot.createPackageFragment(path, false, monitor);
    }

    public IIpsSrcFile getCopiedRoot() {
        return handleMap.get(structureRoot);
    }

    /**
     * Represents a productCmptLink, which is defined as a link object between two product cmpts
     * (sourceProductCmpt and targetProductCmpt) and a productCmptTypeAssociation on which the link
     * is based on.
     */
    private class LinkData {

        private IProductCmpt sourceProductCmpt;

        private IProductCmpt targetProductCmpt;

        private IProductCmptTypeAssociation productCmptTypeAssociation;

        public LinkData(IProductCmpt sourceProductCmpt, IProductCmpt targetProductCmpt,
                IProductCmptTypeAssociation productCmptTypeAssociation) {

            this.sourceProductCmpt = sourceProductCmpt;
            this.targetProductCmpt = targetProductCmpt;
            this.productCmptTypeAssociation = productCmptTypeAssociation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + ((productCmptTypeAssociation == null) ? 0 : productCmptTypeAssociation.hashCode());
            result = prime * result + ((sourceProductCmpt == null) ? 0 : sourceProductCmpt.hashCode());
            return prime * result + ((targetProductCmpt == null) ? 0 : targetProductCmpt.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            LinkData other = (LinkData)obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            return Objects.equals(productCmptTypeAssociation, other.productCmptTypeAssociation)
                    && Objects.equals(sourceProductCmpt, other.sourceProductCmpt)
                    && Objects.equals(targetProductCmpt, other.targetProductCmpt);
        }

        private DeepCopyOperation getOuterType() {
            return DeepCopyOperation.this;
        }
    }

    /**
     * Represents a tblContentUsage object, which is defined by productCmpt and the table usage name
     * (role of the table contents) inside the productCmpt.
     */
    private static class TblContentUsageData {

        private IProductCmpt productCmpt;

        private String tableUsageName;

        public TblContentUsageData(ITableContentUsage tblContentUsage) {
            this(tblContentUsage.getProductCmpt(), tblContentUsage.getTableContentName());
        }

        public TblContentUsageData(IProductCmpt productCmpt, String tableUsageName) {
            this.productCmpt = productCmpt;
            this.tableUsageName = tableUsageName;
        }

        public IProductCmpt getProductCmpt() {
            return productCmpt;
        }

        public String getTableUsageName() {
            return tableUsageName;
        }

        @Override
        public boolean equals(Object other) {
            if ((this == other)) {
                return true;
            }
            if ((other == null) || !(other instanceof TblContentUsageData castOther)) {
                return false;
            }
            return (getProductCmpt() != null && getProductCmpt().equals(castOther.getProductCmpt()))
                    && (getTableUsageName() != null && getTableUsageName().equals(castOther.getTableUsageName()));
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + productCmpt.hashCode();
            return 37 * result + tableUsageName.hashCode();
        }

    }

}
