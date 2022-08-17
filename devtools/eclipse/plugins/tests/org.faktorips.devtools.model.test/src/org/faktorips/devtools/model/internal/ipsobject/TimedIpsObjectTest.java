/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TimedIpsObjectTest extends AbstractIpsPluginTest {

    private ITimedIpsObject timedObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        IIpsProject project = newIpsProject(new ArrayList<Locale>());
        timedObject = newProductCmpt(project, "Product");
        ((ProductCmpt)timedObject).setRuntimeId("abc");
    }

    @Test
    public void testGetValidFrom_FromFirstGeneration() {
        IIpsObjectGeneration gen = timedObject.newGeneration();

        GregorianCalendar newValidFrom = new GregorianCalendar(1000, 1, 1);
        timedObject.setValidFrom(newValidFrom);

        assertEquals(newValidFrom, timedObject.getValidFrom());
        assertEquals(newValidFrom, gen.getValidFrom());
    }

    @Test
    public void testGetChildren() {
        assertEquals(0, timedObject.getChildren().length);
        IIpsObjectGeneration gen = timedObject.newGeneration();
        assertEquals(1, timedObject.getChildren().length);
        assertEquals(gen, timedObject.getChildren()[0]);

        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        assertEquals(2, timedObject.getChildren().length);
        assertEquals(gen, timedObject.getChildren()[0]);
        assertEquals(gen2, timedObject.getChildren()[1]);
    }

    @Test
    public void testChangesOn() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        assertFalse(timedObject.changesOn(date));
        timedObject.newGeneration().setValidFrom(new GregorianCalendar(2004, 0, 1));
        assertFalse(timedObject.changesOn(date));
        timedObject.newGeneration().setValidFrom(new GregorianCalendar(2005, 0, 1));
        assertTrue(timedObject.changesOn(date));
    }

    @Test
    public void testGetGenerations_noGens() {
        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(0, generations.length);
    }

    @Test
    public void testGetGenerations_oneGen() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 1, 1));

        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();

        assertEquals(1, generations.length);
        assertEquals(gen1, generations[0]);
    }

    @Test
    public void testGetGenerations_twoGens() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 1, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 1, 1));

        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();

        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);
    }

    @Test
    public void testGetGenerations_twoGensReverse() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 1, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2003, 1, 1));

        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();

        assertEquals(2, generations.length);
        assertEquals(gen2, generations[0]);
        assertEquals(gen1, generations[1]);
    }

    @Test
    public void testGetGenerations_twoGensFirstNull() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(null);
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2004, 1, 1));

        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();

        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);
    }

    @Test
    public void testGetGenerations_twoGensSecondNull() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 1, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(null);

        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();

        assertEquals(2, generations.length);
        assertEquals(gen2, generations[0]);
        assertEquals(gen1, generations[1]);
    }

    @Test
    public void testGetGenerations_twoGensBothNull() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen1.setValidFrom(null);
        gen2.setValidFrom(null);

        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();

        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);

    }

    @Test
    public void testFindGenerationEffectiveOn() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));

        IIpsObjectGeneration genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2004, 3, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2003, 0, 1));
        assertNull(genFound);

        genFound = timedObject.getGenerationEffectiveOn(null);
        assertNull(genFound);
    }

    @Test
    public void testGetGenerationEffectiveOn() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));

        IIpsObjectGeneration genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2004, 3, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2003, 0, 1));
        assertNull(genFound);

        genFound = timedObject.getGenerationEffectiveOn(null);
        assertNull(genFound);
    }

    @Test
    public void testGetGenerationEffectiveOn_validTo() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));
        timedObject.setValidTo(new GregorianCalendar(2006, 0, 1));

        IIpsObjectGeneration genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2005, 2, 1));
        assertEquals(gen2, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2006, 0, 1));
        assertEquals(gen2, genFound);

        genFound = timedObject.getGenerationEffectiveOn(new GregorianCalendar(2006, 0, 2));
        assertNull(genFound);
    }

    @Test
    public void testGetBestMatchingGenerationEffectiveOn_ReturnFirstGenerationIfDateBeforeFirstGeneration() {
        IIpsObjectGeneration firstGeneration = timedObject.newGeneration(new GregorianCalendar(2012, 0, 1));
        timedObject.newGeneration(new GregorianCalendar(2012, 3, 1));

        GregorianCalendar effectiveDate = new GregorianCalendar(2010, 0, 1);
        assertNull(timedObject.getGenerationEffectiveOn(effectiveDate));
        assertEquals(firstGeneration, timedObject.getBestMatchingGenerationEffectiveOn(effectiveDate));
    }

    @Test
    public void testGetBestMatchingGenerationEffectiveOn_ReturnLatestGenerationIfDateAfterLatestGeneration() {
        timedObject.newGeneration(new GregorianCalendar(2012, 0, 1));
        IIpsObjectGeneration latestGeneration = timedObject.newGeneration(new GregorianCalendar(2012, 3, 1));
        timedObject.setValidTo(new GregorianCalendar(2013, 0, 1));

        GregorianCalendar effectiveDate = new GregorianCalendar(2050, 0, 1);
        assertNull(timedObject.getGenerationEffectiveOn(effectiveDate));
        assertEquals(latestGeneration, timedObject.getBestMatchingGenerationEffectiveOn(effectiveDate));
    }

    @Test
    public void testGetBestMatchingGenerationEffectiveOn_ReturnEffectiveGenerationIfAvailable() {
        timedObject.newGeneration(new GregorianCalendar(2011, 0, 1));
        IIpsObjectGeneration generation = timedObject.newGeneration(new GregorianCalendar(2012, 0, 1));

        GregorianCalendar effectiveDate = new GregorianCalendar(2014, 0, 1);
        assertEquals(generation, timedObject.getGenerationEffectiveOn(effectiveDate));
        assertEquals(generation, timedObject.getBestMatchingGenerationEffectiveOn(effectiveDate));
    }

    @Test
    public void testGetGenerationByEffectiveDate() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));

        IIpsObjectGeneration genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 3, 1));
        assertNull(genFound);

        genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);

        assertNull(timedObject.getGenerationByEffectiveDate(null));

        gen1.setValidFrom(null);
        assertNull(timedObject.getGenerationByEffectiveDate(null));
        assertNull(timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 0, 1)));
    }

    @Test
    public void testNewGeneration() {
        IIpsObjectGeneration gen = timedObject.newGeneration();
        assertEquals(timedObject, gen.getParent());
        assertEquals(1, timedObject.getNumOfGenerations());
        assertEquals(gen, timedObject.getGenerationsOrderedByValidDate()[0]);
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        timedObject.initFromXml(doc.getDocumentElement());
        assertEquals(2, timedObject.getNumOfGenerations());
    }

    @Test
    public void testToXml() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2005, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2006, 0, 1));

        Element element = timedObject.toXml(newDocument());
        gen1.delete();
        gen2.delete();
        timedObject.initFromXml(element);
        assertEquals(2, timedObject.getNumOfGenerations());
    }

    @Test
    public void testRetainOnlyGeneration() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2005, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2006, 0, 1));

        timedObject.retainOnlyGeneration(new GregorianCalendar(2005, 0, 1), new GregorianCalendar(2004, 0, 1));
        assertEquals(1, timedObject.getNumOfGenerations());
        assertEquals(new GregorianCalendar(2004, 0, 1), timedObject.getGeneration(0).getValidFrom());
        assertNull(timedObject.getGeneration(0).getValidTo());
    }

    @Test
    public void testReassignGenerations() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2005, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IIpsObjectGeneration gen3 = timedObject.newGeneration();
        gen3.setValidFrom(new GregorianCalendar(2007, 0, 1));
        GregorianCalendar validToFirst = new GregorianCalendar();
        validToFirst.setTimeInMillis(new GregorianCalendar(2006, 0, 1).getTimeInMillis() - 1);
        GregorianCalendar validToSecond = new GregorianCalendar();
        validToSecond.setTimeInMillis(new GregorianCalendar(2007, 0, 1).getTimeInMillis() - 1);

        timedObject.reassignGenerations(new GregorianCalendar(2005, 5, 1));
        assertEquals(3, timedObject.getNumOfGenerations());
        assertEquals(new GregorianCalendar(2005, 5, 1), timedObject.getGeneration(0).getValidFrom());
        assertEquals(validToFirst, timedObject.getGeneration(0).getValidTo());
        assertEquals(new GregorianCalendar(2006, 0, 1), timedObject.getGeneration(1).getValidFrom());
        assertEquals(validToSecond, timedObject.getGeneration(1).getValidTo());
        assertEquals(new GregorianCalendar(2007, 0, 1), timedObject.getGeneration(2).getValidFrom());
        assertNull(timedObject.getGeneration(2).getValidTo());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The valid from date of the {@link ITimedIpsObject} is stored at the first generation, even if
     * the object does not allow generations. This test case creates this setup and employs an
     * invalid valid from date which has to be validated.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * A validation message must be produced.
     */
    @Test
    public void testValidate_NoGenerationsAllowed_ValidFromDate() {
        // Setup
        IIpsProject ipsProject = newIpsProject();

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "NotChangingProductCmptType");
        productCmptType.setChangingOverTime(false);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "NotChangingProductCmpt");
        productCmpt.setValidFrom(null);

        // Execute
        MessageList messageList = productCmpt.validate(ipsProject);

        // Verify
        Message message = messageList.getMessageByCode(IIpsObjectGeneration.MSGCODE_INVALID_FORMAT_VALID_FROM);
        assertEquals(productCmpt, message.getInvalidObjectProperties().get(0).getObject());
    }

}
