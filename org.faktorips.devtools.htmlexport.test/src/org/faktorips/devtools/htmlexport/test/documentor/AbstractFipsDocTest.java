package org.faktorips.devtools.htmlexport.test.documentor;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.Documentor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.helper.html.HtmlLayouter;

public abstract class AbstractFipsDocTest extends AbstractIpsPluginTest {

	protected static final String FIPSDOC_GENERIERT_HOME = "/home/dicker/fipsdoc/generiert";
	protected IIpsProject ipsProject;
	protected DocumentorConfiguration documentorConfig;
	protected Documentor documentor;

	public AbstractFipsDocTest() {
		super();
	}

	public AbstractFipsDocTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ipsProject = newIpsProject("TestProjekt");

		documentorConfig = new DocumentorConfiguration();
		documentorConfig.setPath(FIPSDOC_GENERIERT_HOME);
		documentorConfig.setIpsProject(ipsProject);
		documentorConfig.setLayouter(new HtmlLayouter());

		documentor = new Documentor(documentorConfig);
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

	protected void createMassivProjekt() {
		try {
			newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
			newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
			PolicyCmptType baseBVB = newPolicyCmptType(ipsProject, "base.BVB");
			PolicyCmptType baseSubBVB = newPolicyCmptType(ipsProject, "base.sub.SubBVB");
			baseSubBVB.setSupertype(baseBVB.getQualifiedName());

			PolicyCmptType krankenBVB = newPolicyCmptType(ipsProject, "kranken.KrankenBVB");
			krankenBVB.setSupertype(baseSubBVB.getQualifiedName());
			
			krankenBVB.setDescription("blablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");

			addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Text  1", "String",
					Modifier.PUBLISHED, AttributeType.CHANGEABLE);
			addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Geld  2", "Money", Modifier.PUBLIC,
					AttributeType.CONSTANT);
			addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Zahl  3", "Integer", Modifier.PUBLIC,
					AttributeType.DERIVED_ON_THE_FLY);

			ProductCmptType newProductCmp = (ProductCmptType) ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE,
					krankenBVB.getProductCmptType());
			addAttribute(newProductCmp.newAttribute(), "Geld 1", "Money", Modifier.PUBLIC);
			addAttribute(newProductCmp.newAttribute(), "Zahl 2", "Integer", Modifier.PUBLISHED);
			addAttribute(newProductCmp.newAttribute(), "Text 3", "Money", Modifier.PUBLIC);
			newProductCmp
					.setDescription("Produkt blabla\nblablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");

			newPolicyCmptType(ipsProject, "kranken.sub.KrankenSubBVB");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void addPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute policyAttribute, String name, String datatype,
			Modifier modifier, AttributeType attributeType) {
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
		File file = new File(documentorConfig.getPath());
		if (file.exists()) {
			file.delete();
		}
	}

}