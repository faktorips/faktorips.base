/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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
 * policy cmpt, test policy cmpt link, or test value exisits, but the config element is missing.
 * <li>MISSING_TEST_ATTRIBUTE (Test case type side) - Missing test attributes (will be removed). A
 * test policy cmpt attribute exists, but the config element test attribute is missing.
 * </ul>
 * 
 * @author Joerg Ortmann
 */
class TestCaseDeltaType extends DefaultEnumValue {

    /* Test case side */

    /**
     * A config element test parameter exists, but the test objects are missing (e.g. test value or
     * test policy cmpt)
     */
    public static final TestCaseDeltaType MISSING_ROOT_TEST_OBJECT;

    /**
     * A config element test attribute exists, but the test attribute value is missing.
     */
    public static final TestCaseDeltaType MISSING_TEST_ATTRIBUTE_VALUE;

    /* Test case type side */

    /**
     * A test policy cmpt, test policy cmpt link, or test value exisits, but the config element is
     * missing.
     */
    public static final TestCaseDeltaType MISSING_TEST_PARAM;

    /**
     * A test policy cmpt attribute exists, but the config element test attribute is missing.
     */
    public static final TestCaseDeltaType MISSING_TEST_ATTRIBUTE;

    /**
     * The sort order of the test parameter and the corresponding test objetcs is different.
     */
    public static final TestCaseDeltaType DIFFERENT_SORT_ORDER;

    private final static DefaultEnumType enumType;

    static {
        enumType = new DefaultEnumType("TestCaseDeltaType", TestCaseDeltaType.class); //$NON-NLS-1$
        MISSING_ROOT_TEST_OBJECT = new TestCaseDeltaType(
                enumType,
                "missingRootTestObject", Messages.TestCaseDeltaType_MissingRootTestObject, "DeltaTypeMissingPropertyValue.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
        MISSING_TEST_ATTRIBUTE_VALUE = new TestCaseDeltaType(
                enumType,
                "missingTestAttributeValue", Messages.TestCaseDeltaType_MissingTestAttributeValue, "DeltaTypeMissingPropertyValue.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 

        MISSING_TEST_PARAM = new TestCaseDeltaType(enumType,
                "missingTestParam", Messages.TestCaseDeltaType_MissingTestParam, "DeltaTypeValueWithoutProperty.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
        MISSING_TEST_ATTRIBUTE = new TestCaseDeltaType(
                enumType,
                "missingTestAttribute", Messages.TestCaseDeltaType_MissingTestAttribute, "DeltaTypeValueWithoutProperty.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 

        DIFFERENT_SORT_ORDER = new TestCaseDeltaType(enumType,
                "differentSortOrder", Messages.TestCaseDeltaType_DifferentSortOrder, "ChangedOrder.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
    }

    private ImageDescriptor imageDescriptor;

    private TestCaseDeltaType(DefaultEnumType type, String id, String name, String icon) {
        super(type, id, name);
        imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(icon);
    }

    /**
     * @return The image for this type.
     */
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }
}
