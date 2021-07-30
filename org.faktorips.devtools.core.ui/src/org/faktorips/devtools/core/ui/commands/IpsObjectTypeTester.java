/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.model.IIpsMetaClass;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Tests a given {@link IIpsSrcFile}'s {@link IpsObjectType}. The following properties may be
 * checked:
 * <ul>
 * <li>{@value #PROPERTY_IS_IPS_OBJECT_TYPE}: matches the {@link IpsObjectType#getId()
 * IpsObjectType-ID} given as expected value, for example
 * 
 * <pre>
 * {@code
<adapt type="org.faktorips.devtools.model.ipsobject.IIpsSrcFile">
  <test
        property="org.faktorips.devtools.core.ui.commands.IpsObjectTypeTester.isIpsObjectType"
        value="ProductCmpt">
  </test>
</adapt>}
 * </pre>
 * 
 * matches {@link IpsObjectType#PRODUCT_CMPT}
 *
 * </li>
 * <li>{@value #PROPERTY_IS_TYPE}: checks whether the object type is one derived from {@link IType}
 * ({@link IpsObjectType#POLICY_CMPT_TYPE} or {@link IpsObjectType#PRODUCT_CMPT_TYPE})</li>
 * <li>{@value #PROPERTY_IS_META_TYPE}: checks whether the object type is one derived from
 * {@link IIpsMetaClass}</li>
 * <li>{@value #PROPERTY_IS_MODEL_TYPE}: checks whether the object type is a model object type (not
 * a {@link IpsObjectType#isProductDefinitionType() product definition type})</li>
 * <li>{@value #PROPERTY_IS_PRODUCT_TYPE}: checks whether the object type is a
 * {@link IpsObjectType#isProductDefinitionType() product definition type}</li>
 * </ul>
 */
public class IpsObjectTypeTester extends PropertyTester {

    public static final String PROPERTY_IS_IPS_OBJECT_TYPE = "isIpsObjectType"; //$NON-NLS-1$
    public static final String PROPERTY_IS_TYPE = "isType"; //$NON-NLS-1$
    public static final String PROPERTY_IS_META_TYPE = "isMetaType"; //$NON-NLS-1$
    public static final String PROPERTY_IS_PRODUCT_TYPE = "isProductType"; //$NON-NLS-1$
    public static final String PROPERTY_IS_MODEL_TYPE = "isModelType"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)receiver;
        switch (property) {
            case PROPERTY_IS_IPS_OBJECT_TYPE:
                String expectedIpsObjectType = (String)expectedValue;
                return ipsSrcFile.getIpsObjectType().getId().equalsIgnoreCase(expectedIpsObjectType);
            case PROPERTY_IS_TYPE:
                return ipsSrcFile.getIpsObjectType().isEntityType();
            case PROPERTY_IS_PRODUCT_TYPE:
                return ipsSrcFile.getIpsObjectType().isProductDefinitionType();
            case PROPERTY_IS_MODEL_TYPE:
                return !ipsSrcFile.getIpsObjectType().isProductDefinitionType();
            case PROPERTY_IS_META_TYPE:
                IpsObjectType ipsObjectType = ipsSrcFile.getIpsObjectType();
                return ipsObjectType.getId().equals(IpsObjectType.PRODUCT_CMPT_TYPE.getId())
                        || ipsObjectType.getId().equals(IpsObjectType.ENUM_TYPE.getId())
                        || ipsObjectType.getId().equals(IpsObjectType.TABLE_STRUCTURE.getId())
                        || ipsObjectType.getId().equals(IpsObjectType.TEST_CASE_TYPE.getId());
            default:
                return false;
        }
    }
}
