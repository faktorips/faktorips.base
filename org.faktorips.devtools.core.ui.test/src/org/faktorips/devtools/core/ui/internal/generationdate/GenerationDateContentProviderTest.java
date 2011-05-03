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

package org.faktorips.devtools.core.ui.internal.generationdate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;

public class GenerationDateContentProviderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ProductCmpt rootCmpt;
    private ProductCmpt subCmpt1;
    private ProductCmpt subSubCmpt;
    private ProductCmpt subCmpt2;
    private GregorianCalendar[] validFroms;
    private MyTestContentProvider contentProvider;
    private ProductCmptType type;
    private IProductCmptTypeAssociation association;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        contentProvider = new MyTestContentProvider();

        project = newIpsProject();
        type = newProductCmptType(project, "Type");
        association = type.newProductCmptTypeAssociation();
        association.setTargetRoleSingular("asso");
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(type.getQualifiedName());

        rootCmpt = newProductCmpt(type, "RootCmpt");

        subCmpt1 = newProductCmpt(type, "SubCmpt1");
        subCmpt2 = newProductCmpt(type, "SubCmpt2");
        subSubCmpt = newProductCmpt(type, "SubSubCmpt");

        validFroms = new GregorianCalendar[7];
        GregorianCalendar validFrom = new GregorianCalendar();
        validFrom.set(0, 0, 1); // 0
        validFroms[0] = validFrom;
        validFrom = new GregorianCalendar();
        validFrom.set(1, 0, 1); // 1
        validFroms[1] = validFrom;
        validFrom = new GregorianCalendar();
        validFrom.set(2, 0, 1);// 2
        validFroms[2] = validFrom;
        validFrom = new GregorianCalendar();
        validFrom.set(3, 0, 1);// 3
        validFroms[3] = validFrom;
        validFrom = new GregorianCalendar();
        validFrom.set(4, 0, 1);// 4
        validFroms[4] = validFrom;
        validFrom = new GregorianCalendar();
        validFrom.set(5, 0, 1);// 5
        validFroms[5] = validFrom;
        validFrom = new GregorianCalendar();
        validFrom.set(6, 0, 1);// 6
        validFroms[6] = validFrom;

        rootCmpt.getGeneration(0).delete();
        rootCmpt.newGeneration(validFroms[1]); // 1 - 3
        rootCmpt.newGeneration(validFroms[3]); // 3 - 5
        rootCmpt.newGeneration(validFroms[5]); // 5 - null

        subCmpt1.getGeneration(0).delete();
        subCmpt1.newGeneration(validFroms[0]); // 0 - 2
        subCmpt1.newGeneration(validFroms[2]); // 2 - 4
        subCmpt1.newGeneration(validFroms[4]); // 4 - null

        subCmpt2.getGeneration(0).delete();
        subCmpt2.newGeneration(validFroms[1]); // 1 - 2
        subCmpt2.newGeneration(validFroms[2]); // 2 - 4
        subCmpt2.newGeneration(validFroms[4]); // 4 - null

        subSubCmpt.getGeneration(0).delete();
        subSubCmpt.newGeneration(validFroms[1]); // 1 - 4
        subSubCmpt.newGeneration(validFroms[4]); // 4 - 6
        subSubCmpt.newGeneration(validFroms[6]); // 6 - null

    }

    @Test
    public void testCollectElements() {
        checkAdjustmentDates(5, 3, 1);

        IProductCmptLink link1 = rootCmpt.getProductCmptGeneration(0).newLink(association);
        link1.setTarget(subCmpt1.getQualifiedName());
        // link only exists in first generation of rootCmpt
        checkAdjustmentDates(5, 3, 2, 1);

        IProductCmptLink link2 = rootCmpt.getProductCmptGeneration(1).newLink(association);
        link2.setTarget(subCmpt1.getQualifiedName());
        // link exists in generation 1 and 2 of rootCmpt
        checkAdjustmentDates(5, 4, 3, 2, 1);

        IProductCmptLink link3 = rootCmpt.getProductCmptGeneration(2).newLink(association);
        link3.setTarget(subCmpt1.getQualifiedName());
        // link exists in generation 1, 2 and 3 of rootCmpt
        checkAdjustmentDates(5, 4, 3, 2, 1);

        link2.delete();
        // link exists in generation 1 and 3 of rootCmpt
        checkAdjustmentDates(5, 3, 2, 1);

        IProductCmptLink link4 = rootCmpt.getProductCmptGeneration(1).newLink(association);
        link4.setTarget(subCmpt2.getQualifiedName());
        checkAdjustmentDates(5, 4, 3, 2, 1);

        IProductCmptLink link5 = subCmpt2.getProductCmptGeneration(0).newLink(association);
        link5.setTarget(subSubCmpt.getQualifiedName());
        checkAdjustmentDates(5, 4, 3, 2, 1);

        link4.delete();
        link4 = rootCmpt.getProductCmptGeneration(2).newLink(association);
        link4.setTarget(subCmpt2.getQualifiedName());
        checkAdjustmentDates(5, 3, 2, 1);

        IProductCmptLink link6 = subCmpt2.getProductCmptGeneration(2).newLink(association);
        link6.setTarget(subSubCmpt.getQualifiedName());
        checkAdjustmentDates(6, 5, 3, 2, 1);

    }

    private void checkAdjustmentDates(int... expectedDateIndices) {
        Object[] result = contentProvider.collectElements(rootCmpt, new NullProgressMonitor());
        assertResult(result, expectedDateIndices);
    }

    private void assertResult(Object[] result, int... expecteds) {
        assertEquals(expecteds.length, result.length);
        for (int i = 0; i < expecteds.length; i++) {
            GenerationDate adjDate = (GenerationDate)result[i];
            assertEquals(validFroms[expecteds[i]].getTimeInMillis(), adjDate.getValidFrom().getTimeInMillis());
            if (i > 0) {
                GregorianCalendar expectedValidTo = validFroms[expecteds[i - 1]];
                // validTo-Dates are one millisecond before next day to avoid overlapping
                expectedValidTo.add(Calendar.MILLISECOND, -1);
                assertEquals(expectedValidTo.getTimeInMillis(), adjDate.getValidTo().getTimeInMillis());
            } else {
                assertNull(adjDate.getValidTo());
            }
        }
    }

    // need to test protected method
    private static class MyTestContentProvider extends GenerationDateContentProvider {

        @Override
        public Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
            return super.collectElements(inputElement, monitor);
        }
    }
}
