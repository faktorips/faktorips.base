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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeStatus;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

/**
 * Presentation model for the "Update Valid From" wizard page.
 */
public class UpdateValidfromPresentationModel extends PresentationModelObject {

    public static final String NEW_VALID_FROM = "newValidFrom"; //$NON-NLS-1$
    public static final String NEW_VERSION_ID = "newVersionId"; //$NON-NLS-1$
    public static final String CHANGE_GENERATION_ID = "changeGenerationId"; //$NON-NLS-1$
    public static final String CHANGE_ATTRIBUTES = "changeAttributes"; //$NON-NLS-1$
    public static final String STRUCTURE = "structure"; //$NON-NLS-1$

    public static final String MSG_CODE_EMPTY_NEW_VALID_FROM = "empty_" + NEW_VALID_FROM; //$NON-NLS-1$
    public static final String MSG_CODE_EMPTY_NEW_VERSION_ID = "empty_" + NEW_VERSION_ID; //$NON-NLS-1$
    public static final String MSG_CODE_VALID_FROM_DATE_FORMAT_ERROR = "validFromDateFormatError"; //$NON-NLS-1$
    public static final String MSG_CODE_VALID_FROM_MOVED_PAST_NEXT_GENERATION = "validFromMovedPastNextGeneration"; //$NON-NLS-1$

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

    /** Validates user inputs and sets error message if necessary. */
    MessageList validate() {
        var messageList = new MessageList();
        if (getNewValidFrom() == null) {
            messageList.newError(MSG_CODE_EMPTY_NEW_VALID_FROM,
                    Messages.UpdateValidFromSourcePage_emptyValidFomDateError,
                    new ObjectProperty(this, NEW_VALID_FROM));
            return messageList;
        }

        if (isChangeGenerationId() && StringUtils.isBlank(getNewVersionId())) {
            messageList.newError(MSG_CODE_EMPTY_NEW_VERSION_ID, Messages.UpdateValidFromSourcePage_emptyVersionIdError,
                    new ObjectProperty(this, NEW_VERSION_ID));
            return messageList;
        }

        validateWorkingDate(messageList);
        if (messageList.containsErrorMsg()) {
            return messageList;
        }

        IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
        if (namingStrategy != null && namingStrategy.supportsVersionId() && isChangeGenerationId()) {
            MessageList validation = namingStrategy.validateVersionId(getNewVersionId());
            if (validation.containsErrorMsg()) {
                messageList.add(validation);
                return messageList;
            }
        }

        validateAdjustments(messageList);
        return messageList;
    }

    private IProductCmptNamingStrategy getNamingStrategy() {
        return getIpsProject().getProductCmptNamingStrategy();
    }

    private void validateAdjustments(MessageList messageList) {
        DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        if (productCmpt.allowGenerations() && productCmpt.getNumOfGenerations() > 1) {
            GregorianCalendar secondGenValidFrom = productCmpt.getGenerationsOrderedByValidDate()[1].getValidFrom();
            if (secondGenValidFrom != null && secondGenValidFrom.before(getNewValidFrom())) {
                var changesOverTimeNamingConvention = IpsPlugin.getDefault().getIpsPreferences()
                        .getChangesOverTimeNamingConvention();
                messageList.newWarning(MSG_CODE_VALID_FROM_MOVED_PAST_NEXT_GENERATION,
                        NLS.bind(Messages.UpdateValidFromSourcePage_ValidFromMovedPastNextGeneration,
                                new Object[] {
                                        format.format(newValidFrom.getTime()),
                                        changesOverTimeNamingConvention
                                                .getGenerationConceptNameSingular(true),
                                        format.format(secondGenValidFrom.getTime()),
                                        changesOverTimeNamingConvention
                                                .getGenerationConceptNamePlural(true)
                                }),
                        new ObjectProperty(this, NEW_VALID_FROM));
            }
        }
    }

    /** Validates format of the entered date. */
    private void validateWorkingDate(MessageList messageList) {
        Calendar calendar = getNewValidFrom();
        if (calendar == null) {
            String pattern;
            DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
            if (dateFormat instanceof SimpleDateFormat) {
                pattern = ((SimpleDateFormat)dateFormat).toLocalizedPattern();
            } else {
                pattern = "\"" + dateFormat.format(new GregorianCalendar().getTime()) + "\""; // NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
            }
            messageList.newError(MSG_CODE_VALID_FROM_DATE_FORMAT_ERROR,
                    NLS.bind(Messages.UpdateValidFromSourcePage_ValidFromDateFormatError, pattern),
                    new ObjectProperty(this, NEW_VALID_FROM));
        }
    }
}
