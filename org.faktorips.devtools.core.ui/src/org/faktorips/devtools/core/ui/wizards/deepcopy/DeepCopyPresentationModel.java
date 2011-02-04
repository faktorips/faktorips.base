/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.beans.PropertyChangeEvent;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;

public class DeepCopyPresentationModel extends PresentationModelObject {

    public static final String OLD_VALID_FROM = "oldValidFrom"; //$NON-NLS-1$
    public static final String NEW_VALID_FROM = "newValidFrom"; //$NON-NLS-1$
    public static final String VERSION_ID = "versionId"; //$NON-NLS-1$
    public static final String SEARCH_INPUT = "searchInput"; //$NON-NLS-1$
    public static final String REPLACE_INPUT = "replaceInput"; //$NON-NLS-1$
    public static final String TARGET_PACKAGE = "targetPackage"; //$NON-NLS-1$
    public static final String PACKAGE_FRAGMENT_ROOT = "ipsPckFragmentRoot"; //$NON-NLS-1$
    public static final String TARGET_PACKAGE_ROOT = "targetPackageRoot"; //$NON-NLS-1$
    public static final String COPY_TABLE = "copyTable"; //$NON-NLS-1$
    // this is the negation of copy table but is modeled in two values to have binding with radio
    // buttons for better usability
    public static final String CREATE_EMPTY_TABLE = "createEmptyTable"; //$NON-NLS-1$

    private GenerationDate oldValidFrom;
    private GregorianCalendar newValidFrom;
    private String versionIdString = ""; //$NON-NLS-1$
    private String searchInputString = ""; //$NON-NLS-1$
    private String replaceInputString = ""; //$NON-NLS-1$
    private IIpsPackageFragment targetPackage;
    private IIpsPackageFragmentRoot targetPackageRoot;
    private boolean copyTable;

    private IProductCmptTreeStructure structure;

    private Pattern searchPattern;

    private DeepCopyTreeStatus treeStatus;

    public DeepCopyPresentationModel(IProductCmptTreeStructure structure) {
        initialize(structure);
    }

    protected void initialize(IProductCmptTreeStructure structure) {
        this.structure = structure;
        initTreeStatusDefaults();
    }

    private void initTreeStatusDefaults() {
        treeStatus = new DeepCopyTreeStatus();
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                // no status for associations is needed
                continue;
            } else {
                LinkStatus status = treeStatus.getStatus((IIpsObjectPart)reference.getWrapped());
                if (reference instanceof IProductCmptReference) {
                    IProductCmptReference cmptReference = (IProductCmptReference)reference;
                    IProductCmptTypeAssociationReference parent = (IProductCmptTypeAssociationReference)cmptReference
                            .getParent();
                    if (parent != null && parent.getAssociation().isAssoziation()) {
                        // default for associated product components is linked, not copy
                        status.setCopyOrLink(CopyOrLink.LINK);
                    }
                }
            }
        }
    }

    public GenerationDate getOldValidFrom() {
        return oldValidFrom;
    }

    public void setOldValidFrom(GenerationDate newValue) {
        GenerationDate oldValue = this.oldValidFrom;
        this.oldValidFrom = newValue;
        notifyListeners(new PropertyChangeEvent(this, OLD_VALID_FROM, oldValue, newValue));
    }

    public GregorianCalendar getNewValidFrom() {
        return newValidFrom;
    }

    public void setNewValidFrom(GregorianCalendar newValue) {
        GregorianCalendar oldValue = newValidFrom;
        newValidFrom = newValue;
        notifyListeners(new PropertyChangeEvent(this, NEW_VALID_FROM, oldValue, newValue));
    }

    public String getVersionId() {
        return versionIdString;
    }

    public void setVersionId(String newValue) {
        String oldValue = versionIdString;
        versionIdString = newValue;
        notifyListeners(new PropertyChangeEvent(this, VERSION_ID, oldValue, newValue));
    }

    public String getSearchInput() {
        return searchInputString;
    }

    public void setSearchInput(String newValue) {
        String oldValue = searchInputString;
        searchInputString = newValue;
        searchPattern = null;
        notifyListeners(new PropertyChangeEvent(this, SEARCH_INPUT, oldValue, newValue));
    }

    public String getReplaceInput() {
        return replaceInputString;
    }

    public void setReplaceInput(String newValue) {
        String oldValue = replaceInputString;
        replaceInputString = newValue;
        notifyListeners(new PropertyChangeEvent(this, REPLACE_INPUT, oldValue, newValue));
    }

    public IIpsPackageFragment getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(IIpsPackageFragment newValue) {
        IIpsPackageFragment oldValue = targetPackage;
        targetPackage = newValue;
        notifyListeners(new PropertyChangeEvent(this, TARGET_PACKAGE, oldValue, newValue));
    }

    public IIpsPackageFragmentRoot getTargetPackageRoot() {
        return targetPackageRoot;
    }

    public void setTargetPackageRoot(IIpsPackageFragmentRoot newValue) {
        IIpsPackageFragmentRoot oldValue = targetPackageRoot;
        targetPackageRoot = newValue;
        notifyListeners(new PropertyChangeEvent(this, TARGET_PACKAGE_ROOT, oldValue, newValue));
    }

    public IProductCmptTreeStructure getStructure() {
        return structure;
    }

    public IIpsProject getIpsProject() {
        return structure.getRoot().getProductCmpt().getIpsProject();
    }

    // /**
    // * Note: The linked objects are populated by the source page. It is not updated by the binding
    // * context and does NEVER trigger a change event!
    // */
    // public Set<IProductCmptStructureReference> getLinkedElements() {
    // return new HashSet<IProductCmptStructureReference>();
    // }

    // /**
    // * Returns all {@link IProductCmptReference} and {@link
    // IProductCmptStructureTblUsageReference}
    // * that should be copied (checked and not linked).
    // */
    // Set<IProductCmptStructureReference> getAllCopyElements() {
    // if (allCopyElementsCached != null) {
    // return allCopyElementsCached;
    // }
    // Set<IProductCmptStructureReference> result = new
    // HashSet<IProductCmptStructureReference>();
    // Set<IProductCmptStructureReference> linkedElements =
    // presentationModel.getLinkedElements();
    // for (IProductCmptStructureReference checkedElement :
    // presentationModel.getCheckedElementsSet()) {
    // if (linkedElements.contains(checkedElement)) {
    // continue;
    // }
    // if (checkedElement instanceof IProductCmptReference
    // || checkedElement instanceof IProductCmptStructureTblUsageReference) {
    // result.add(checkedElement);
    // }
    // }
    // allCopyElementsCached = result;
    // return allCopyElementsCached;
    // return new HashSet<IProductCmptStructureReference>();
    // }

    public void setCopyTable(boolean copyTable) {
        boolean oldValue = isCopyTable();
        this.copyTable = copyTable;
        notifyListeners(new PropertyChangeEvent(this, COPY_TABLE, oldValue, copyTable));
    }

    public boolean isCopyTable() {
        return copyTable;
    }

    public void setCreateEmptyTable(boolean createEmptyTable) {
        boolean oldValue = isCreateEmptyTable();
        this.copyTable = !createEmptyTable;
        notifyListeners(new PropertyChangeEvent(this, CREATE_EMPTY_TABLE, oldValue, createEmptyTable));
    }

    public boolean isCreateEmptyTable() {
        return !isCopyTable();
    }

    public Pattern getSearchPattern() {
        if (searchPattern == null) {
            searchPattern = Pattern.compile(searchInputString);
        }
        return searchPattern;
    }

    public DeepCopyTreeStatus getTreeStatus() {
        return treeStatus;
    }

    /**
     * Get all enabled elements, that are marked to copy.
     * 
     * @see DeepCopyTreeStatus#isEnabled(IProductCmptStructureReference)
     */
    public Set<IProductCmptStructureReference> getAllCopyElements() {
        return getAllCheckedElements(CopyOrLink.COPY);
    }

    /**
     * Get all enabled elements, that are marked to link.
     * 
     * @see DeepCopyTreeStatus#isEnabled(IProductCmptStructureReference)
     */
    public Set<IProductCmptStructureReference> getLinkedElements() {
        return getAllCheckedElements(CopyOrLink.LINK);
    }

    private Set<IProductCmptStructureReference> getAllCheckedElements(CopyOrLink copyOrLink) {
        HashSet<IProductCmptStructureReference> result = new HashSet<IProductCmptStructureReference>();
        Set<IProductCmptStructureReference> set = structure.toSet(false);
        for (IProductCmptStructureReference reference : set) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                continue;
            }
            LinkStatus status = treeStatus.getStatus((IIpsObjectPart)reference.getWrapped());
            if (treeStatus.isEnabled(reference) && status.getCopyOrLink() == copyOrLink) {
                result.add(reference);
            }
        }
        return result;
    }

}
