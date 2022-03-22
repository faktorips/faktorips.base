/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class CopyProductCmptOperationTest extends AbstractIpsPluginTest {

    @Mock
    private IProgressMonitor monitor;

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @Test
    public void testRun_CopySrcFileIfCopyMode() throws InvocationTargetException, InterruptedException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute attribute = createProductCmptTypeAttribute(productCmptType, "testAttribute",
                Datatype.STRING, "");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "TestProductCmpt");
        IAttributeValue attributeValue = productCmpt.getLatestProductCmptGeneration().newAttributeValue(attribute);
        attributeValue.setValueHolder(AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue,
                ValueFactory.createStringValue("testValue")));
        IIpsPackageFragment targetIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .createPackageFragment("targetPackage", true, null);

        productCmpt.getIpsSrcFile().save(true, null);

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(targetIpsPackageFragment);
        pmo.setCopyProductCmpt(productCmpt);
        // Template deliberately left undefined

        CopyProductCmptOperation operation = new CopyProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile copiedSrcFile = targetIpsPackageFragment.getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        assertTrue(copiedSrcFile.exists());
    }

    private IProductCmptTypeAttribute createProductCmptTypeAttribute(IProductCmptType productCmptType,
            String name,
            Datatype datatype,
            String defaultValue) {

        IProductCmptTypeAttribute productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute(name);
        productCmptTypeAttribute.setDatatype(datatype.getQualifiedName());
        productCmptTypeAttribute.setDefaultValue(defaultValue);
        return productCmptTypeAttribute;
    }

}
