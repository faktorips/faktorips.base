/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.MessageSeverity;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.values.LocalizedString;
import org.junit.Before;

public abstract class AbstractHtmlExportPluginTest extends AbstractIpsPluginTest {

    protected String zielpfad;
    protected IIpsProject ipsProject;
    protected DocumentationContext context;
    protected HtmlExportOperation operation;

    public AbstractHtmlExportPluginTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProjekt");

        context = new DocumentationContext();
        initContext();

        operation = new HtmlExportOperation(context);
    }

    public DocumentationContext getContext() {
        return context;
    }

    protected void initContext() throws IOException {
        File tmpFile = File.createTempFile("tmp", "tmp");
        String location = tmpFile.getParentFile() + File.separator + "fips";
        tmpFile.delete();

        context.setPath(location + File.separator + "html");
        context.setIpsProject(ipsProject);
        context.setLayouter(new HtmlLayouter(context, ".resource"));
        context.setDocumentationLocale(Locale.GERMANY);
    }

    protected void createStandardProjekt() {
        newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
        newPolicyCmptType(ipsProject, "BVB");
    }

    protected void createMassivProjekt() {
        IPolicyCmptType vertrag = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        IPolicyCmptType lvb = newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
        IPolicyCmptType baseBVB = newPolicyAndProductCmptType(ipsProject, "base.BVB", "base.BVBArt");
        IPolicyCmptType versObj = newPolicyAndProductCmptType(ipsProject, "base.versobj.VersObj",
                "base.versobj.VersObjArt");

        IPolicyCmptTypeAssociation assoLvbBvb = lvb.newPolicyCmptTypeAssociation();
        assoLvbBvb.setTarget(baseBVB.getQualifiedName());
        assoLvbBvb.setQualified(true);
        assoLvbBvb.setMinCardinality(2);
        assoLvbBvb.setMaxCardinality(5);
        assoLvbBvb.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assoLvbBvb.setTargetRolePlural("BVBs");
        assoLvbBvb.setTargetRoleSingular("BVB");

        IPolicyCmptTypeAssociation assoBvbLvb = assoLvbBvb.newInverseAssociation();
        assoBvbLvb.setTargetRoleSingular("LVB");
        assoBvbLvb.setTargetRolePlural("LVBs");

        IPolicyCmptTypeAssociation assoLvbVersObj = lvb.newPolicyCmptTypeAssociation();
        assoLvbVersObj.setTarget(versObj.getQualifiedName());
        assoLvbVersObj.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assoLvbVersObj.newDescription().setText("Das übliche Blabla");
        assoLvbVersObj.setTargetRoleSingular("Versichertes Objekt");

        IPolicyCmptTypeAssociation assoVersObjLvb = assoLvbVersObj.newInverseAssociation();
        assoVersObjLvb.setTargetRoleSingular("LVB");
        assoVersObjLvb.setTargetRolePlural("LVBs");

        IPolicyCmptTypeAssociation assoVertragLvb = vertrag.newPolicyCmptTypeAssociation();
        assoVertragLvb.setTarget(lvb.getQualifiedName());
        assoVertragLvb.setTargetRoleSingular("LVB");
        assoVertragLvb.setTargetRolePlural("LVBs");

        IPolicyCmptTypeAssociation assoLvbVertrag = assoVertragLvb.newInverseAssociation();
        assoLvbVertrag.setTargetRoleSingular("Vertrag");
        assoLvbVertrag.setTargetRolePlural("Verträge");

        IPolicyCmptType baseSubBVB = newPolicyAndProductCmptType(ipsProject, "base.sub.SubBVB",
                "base.sub.SubBVBArt");
        baseSubBVB.setSupertype(baseBVB.getQualifiedName());

        IPolicyCmptType krankenBVB = newPolicyAndProductCmptType(ipsProject, "kranken.KrankenBVB",
                "kranken.KrankenBVBArt");
        krankenBVB.setSupertype(baseSubBVB.getQualifiedName());

        krankenBVB.newDescription().setText(
                "blablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");
        // krankenBVB.setAbstract(true);

        addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Text1", "String", Modifier.PUBLISHED,
                AttributeType.CHANGEABLE);
        addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Geld2", "Money", Modifier.PUBLIC,
                AttributeType.CONSTANT);
        addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Zahl3", "Integer", Modifier.PUBLIC,
                AttributeType.DERIVED_ON_THE_FLY);
        IPolicyCmptTypeAttribute attributeZahl3 = krankenBVB.getPolicyCmptTypeAttribute("Zahl3");

        IValidationRule rule1 = krankenBVB.newRule();
        rule1.setName("Regel1");
        rule1.newDescription().setText("Beschreibung der Regel");
        rule1.setMessageCode("xyz");
        rule1.setMessageSeverity(MessageSeverity.WARNING);

        String msgText = "Hallo, Fehler!";
        rule1.getMessageText().add(new LocalizedString(context.getDocumentationLocale(), msgText));
        rule1.addValidatedAttribute("Text1");
        rule1.addValidatedAttribute("Geld2");

        IProductCmptTypeMethod methodCompZahl3 = ipsProject.findProductCmptType(krankenBVB.getProductCmptType())
                .newProductCmptTypeMethod();
        String methodNameZahl3 = "berechneZahl3";
        methodCompZahl3.setName(methodNameZahl3);
        methodCompZahl3.setDatatype("Integer");
        methodCompZahl3.setFormulaSignatureDefinition(true);
        methodCompZahl3.setFormulaName("Formelname");
        methodCompZahl3.newDescription().setText("Die Methode berechneZahl3 ...");
        attributeZahl3.setComputationMethodSignature(methodNameZahl3 + "()");

        IProductCmptTypeMethod methodCompXyz = ipsProject.findProductCmptType(krankenBVB.getProductCmptType())
                .newProductCmptTypeMethod();
        String methodNameXyz = "berechneXyz";
        methodCompXyz.setName(methodNameXyz);
        methodCompXyz.setDatatype("String");
        methodCompXyz.setFormulaSignatureDefinition(false);
        methodCompXyz.newDescription().setText("Die Methode berechneXyz ...");
        methodCompXyz.newParameter("Integer", "zahl");
        methodCompXyz.newParameter("String", "name");

        IProductCmptType krankenBVBArt = (ProductCmptType)ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE,
                krankenBVB.getProductCmptType());
        krankenBVBArt.setPolicyCmptType(krankenBVB.getQualifiedName());
        krankenBVBArt.setSupertype(baseSubBVB.getProductCmptType());

        IProductCmptTypeAttribute geld1Attribute = krankenBVBArt.newProductCmptTypeAttribute();
        addAttribute(geld1Attribute, "Geld1", "Money", Modifier.PUBLIC);
        IProductCmptTypeAttribute zahl2Attribute = krankenBVBArt.newProductCmptTypeAttribute();
        addAttribute(zahl2Attribute, "Zahl2", "Integer", Modifier.PUBLISHED);
        IProductCmptTypeAttribute text3Attribute = krankenBVBArt.newProductCmptTypeAttribute();
        addAttribute(text3Attribute, "Text3", "String", Modifier.PUBLIC);
        krankenBVBArt.newDescription().setText(
                "Produkt blabla\nblablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");

        IProductCmpt krankenBvbBeitragXyz = newProductCmpt(krankenBVBArt, "kranken.KrankenBVB-Beitrag-Xyz");
        krankenBvbBeitragXyz.newDescription().setText("Ich bin also die BVB für den Beitrag!");

        GregorianCalendar cal = new GregorianCalendar(2010, 0, 1);
        IProductCmptGeneration krankenBvbBeitragXyzGeneration = (IProductCmptGeneration)krankenBvbBeitragXyz
                .newGeneration(cal);
        IAttributeValue newAttributeValue = krankenBvbBeitragXyzGeneration.newAttributeValue(text3Attribute);
        newAttributeValue.setValueHolder(new SingleValueHolder(newAttributeValue,
                "Ich bin der Text meiner Generation"));
        IAttributeValue newAttributeValue2 = krankenBvbBeitragXyzGeneration.newAttributeValue(zahl2Attribute);
        newAttributeValue2.setValueHolder(new SingleValueHolder(newAttributeValue2, "234"));
        IAttributeValue newAttributeValue3 = krankenBvbBeitragXyzGeneration.newAttributeValue(geld1Attribute);
        newAttributeValue3.setValueHolder(new SingleValueHolder(newAttributeValue3, "120 EUR"));
        krankenBvbBeitragXyzGeneration.newFormula(methodCompZahl3);

        IProductCmpt krankenBvbBeitragZyx = newProductCmpt(krankenBVBArt, "kranken.KrankenBVB-Beitrag-Zyx");
        krankenBvbBeitragZyx.newDescription().setText(("Ich bin die andere BVB für den Beitrag!"));

        IProductCmptGeneration krankenBvbBeitragZyxGeneration = (IProductCmptGeneration)krankenBvbBeitragZyx
                .newGeneration(cal);
        IAttributeValue newAttributeValue4 = krankenBvbBeitragZyxGeneration.newAttributeValue(text3Attribute);
        newAttributeValue4.setValueHolder(new SingleValueHolder(newAttributeValue4,
                "Ich bin der Text meiner Generation"));
        IAttributeValue newAttributeValue5 = krankenBvbBeitragZyxGeneration.newAttributeValue(zahl2Attribute);
        newAttributeValue5.setValueHolder(new SingleValueHolder(newAttributeValue5, "254"));
        IAttributeValue newAttributeValue6 = krankenBvbBeitragZyxGeneration.newAttributeValue(geld1Attribute);
        newAttributeValue6.setValueHolder(new SingleValueHolder(newAttributeValue6, "125 EUR"));
        krankenBvbBeitragZyxGeneration.newFormula(methodCompZahl3);

        newPolicyAndProductCmptType(ipsProject, "kranken.sub.KrankenSubBVB", "kranken.sub.KrankenSubBVBArt");
    }

    private void addPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute policyAttribute,
            String name,
            String datatype,
            Modifier modifier,
            AttributeType attributeType) {
        addAttribute(policyAttribute, name, datatype, modifier);

        policyAttribute.setAttributeType(attributeType);
        policyAttribute.setValueSetConfiguredByProduct(name.contains("l"));

    }

    private void addAttribute(IAttribute newAttribute, String name, String datatype, Modifier modifier) {
        newAttribute.setName(name);
        newAttribute.setDatatype(datatype);
        newAttribute.setModifier(modifier);
        newAttribute.newDescription().setText(name + " - " + datatype);
    }

    protected void deletePreviousGeneratedFiles() {
        File file = new File(context.getPath());
        if (file.exists()) {
            file.delete();
        }
    }
}