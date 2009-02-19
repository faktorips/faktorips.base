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

import org.eclipse.core.runtime.CoreException;
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
 * <p>
 * Another enum type is provided called difficulty enum type. In the difficulty enum type the enum
 * values are stored directly in the enum type itself.
 * </p>
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractIpsEnumPluginTest extends AbstractIpsPluginTest {

    protected final String STRING_DATATYPE_NAME = "String";
    protected final String INTEGER_DATATYPE_NAME = "Integer";
    protected final String BOOLEAN_DATATYPE_NAME = "Boolean";
    
    protected final String GENDER_ENUM_TYPE_NAME = "GenderEnumType";
    protected final String GENDER_ENUM_ATTRIBUTE_ID_NAME = "Id";
    protected final String GENDER_ENUM_ATTRIBUTE_NAME_NAME = "Name";
    protected final String GENDER_ENUM_CONTENT_NAME = "GenderEnumContent";

    protected final String GENDER_ENUM_LITERAL_MALE_ID = "m";
    protected final String GENDER_ENUM_LITERAL_FEMALE_ID = "w";
    protected final String GENDER_ENUM_LITERAL_MALE_NAME = "male";
    protected final String GENDER_ENUM_LITERAL_FEMALE_NAME = "female";

    protected final String DIFFICULTY_ENUM_TYPE_NAME = "DifficultyEnumType";
    protected final String DIFFICULTY_ENUM_ATTRIBUTE_ID_NAME = "Id";
    protected final String DIFFICULTY_ENUM_ATTRIBUTE_LABEL_NAME = "Label";

    protected final String DIFFICULTY_ENUM_LITERAL_EASY_ID = "e";
    protected final String DIFFICULTY_ENUM_LITERAL_MEDIUM_ID = "m";
    protected final String DIFFICULTY_ENUM_LITERAL_HARD_ID = "h";
    protected final String DIFFICULTY_ENUM_LITERAL_EASY_LABEL = "easy";
    protected final String DIFFICULTY_ENUM_LITERAL_MEDIUM_LABEL = "medium";
    protected final String DIFFICULTY_ENUM_LITERAL_HARD_LABEL = "hard";

    protected IIpsProject ipsProject;

    protected IEnumType genderEnumType;
    protected IEnumAttribute genderEnumAttributeId;
    protected IEnumAttribute genderEnumAttributeName;
    protected IEnumContent genderEnumContent;
    protected IEnumValue genderEnumValueMale;
    protected IEnumValue genderEnumValueFemale;

    protected IEnumType difficultyEnumType;
    protected IEnumAttribute difficultyEnumAttributeId;
    protected IEnumAttribute difficultyEnumAttributeLabel;
    protected IEnumValue difficultyEnumValueEasy;
    protected IEnumValue difficultyEnumValueMedium;
    protected IEnumValue difficultyEnumValueHard;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("EnumTestProject");

        createGenderEnum();
        initGenderEnumValues();

        createDifficultyEnum();
        initDifficultyEnumValues();
    }

    private void createGenderEnum() throws CoreException {
        genderEnumType = newEnumType(ipsProject, GENDER_ENUM_TYPE_NAME);
        genderEnumType.setAbstract(false);
        genderEnumType.setValuesArePartOfModel(false);
        genderEnumType.setSuperEnumType("");

        genderEnumAttributeId = genderEnumType.newEnumAttribute();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
        genderEnumAttributeId.setIdentifier(true);
        genderEnumAttributeName = genderEnumType.newEnumAttribute();
        genderEnumAttributeName.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        genderEnumAttributeName.setDatatype(STRING_DATATYPE_NAME);

        genderEnumContent = newEnumContent(ipsProject, GENDER_ENUM_CONTENT_NAME);
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        genderEnumValueMale = genderEnumContent.newEnumValue();
        genderEnumValueFemale = genderEnumContent.newEnumValue();
    }

    private void initGenderEnumValues() {
        IEnumAttributeValue tempAttributeValueRef;

        tempAttributeValueRef = genderEnumValueMale.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_MALE_ID);
        tempAttributeValueRef = genderEnumValueMale.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_MALE_NAME);

        tempAttributeValueRef = genderEnumValueFemale.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_FEMALE_ID);
        tempAttributeValueRef = genderEnumValueFemale.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_FEMALE_NAME);
    }

    private void createDifficultyEnum() throws CoreException {
        difficultyEnumType = newEnumType(ipsProject, DIFFICULTY_ENUM_TYPE_NAME);
        difficultyEnumType.setAbstract(false);
        difficultyEnumType.setValuesArePartOfModel(true);
        difficultyEnumType.setSuperEnumType("");

        difficultyEnumAttributeId = difficultyEnumType.newEnumAttribute();
        difficultyEnumAttributeId.setName(DIFFICULTY_ENUM_ATTRIBUTE_ID_NAME);
        difficultyEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
        difficultyEnumAttributeId.setIdentifier(true);
        difficultyEnumAttributeLabel = difficultyEnumType.newEnumAttribute();
        difficultyEnumAttributeLabel.setName(DIFFICULTY_ENUM_ATTRIBUTE_LABEL_NAME);
        difficultyEnumAttributeLabel.setDatatype(STRING_DATATYPE_NAME);

        difficultyEnumValueEasy = difficultyEnumType.newEnumValue();
        difficultyEnumValueMedium = difficultyEnumType.newEnumValue();
        difficultyEnumValueHard = difficultyEnumType.newEnumValue();
    }

    private void initDifficultyEnumValues() {
        IEnumAttributeValue tempAttributeValueRef;

        tempAttributeValueRef = difficultyEnumValueEasy.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(DIFFICULTY_ENUM_LITERAL_EASY_ID);
        tempAttributeValueRef = difficultyEnumValueEasy.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(DIFFICULTY_ENUM_LITERAL_EASY_LABEL);

        tempAttributeValueRef = difficultyEnumValueMedium.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(DIFFICULTY_ENUM_LITERAL_MEDIUM_ID);
        tempAttributeValueRef = difficultyEnumValueMedium.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(DIFFICULTY_ENUM_LITERAL_MEDIUM_LABEL);

        tempAttributeValueRef = difficultyEnumValueHard.getEnumAttributeValue(0);
        tempAttributeValueRef.setValue(DIFFICULTY_ENUM_LITERAL_HARD_ID);
        tempAttributeValueRef = difficultyEnumValueHard.getEnumAttributeValue(1);
        tempAttributeValueRef.setValue(DIFFICULTY_ENUM_LITERAL_HARD_LABEL);
    }

    protected Document createXmlDocument(String xmlTag) throws ParserConfigurationException {
        Document xmlDocument = getDocumentBuilder().newDocument();
        xmlDocument.appendChild(xmlDocument.createElement(xmlTag));
        return xmlDocument;
    }
}
