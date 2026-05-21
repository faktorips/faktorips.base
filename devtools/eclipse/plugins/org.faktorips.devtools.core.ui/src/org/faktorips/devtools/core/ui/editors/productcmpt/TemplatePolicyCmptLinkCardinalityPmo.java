/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.beans.PropertyChangeEvent;
import java.util.function.Supplier;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * @since 26.7
 */
public class TemplatePolicyCmptLinkCardinalityPmo extends AbstractTemplateValuePmo<IPolicyCmptLinkCardinality> {

    public static final String PROPERTY_STATUS_BUTTON_ENABLED = "statusButtonEnabled"; //$NON-NLS-1$

    private Supplier<IPolicyCmptLinkCardinality> cardinalityCreator;

    @Override
    public TemplateValueUiStatus getTemplateValueStatus() {
        if (isCardinalityAvailable()) {
            return TemplateValueUiStatus.mapStatus(getTemplatedProperty());
        }
        return TemplateValueUiStatus.NEWLY_DEFINED;
    }

    private boolean isCardinalityAvailable() {
        return getTemplatedProperty() != null;
    }

    @Override
    @CheckForNull
    public String getToolTipText() {
        if (!isCardinalityAvailable()) {
            return Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_NewlyDefined;
        }
        return switch (getTemplateValueStatus()) {
            case INHERITED -> NLS.bind(Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_Inherited,
                    getAssociationLabel(), getTemplateName());
            case OVERWRITE -> NLS.bind(Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_Override,
                    getAssociationLabel(), getTemplateName());
            case OVERWRITE_EQUAL -> NLS.bind(Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_OverrideEqual,
                    getTemplateName());
            case UNDEFINED -> NLS.bind(Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_Delete,
                    getAssociationLabel(), getTemplateName());
            case NEWLY_DEFINED -> Messages.TemplatePolicyCmptLinkCardinalityPmo_Status_NewlyDefined;
        };
    }

    @Override
    public void onClick() {
        if (!isCardinalityAvailable()) {
            IPolicyCmptLinkCardinality created = createCardinality();
            if (created == null) {
                return;
            }
            setCardinality(created);
        }
        super.onClick();
    }

    private IPolicyCmptLinkCardinality createCardinality() {
        if (cardinalityCreator != null) {
            return cardinalityCreator.get();
        }
        return null;
    }

    private String getAssociationLabel() {
        return getTemplatedProperty().getAssociation();
    }

    public void setCardinality(IPolicyCmptLinkCardinality cardinality) {
        setTemplatedProperty(cardinality);
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_STATUS_BUTTON_ENABLED, null, null));
    }

    public void setCardinalityCreator(Supplier<IPolicyCmptLinkCardinality> creator) {
        cardinalityCreator = creator;
    }

    @CheckForNull
    public IPolicyCmptLinkCardinality getCardinality() {
        return getTemplatedProperty();
    }

    public boolean isStatusButtonEnabled() {
        return cardinalityCreator != null || isCardinalityAvailable();
    }

    @CheckForNull
    public IPolicyCmptLinkCardinality findTemplateCardinality() {
        if (!isCardinalityAvailable()) {
            return null;
        }
        return findTemplateProperty();
    }

}
