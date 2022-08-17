/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import static org.junit.Assert.assertEquals;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;
import org.junit.Before;

/**
 * Base test for all enumeration tests providing a simple enumeration model with a gender
 * enumeration and a payment mode enumeration.
 * <p>
 * There is a gender <code>IEnumType</code>, the values are stored separated from the
 * <code>IEnumType</code> in a gender <code>IEnumContent</code>.
 * <p>
 * The payment mode <code>IEnumType</code> stores its values directly.
 * <p>
 * Utility methods and helpful string constants are provided.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class AbstractIpsEnumPluginTest extends AbstractIpsPluginTest {

    protected final String GENDER_ENUM_TYPE_NAME = "GenderEnumType";
    protected final String GENDER_ENUM_ATTRIBUTE_ID_NAME = "Id";
    protected final String GENDER_ENUM_ATTRIBUTE_NAME_NAME = "Name";
    protected final String GENDER_ENUM_CONTENT_NAME = "GenderEnumContent";

    protected final String GENDER_ENUM_LITERAL_MALE_ID = "m";
    protected final String GENDER_ENUM_LITERAL_FEMALE_ID = "w";
    protected final String GENDER_ENUM_LITERAL_MALE_NAME = "male";
    protected final String GENDER_ENUM_LITERAL_FEMALE_NAME = "female";

    protected final String ENUMCONTENTS_NAME = "enumcontents.GenderEnumContent";

    protected IIpsProject ipsProject;

    protected IEnumType genderEnumType;
    protected IEnumAttribute genderEnumAttributeId;
    protected IEnumAttribute genderEnumAttributeName;
    protected IEnumContent genderEnumContent;
    protected IEnumValue genderEnumValueMale;
    protected IEnumValue genderEnumValueFemale;

    protected EnumType paymentMode;

    protected ContentsChangeCounter contentsChangeCounter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        contentsChangeCounter = new ContentsChangeCounter();
        getIpsModel().addChangeListener(contentsChangeCounter);
        createGenderEnum();
        initGenderEnumValues();
        createPaymentModeEnum();
    }

    private void createPaymentModeEnum() throws Exception {
        paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        IEnumLiteralNameAttribute literalNameAttribute = paymentMode.newEnumLiteralNameAttribute();
        literalNameAttribute.setDefaultValueProviderAttribute("name");

        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setUnique(true);
        id.setName("id");
        id.setIdentifier(true);
        IEnumAttribute name = paymentMode.newEnumAttribute();
        name.setName("name");
        name.setUsedAsNameInFaktorIpsUi(true);
        name.setUnique(true);
        name.setDatatype(Datatype.STRING.getQualifiedName());

        IEnumValue value1 = paymentMode.newEnumValue();
        value1.setEnumAttributeValue(0, ValueFactory.createStringValue("MONTHLY"));
        value1.setEnumAttributeValue(1, ValueFactory.createStringValue("P1"));
        value1.setEnumAttributeValue(2, ValueFactory.createStringValue("monthly"));
        IEnumValue value2 = paymentMode.newEnumValue();
        value2.setEnumAttributeValue(0, ValueFactory.createStringValue("ANNUALLY"));
        value2.setEnumAttributeValue(1, ValueFactory.createStringValue("P2"));
        value2.setEnumAttributeValue(2, ValueFactory.createStringValue("annually"));
    }

    private void createGenderEnum() {
        genderEnumType = newEnumType(ipsProject, GENDER_ENUM_TYPE_NAME);
        genderEnumType.setAbstract(false);
        genderEnumType.setExtensible(true);
        genderEnumType.setSuperEnumType("");
        genderEnumType.setEnumContentName(ENUMCONTENTS_NAME);

        genderEnumType.newEnumLiteralNameAttribute();

        genderEnumAttributeId = genderEnumType.newEnumAttribute();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());
        genderEnumAttributeId.setUnique(true);
        genderEnumAttributeId.setIdentifier(true);
        genderEnumAttributeName = genderEnumType.newEnumAttribute();
        genderEnumAttributeName.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        genderEnumAttributeName.setDatatype(Datatype.STRING.getQualifiedName());
        genderEnumAttributeName.setUsedAsNameInFaktorIpsUi(true);
        genderEnumAttributeName.setUnique(true);

        genderEnumContent = newEnumContent(ipsProject, ENUMCONTENTS_NAME);
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        genderEnumValueMale = genderEnumContent.newEnumValue();
        genderEnumValueFemale = genderEnumContent.newEnumValue();
    }

    private void initGenderEnumValues() {
        IEnumAttributeValue tempAttributeValueRef;

        tempAttributeValueRef = genderEnumValueMale.getEnumAttributeValues().get(0);
        tempAttributeValueRef.setValue(ValueFactory.createStringValue(GENDER_ENUM_LITERAL_MALE_ID));
        tempAttributeValueRef = genderEnumValueMale.getEnumAttributeValues().get(1);
        tempAttributeValueRef.setValue(ValueFactory.createStringValue(GENDER_ENUM_LITERAL_MALE_NAME));
        tempAttributeValueRef = genderEnumValueFemale.getEnumAttributeValues().get(0);
        tempAttributeValueRef.setValue(ValueFactory.createStringValue(GENDER_ENUM_LITERAL_FEMALE_ID));
        tempAttributeValueRef = genderEnumValueFemale.getEnumAttributeValues().get(1);
        tempAttributeValueRef.setValue(ValueFactory.createStringValue(GENDER_ENUM_LITERAL_FEMALE_NAME));
    }

    protected void assertOneValidationMessage(MessageList validationMessageList) {
        assertEquals(1, validationMessageList.size());
    }

    public class ContentsChangeCounter implements ContentsChangeListener {

        private int counter = 0;

        public int getCounts() {
            return counter;
        }

        public void reset() {
            counter = 0;
        }

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            counter++;
        }
    }
}
