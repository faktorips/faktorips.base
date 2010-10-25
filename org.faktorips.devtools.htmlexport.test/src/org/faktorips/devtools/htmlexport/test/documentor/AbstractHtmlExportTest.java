package org.faktorips.devtools.htmlexport.test.documentor;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;

public abstract class AbstractHtmlExportTest extends AbstractIpsPluginTest {

    protected String zielpfad;
    protected IIpsProject ipsProject;
    protected DocumentorConfiguration config;
    protected HtmlExportOperation operation;

    public AbstractHtmlExportTest() {
        super();
    }

    public AbstractHtmlExportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProjekt");

        config = new DocumentorConfiguration();

        File tmpFile = File.createTempFile("tmp", "tmp");
        String location = tmpFile.getParentFile() + File.separator + "fips";
        tmpFile.delete();

        config.setPath(location + File.separator + "html");
        config.setIpsProject(ipsProject);
        config.setLayouter(new HtmlLayouter(".resource"));
        config.setDescriptionLocale(Locale.GERMANY);

        operation = new HtmlExportOperation(config);
    }

    protected void createStandardProjekt() {
        try {
            newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
            newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
            newPolicyCmptType(ipsProject, "BVB");

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * public void testCreateMassivProjektOhneValidierungsFehler() { createMassivProjekt();
     * 
     * MessageList ml; try { List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
     * ipsProject.findAllIpsSrcFiles(srcFiles);
     * 
     * ml = new MessageList(); for (IIpsSrcFile ipsSrcFile : srcFiles) {
     * ml.add(ipsSrcFile.getIpsObject().validate(ipsProject)); }
     * 
     * assertEquals(ml.toString(), 0, ml.getNoOfMessages(Message.ERROR));
     * 
     * } catch (CoreException e) { e.printStackTrace(); }
     * 
     * }
     */
    protected void createMassivProjekt() {
        try {
            PolicyCmptType vertrag = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
            PolicyCmptType lvb = newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
            PolicyCmptType baseBVB = newPolicyAndProductCmptType(ipsProject, "base.BVB", "base.BVBArt");
            PolicyCmptType versObj = newPolicyAndProductCmptType(ipsProject, "base.versobj.VersObj",
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

            PolicyCmptType baseSubBVB = newPolicyAndProductCmptType(ipsProject, "base.sub.SubBVB", "base.sub.SubBVBArt");
            baseSubBVB.setSupertype(baseBVB.getQualifiedName());

            PolicyCmptType krankenBVB = newPolicyAndProductCmptType(ipsProject, "kranken.KrankenBVB",
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
            rule1.setMessageText("Hallo, Fehler!");
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
            krankenBvbBeitragXyzGeneration.newAttributeValue(text3Attribute, "Ich bin der Text meiner Generation");
            krankenBvbBeitragXyzGeneration.newAttributeValue(zahl2Attribute, "234");
            krankenBvbBeitragXyzGeneration.newAttributeValue(geld1Attribute, "120 EUR");
            krankenBvbBeitragXyzGeneration.newFormula(methodCompZahl3);

            IProductCmpt krankenBvbBeitragZyx = newProductCmpt(krankenBVBArt, "kranken.KrankenBVB-Beitrag-Zyx");
            krankenBvbBeitragZyx.newDescription().setText(("Ich bin die andere BVB für den Beitrag!"));

            IProductCmptGeneration krankenBvbBeitragZyxGeneration = (IProductCmptGeneration)krankenBvbBeitragZyx
                    .newGeneration(cal);
            krankenBvbBeitragZyxGeneration.newAttributeValue(text3Attribute, "Ich bin der Text meiner Generation");
            krankenBvbBeitragZyxGeneration.newAttributeValue(zahl2Attribute, "254");
            krankenBvbBeitragZyxGeneration.newAttributeValue(geld1Attribute, "125 EUR");
            krankenBvbBeitragZyxGeneration.newFormula(methodCompZahl3);

            newPolicyAndProductCmptType(ipsProject, "kranken.sub.KrankenSubBVB", "kranken.sub.KrankenSubBVBArt");

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute policyAttribute,
            String name,
            String datatype,
            Modifier modifier,
            AttributeType attributeType) {
        addAttribute(policyAttribute, name, datatype, modifier);

        policyAttribute.setAttributeType(attributeType);
        policyAttribute.setProductRelevant(name.contains("l"));

    }

    private void addAttribute(IAttribute newAttribute, String name, String datatype, Modifier modifier) {
        newAttribute.setName(name);
        newAttribute.setDatatype(datatype);
        newAttribute.setModifier(modifier);
        newAttribute.setDescription(name + " - " + datatype);
    }

    protected void deletePreviousGeneratedFiles() {
        File file = new File(config.getPath());
        if (file.exists()) {
            file.delete();
        }
    }

}