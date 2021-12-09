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
import java.util.List;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.InvalidGenerationsDeltaEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;

public class ProductCmptToTypeDelta extends PropertyValueContainerToTypeDelta {

    public ProductCmptToTypeDelta(IProductCmpt productCmpt, IIpsProject ipsProject) throws CoreRuntimeException {
        super(productCmpt, productCmpt, ipsProject);
    }

    @Override
    public IProductCmpt getPropertyValueContainer() {
        return (IProductCmpt)super.getPropertyValueContainer();
    }

    @Override
    protected void createAdditionalEntriesAndChildren() throws CoreRuntimeException {
        checkInvalidGenerations();
        for (IIpsObjectGeneration generation : getPropertyValueContainer().getGenerationsOrderedByValidDate()) {
            ProductCmptGeneration productCmptGen = (ProductCmptGeneration)generation;
            IPropertyValueContainerToTypeDelta computeDeltaToModel = productCmptGen
                    .computeDeltaToModel(getIpsProject());
            addChild(computeDeltaToModel);
        }
        findAndSetPredecessors();
    }

    private void checkInvalidGenerations() {
        if (!getPropertyValueContainer().allowGenerations()
                && getPropertyValueContainer().getGenerations().size() > 1) {
            addEntry(new InvalidGenerationsDeltaEntry(getPropertyValueContainer()));
        }
    }

    private void findAndSetPredecessors() {
        List<ValueWithoutPropertyEntry> valueWithoutPropertyEntries = new ArrayList<>();
        List<MissingPropertyValueEntry> missingPropertyValueEntries = new ArrayList<>();

        addEntries(getEntries(), valueWithoutPropertyEntries, missingPropertyValueEntries);
        for (IFixDifferencesComposite fixDifferencesComposite : getChildren()) {
            PropertyValueContainerToTypeDelta propertyValueContainerToTypeDelta = (PropertyValueContainerToTypeDelta)fixDifferencesComposite;
            addEntries(propertyValueContainerToTypeDelta.getEntries(), valueWithoutPropertyEntries,
                    missingPropertyValueEntries);
        }

        for (ValueWithoutPropertyEntry valueWithoutPropertyEntry : valueWithoutPropertyEntries) {
            for (MissingPropertyValueEntry missingPropertyValueEntry : missingPropertyValueEntries) {
                if (isMatchingPropertyValue(valueWithoutPropertyEntry, missingPropertyValueEntry)) {
                    missingPropertyValueEntry.setPredecessor(valueWithoutPropertyEntry);
                }
            }
        }

    }

    private boolean isMatchingPropertyValue(ValueWithoutPropertyEntry valueWithoutPropertyEntry,
            MissingPropertyValueEntry missingPropertyValueEntry) {
        return valueWithoutPropertyEntry.getPropertyName().equals(missingPropertyValueEntry.getPropertyName())
                && valueWithoutPropertyEntry.getPropertyType().equals(missingPropertyValueEntry.getPropertyType());
    }

    protected void addEntries(IDeltaEntry[] entries,
            List<ValueWithoutPropertyEntry> valueWithoutPropertyEntries,
            List<MissingPropertyValueEntry> missingPropertyEntries) {
        for (IDeltaEntry entry : entries) {
            if (entry instanceof ValueWithoutPropertyEntry) {
                valueWithoutPropertyEntries.add((ValueWithoutPropertyEntry)entry);
            }
            if (entry instanceof MissingPropertyValueEntry) {
                missingPropertyEntries.add((MissingPropertyValueEntry)entry);
            }
        }
    }

}
