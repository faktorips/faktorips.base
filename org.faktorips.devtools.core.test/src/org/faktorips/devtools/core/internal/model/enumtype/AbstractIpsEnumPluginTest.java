/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enumtype;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;

/**
 * <p>
 * Base test for all enum type tests providing a simple enum model with a gender enum.
 * </p>
 * <p>
 * There is a gender enum type, the values are stored separated from the enum type in a gender enum
 * content object.
 * </p>
 * <p>
 * Utility methods and helpful string constants are provided.
 * </p>
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractIpsEnumPluginTest extends AbstractIpsPluginTest {

    protected final String STRING_DATATYPE_NAME = "String";
    protected final String INTEGER_DATATYPE_NAME = "Integer";

    protected final String GENDER_ENUM_TYPE_NAME = "GenderEnum";
    protected final String GENDER_ENUM_ATTRIBUTE_ID_NAME = "Id";
    protected final String GENDER_ENUM_ATTRIBUTE_NAME_NAME = "Name";
    protected final String GENDER_ENUM_CONTENT_NAME = "GenderEnumContent";

    protected final String ENUM_LITERAL_MALE_ID = "m";
    protected final String ENUM_LITERAL_FEMALE_ID = "w";
    protected final String ENUM_LITERAL_MALE = "male";
    protected final String ENUM_LITERAL_FEMALE = "female";

    protected IIpsProject ipsProject;

    protected IEnumType genderEnumType;
    protected IEnumAttribute genderEnumAttributeId;
    protected IEnumAttribute genderEnumAttributeName;
    protected IEnumContent genderEnumContent;
    protected IEnumValue genderEnumMaleValue;
    protected IEnumValue genderEnumFemaleValue;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("EnumTestProject");

        genderEnumType = newEnumType(ipsProject, GENDER_ENUM_TYPE_NAME);
        genderEnumAttributeId = genderEnumType.newEnumAttribute();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
        genderEnumAttributeId.setIsIdentifier(true);
        genderEnumAttributeName = genderEnumType.newEnumAttribute();
        genderEnumAttributeName.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        genderEnumAttributeName.setDatatype(STRING_DATATYPE_NAME);

        genderEnumContent = newEnumContent(ipsProject, GENDER_ENUM_CONTENT_NAME);
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        genderEnumMaleValue = genderEnumContent.newEnumValue();
        genderEnumFemaleValue = genderEnumContent.newEnumValue();

        initEnumValues();
    }

    private void initEnumValues() {
        IEnumAttributeValue tempAttributeValueRef;

        tempAttributeValueRef = genderEnumMaleValue.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(ENUM_LITERAL_MALE_ID);
        tempAttributeValueRef = genderEnumMaleValue.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(ENUM_LITERAL_MALE);

        tempAttributeValueRef = genderEnumFemaleValue.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(ENUM_LITERAL_FEMALE_ID);
        tempAttributeValueRef = genderEnumFemaleValue.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(ENUM_LITERAL_FEMALE);
    }

    protected Document createXmlDocument(String xmlTag) throws ParserConfigurationException {
        Document xmlDocument = getDocumentBuilder().newDocument();
        xmlDocument.appendChild(xmlDocument.createElement(xmlTag));
        return xmlDocument;
    }
}
