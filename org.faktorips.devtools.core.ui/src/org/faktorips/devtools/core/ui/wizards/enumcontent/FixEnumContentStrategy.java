/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Ints;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.core.ui.wizards.fixcontent.DeltaFixWizardStrategy;
import org.faktorips.devtools.core.ui.wizards.fixcontent.TabularContentStrategy;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.ValueTypeMismatch;

/**
 * Strategy implementation of TabularContentStrategy
 * 
 * @author Patrick Bui
 */
public class FixEnumContentStrategy implements TabularContentStrategy<IEnumType, IEnumAttribute> {

    private IEnumContent enumContent;

    public FixEnumContentStrategy(IEnumContent enumContent) {
        this.enumContent = enumContent;
    }

    @Override
    public int getContentValuesCount() {
        return enumContent.getEnumValuesCount();
    }

    @Override
    public void setContentType(String enumType) throws CoreRuntimeException {
        enumContent.setEnumType(enumType);
    }

    @Override
    public void fixAllContentAttributeValues() {
        enumContent.fixAllEnumAttributeValues();
    }

    private List<IEnumValue> getContentValues() {
        return enumContent.getEnumValues();
    }

    @Override
    public IIpsProject getIpsProject() {
        return enumContent.getIpsProject();
    }

    @Override
    public IEnumType findContentType(IIpsProject ipsProject) {
        return enumContent.findEnumType(ipsProject);
    }

    @Override
    public void deleteObsoleteContentAttributeValues(
            AssignContentAttributesPage<IEnumType, IEnumAttribute> assignEnumAttributesPage) {
        // Collect all obsolete EnumAttributeValues to delete.
        List<Integer> notAssignedColumns = assignEnumAttributesPage.getCurrentlyNotAssignedColumns();
        List<IEnumAttributeValue> enumAttributeValuesToDelete = new ArrayList<>();
        for (Integer currentNotAssignedColumn : notAssignedColumns) {
            for (IEnumValue currentEnumValue : getContentValues()) {
                enumAttributeValuesToDelete
                        .add(currentEnumValue.getEnumAttributeValues().get(currentNotAssignedColumn.intValue() - 1));
            }
        }
        // Delete all the collected EnumAttributeValues.
        for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValuesToDelete) {
            currentEnumAttributeValue.delete();
        }

    }

    @Override
    public void createNewContentAttributeValues(
            AssignContentAttributesPage<IEnumType, IEnumAttribute> assignEnumAttributesPage) {
        int[] columnOrder = assignEnumAttributesPage.getColumnOrder();
        for (int currentPosition : columnOrder) {
            if (currentPosition == 0) {
                for (IEnumValue currentEnumValue : getContentValues()) {
                    try {
                        currentEnumValue.newEnumAttributeValue();
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
        }

    }

    @Override
    public void moveAttributeValues(int[] attributeOrdering) {
        int[] currentAttribute = new int[attributeOrdering.length];
        // filling currentColumnOrdering with numbers
        for (int i = 0; i < currentAttribute.length; i++) {
            currentAttribute[i] = i + 1;
        }
        // Using a reverse Insertion Sort to bring the columns to the right position
        for (int i = 0; i < currentAttribute.length; i++) {
            if (Arrays.equals(attributeOrdering, currentAttribute)) {
                break;
            }
            int indexDestination = Ints.indexOf(currentAttribute, attributeOrdering[i]);
            // No swapping needed if the EnumAttribute is already at the right position
            if (i == indexDestination) {
                continue;
            }
            for (IEnumValue currentEnumValue : getContentValues()) {
                currentEnumValue.swapEnumAttributeValue(i, indexDestination);
            }
            swapValues(currentAttribute, i, indexDestination);

        }
    }

    private void swapValues(int[] order, int firstIndex, int secondIndex) {
        int temp = order[firstIndex];
        order[firstIndex] = order[secondIndex];
        order[secondIndex] = temp;
    }

    @Override
    public Map<String, ValueTypeMismatch> checkAllContentAttributeValueTypeMismatch() {
        return enumContent.checkAllEnumAttributeValueTypeMismatch();
    }

    @Override
    public String getImage() {
        return "wizards/BrokenEnumWizard.png"; //$NON-NLS-1$
    }

    @Override
    public DeltaFixWizardStrategy<IEnumType, IEnumAttribute> createContentPageStrategy() {
        return new FixEnumWizardStrategy(enumContent);
    }
}
