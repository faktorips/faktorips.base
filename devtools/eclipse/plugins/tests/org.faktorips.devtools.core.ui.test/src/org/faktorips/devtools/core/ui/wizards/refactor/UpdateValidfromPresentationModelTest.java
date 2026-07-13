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

import static org.faktorips.testsupport.IpsMatchers.containsNoErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasWarningMessage;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;

public class UpdateValidfromPresentationModelTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration productCmptGen;
    private IProductCmptTypeAssociation association;
    private IIpsProject ipsProject;
    private ProductCmpt productCmptTarget;

    @Before
    public void setup() {

        ipsProject = this.newIpsProject("TestProject");

        setProjectProperty(ipsProject, properties -> {
            properties.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true));
            properties.setPredefinedDatatypesUsed(new String[] {
                    Datatype.DECIMAL.getName(),
                    Datatype.MONEY.getName(),
                    Datatype.INTEGER.getName(),
                    Datatype.PRIMITIVE_INT.getName(),
                    Datatype.PRIMITIVE_LONG.getName(),
                    Datatype.PRIMITIVE_BOOLEAN.getName(),
                    Datatype.STRING.getName(),
                    Datatype.BOOLEAN.getName(),
                    LocalDateDatatype.DATATYPE.getName(),
                    LocalDateDatatype.GREGORIAN_CALENDAR.getName(),
                    LocalDateTimeDatatype.DATATYPE.getName()
            });
        });

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestPolicyType");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setChangingOverTime(true);

        IPolicyCmptType policyCmptTypeTarget = newPolicyAndProductCmptType(ipsProject, "TestTarget", "TestTargetType");
        IProductCmptType productCmptTypeTarget = policyCmptTypeTarget.findProductCmptType(ipsProject);
        productCmptTypeTarget.setChangingOverTime(true);

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTargetRoleSingular("TestRelation");
        association.setTarget(productCmptTypeTarget.getQualifiedName());

        productCmpt = newProductCmpt(productCmptType, "products.TestProduct 2025-01");
        productCmpt.setValidFrom(new GregorianCalendar(2025, 0, 1));

        productCmptGen = productCmpt.getProductCmptGeneration(0);

        productCmptTarget = newProductCmpt(productCmptTypeTarget, "products.TestProductTarget 2025-01");

        productCmptTarget.setValidFrom(new GregorianCalendar(2025, 0, 1));

        IProductCmptLink link = productCmptGen.newLink(association.getName());
        link.setTarget(productCmptTarget.getQualifiedName());
    }

    @Test
    public void testValidate_NoNewValidFrom() {
        var presentationModel = new UpdateValidfromPresentationModel(productCmpt);
        presentationModel.setNewVersionId("2026-01");

        var messageList = presentationModel.validate();

        assertThat(messageList, hasErrorMessage(UpdateValidfromPresentationModel.MSG_CODE_EMPTY_NEW_VALID_FROM));
    }

    @Test
    public void testValidate_NoNewVersionId() {
        var presentationModel = new UpdateValidfromPresentationModel(productCmpt);
        presentationModel.setNewValidFrom(new GregorianCalendar(2026, 0, 1));

        var messageList = presentationModel.validate();

        assertThat(messageList, hasErrorMessage(UpdateValidfromPresentationModel.MSG_CODE_EMPTY_NEW_VERSION_ID));
    }

    @Test
    public void testValidate_MovingOnlyGeneration() {
        var presentationModel = new UpdateValidfromPresentationModel(productCmpt);
        presentationModel.setNewValidFrom(new GregorianCalendar(2026, 0, 1));
        presentationModel.setNewVersionId("2026-01");

        var messageList = presentationModel.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidate_MovingFirstGenerationPastSecond() {
        productCmpt.newGeneration(new GregorianCalendar(2026, 0, 1));
        var presentationModel = new UpdateValidfromPresentationModel(productCmpt);
        presentationModel.setNewValidFrom(new GregorianCalendar(2027, 0, 1));
        presentationModel.setNewVersionId("2027-01");

        var messageList = presentationModel.validate();

        assertThat(messageList,
                hasWarningMessage(UpdateValidfromPresentationModel.MSG_CODE_VALID_FROM_MOVED_PAST_NEXT_GENERATION));
    }

}
