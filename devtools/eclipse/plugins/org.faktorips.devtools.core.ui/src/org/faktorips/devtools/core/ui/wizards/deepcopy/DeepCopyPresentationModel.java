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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDateContentProvider;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

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
    public static final String COPY_EXISTING_GENERATIONS = "copyExistingGenerations"; //$NON-NLS-1$

    private final IProductCmptGeneration productCmptGeneration;

    private final List<GenerationDate> generationDates;

    private GenerationDate oldValidFrom;
    private GregorianCalendar newValidFrom;
    private String versionIdString = ""; //$NON-NLS-1$
    private String searchInputString = ""; //$NON-NLS-1$
    private String replaceInputString = ""; //$NON-NLS-1$
    private IIpsPackageFragment targetPackage;
    private IIpsPackageFragmentRoot targetPackageRoot;
    private boolean copyTable;
    private boolean copyExistingGenerations;

    private IProductCmptTreeStructure structure;

    private Pattern searchPattern;

    private DeepCopyTreeStatus treeStatus;

    public DeepCopyPresentationModel(IProductCmptGeneration productCmptGeneration) {
        treeStatus = new DeepCopyTreeStatus();
        this.productCmptGeneration = productCmptGeneration;
        List<GenerationDate> generationDatesInternal;
        try {
            generationDatesInternal = new GenerationDateContentProvider().collectGenerationDates(
                    productCmptGeneration.getProductCmpt(), null);
        } catch (IpsException e) {
            IpsPlugin.log(e);
            generationDatesInternal = new ArrayList<>();
        }
        generationDates = generationDatesInternal;
        setOldValidFromDate(productCmptGeneration.getValidFrom());
    }

    private void setOldValidFromDate(GregorianCalendar validAt) {
        for (GenerationDate generationDate : generationDates) {
            if (!validAt.before(generationDate.getValidFrom())) {
                // elements are sorted, newest generation first, so we only have to check
                // the validFrom date
                setOldValidFrom(generationDate);
                break;
            }
        }
    }

    private void initialize(IProductCmptTreeStructure structure) {
        this.structure = structure;
        treeStatus.initialize(structure);
    }

    public GenerationDate getOldValidFrom() {
        return oldValidFrom;
    }

    public void setOldValidFrom(GenerationDate newValue) {
        GenerationDate oldValue = oldValidFrom;
        oldValidFrom = newValue;

        try {
            IProductCmpt productCmpt = productCmptGeneration.getProductCmpt();
            IProductCmptTreeStructure newStructure = productCmpt.getStructure(getOldValidFrom().getValidFrom(),
                    productCmpt.getIpsProject());
            initialize(newStructure);

        } catch (CycleInProductStructureException e) {
            Exception exp = new Exception(handleCycles(e));
            IpsPlugin.logAndShowErrorDialog(exp);
            setOldValidFrom(oldValue);
            return;
        }

        notifyListeners(new PropertyChangeEvent(this, OLD_VALID_FROM, oldValue, newValue));
    }

    private String handleCycles(CycleInProductStructureException e) {
        IIpsElement[] cyclePath = e.getCyclePath();
        String errorMsg = Messages.DeepCopyPresentationModel_labelCircleRelation;

        String path = Arrays.stream(cyclePath).toList().reversed().stream()
                .filter(Objects::nonNull)
                .map(IIpsElement::getName)
                .collect(Collectors.joining(" -> "));

        return errorMsg + path;
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

    public void setCopyTable(boolean copyTable) {
        boolean oldValue = isCopyTable();
        this.copyTable = copyTable;
        notifyListeners(new PropertyChangeEvent(this, COPY_TABLE, oldValue, copyTable));
    }

    public boolean isCopyTable() {
        return copyTable;
    }

    public void setCopyExistingGenerations(boolean copyExistingGenerations) {
        boolean oldValue = isCopyExistingGenerations();
        this.copyExistingGenerations = copyExistingGenerations;
        notifyListeners(new PropertyChangeEvent(this, COPY_EXISTING_GENERATIONS, oldValue, copyExistingGenerations));
    }

    public boolean isCopyExistingGenerations() {
        return copyExistingGenerations;
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
     * @param includingAssociations true to include associations, false to collect only
     *            compositions/aggregations
     *
     * @see DeepCopyTreeStatus#isEnabled(IProductCmptStructureReference)
     */
    public Set<IProductCmptStructureReference> getAllCopyElements(boolean includingAssociations) {
        return treeStatus.getAllEnabledElements(CopyOrLink.COPY, structure, includingAssociations);
    }

    /**
     * Get all enabled elements, that are marked to link.
     *
     * @see DeepCopyTreeStatus#isEnabled(IProductCmptStructureReference)
     */
    public Set<IProductCmptStructureReference> getLinkedElements() {
        return treeStatus.getAllEnabledElements(CopyOrLink.LINK, structure, true);
    }

    public List<GenerationDate> getGenerationDates() {
        return generationDates;
    }
}
