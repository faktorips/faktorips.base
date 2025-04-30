/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.beans.PropertyChangeEvent;
import java.util.GregorianCalendar;

import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeStatus;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * Presentation model for the "Update Valid From" wizard page.
 */
public class UpdateValidfromPresentationModel extends PresentationModelObject {

    public static final String NEW_VALID_FROM = "newValidFrom"; //$NON-NLS-1$
    public static final String NEW_VERSION_ID = "newVersionId"; //$NON-NLS-1$
    public static final String CHANGE_GENERATION_ID = "changeGenerationId"; //$NON-NLS-1$
    public static final String CHANGE_ATTRIBUTES = "changeAttributes"; //$NON-NLS-1$

    private final IProductCmpt productCmpt;

    private IProductCmptTreeStructure structure;

    private DeepCopyTreeStatus treeStatus;

    private GregorianCalendar newValidFrom;
    private String newVersionId = ""; //$NON-NLS-1$

    private boolean changeGenerationId = true;
    private boolean changeAttributes = false;

    public UpdateValidfromPresentationModel(IProductCmpt productCmpt) {
        treeStatus = new DeepCopyTreeStatus();
        this.productCmpt = productCmpt;
        initialiseOldValidFrom();
    }

    public DeepCopyTreeStatus getTreeStatus() {
        return treeStatus;
    }

    public IProductCmpt getProductCmpt() {
        return productCmpt;
    }

    /**
     * Initializes the internal structure based on the current valid-from of the root product.
     */
    private void initialiseOldValidFrom() {
        try {
            IProductCmptTreeStructure newStructure = productCmpt.getStructure(productCmpt.getValidFrom(),
                    productCmpt.getIpsProject());
            initialize(newStructure);
        } catch (CycleInProductStructureException e) {
            return;
        }
    }

    private void initialize(IProductCmptTreeStructure structure) {
        this.structure = structure;
        treeStatus.initialize(structure);
    }

    public GregorianCalendar getNewValidFrom() {
        return newValidFrom;
    }

    public void setNewValidFrom(GregorianCalendar newValue) {
        GregorianCalendar oldValue = newValidFrom;
        newValidFrom = newValue;
        notifyListeners(new PropertyChangeEvent(this, NEW_VALID_FROM, oldValue, newValue));
    }

    public String getNewVersionId() {
        return newVersionId;
    }

    public void setNewVersionId(String newValue) {
        String oldValue = newVersionId;
        newVersionId = newValue;
        notifyListeners(new PropertyChangeEvent(this, NEW_VERSION_ID, oldValue, newValue));
    }

    public boolean isChangeGenerationId() {
        return changeGenerationId;
    }

    public void setChangeGenerationId(boolean newValue) {
        boolean oldValue = changeGenerationId;
        changeGenerationId = newValue;
        notifyListeners(new PropertyChangeEvent(this, CHANGE_GENERATION_ID, oldValue, newValue));
    }

    public boolean isChangeAttributes() {
        return changeAttributes;
    }

    public void setChangeAttributes(boolean newValue) {
        boolean oldValue = changeAttributes;
        changeAttributes = newValue;
        notifyListeners(new PropertyChangeEvent(this, CHANGE_ATTRIBUTES, oldValue, newValue));
    }

    public IProductCmptTreeStructure getStructure() {
        return structure;
    }

    public IIpsProject getIpsProject() {
        return structure.getRoot().getProductCmpt().getIpsProject();
    }

}
