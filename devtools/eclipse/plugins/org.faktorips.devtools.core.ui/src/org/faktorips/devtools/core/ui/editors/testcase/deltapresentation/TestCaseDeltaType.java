/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.INamedValue;
import org.faktorips.util.ArgumentCheck;

/**
 * Type for test case deltas. The following types are supported:
 * <ul>
 * <li>MISSING_ROOT_TEST_OBJECT (Test case side) - Missing root test objects (will be added). A
 * config element test parameter exists, but the test objects are missing (e.g. test value or test
 * policy cmpt)
 * <li>MISSING_TEST_ATTRIBUTE_VALUE (Test case side) - Missing test attribute values (will be
 * added). A config element test attribute exists, but the test attribute value is missing.
 * <li>DIFFERENT_SORT_ORDER (Test case side) - Different order
 * <li>MISSING_TEST_PARAM (Test case type side) - Missing test parameters (will be removed). A test
 * policy cmpt, test policy cmpt link, or test value exists, but the config element is missing.
 * <li>MISSING_TEST_ATTRIBUTE (Test case type side) - Missing test attributes (will be removed). A
 * test policy cmpt attribute exists, but the config element test attribute is missing.
 * </ul>
 * 
 * @author Joerg Ortmann
 */
enum TestCaseDeltaType implements INamedValue {

    /* Test case side */

    /**
     * A config element test parameter exists, but the test objects are missing (e.g. test value or
     * test policy cmpt)
     */
    MISSING_ROOT_TEST_OBJECT("missingRootTestObject", Messages.TestCaseDeltaType_MissingRootTestObject, //$NON-NLS-1$
            "DeltaTypeMissingPropertyValue.gif"), //$NON-NLS-1$
    /**
     * A config element test attribute exists, but the test attribute value is missing.
     */
    MISSING_TEST_ATTRIBUTE_VALUE("missingTestAttributeValue", Messages.TestCaseDeltaType_MissingTestAttributeValue, //$NON-NLS-1$
            "DeltaTypeMissingPropertyValue.gif"), //$NON-NLS-1$

    /* Test case type side */

    /**
     * A test policy cmpt, test policy cmpt link, or test value exists, but the config element is
     * missing.
     */
    MISSING_TEST_PARAM("missingTestParam", Messages.TestCaseDeltaType_MissingTestParam, //$NON-NLS-1$
            "DeltaTypeValueWithoutProperty.gif"), //$NON-NLS-1$
    /**
     * A test policy cmpt attribute exists, but the config element test attribute is missing.
     */
    MISSING_TEST_ATTRIBUTE("missingTestAttribute", Messages.TestCaseDeltaType_MissingTestAttribute, //$NON-NLS-1$
            "DeltaTypeValueWithoutProperty.gif"), //$NON-NLS-1$

    /**
     * The sort order of the test parameter and the corresponding test objects is different.
     */
    DIFFERENT_SORT_ORDER("differentSortOrder", Messages.TestCaseDeltaType_DifferentSortOrder, "ChangedOrder.gif"); //$NON-NLS-1$ //$NON-NLS-2$

    private final String id;
    private final String name;
    private final ImageDescriptor imageDescriptor;

    TestCaseDeltaType(String id, String name, String icon) {
        ArgumentCheck.notNull(id);
        this.id = id;
        ArgumentCheck.notNull(name);
        this.name = name;
        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(icon);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The image for this type.
     */
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }
}
