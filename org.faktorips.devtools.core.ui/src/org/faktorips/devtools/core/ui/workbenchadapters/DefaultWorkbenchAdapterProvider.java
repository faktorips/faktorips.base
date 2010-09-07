/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.bf.BusinessFunction;
import org.faktorips.devtools.core.internal.model.bf.ControlFlow;
import org.faktorips.devtools.core.internal.model.businessfct.BusinessFunctionImpl;
import org.faktorips.devtools.core.internal.model.enums.EnumAttribute;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.ipsobject.AbstractIpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.ipsproject.AbstractIpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.ArchiveIpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAssociation;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.internal.model.productcmpt.FormulaTestCase;
import org.faktorips.devtools.core.internal.model.productcmpt.FormulaTestInputValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptLink;
import org.faktorips.devtools.core.internal.model.productcmpt.TableContentUsage;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.productcmpttype.TableStructureUsage;
import org.faktorips.devtools.core.internal.model.productcmpttype.TableStructureUsage.TableStructureReference;
import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.Column;
import org.faktorips.devtools.core.internal.model.tablestructure.ColumnRange;
import org.faktorips.devtools.core.internal.model.tablestructure.Key;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestAttributeValue;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcase.TestPolicyCmpt;
import org.faktorips.devtools.core.internal.model.testcase.TestPolicyCmptLink;
import org.faktorips.devtools.core.internal.model.testcase.TestRule;
import org.faktorips.devtools.core.internal.model.testcase.TestValue;
import org.faktorips.devtools.core.internal.model.testcasetype.TestAttribute;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestValueParameter;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Parameter;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class DefaultWorkbenchAdapterProvider implements IWorkbenchAdapterProvider {

    // private DefaultWorkbenchAdapter defaultWorkbenchAdapter;

    private Map<Class<? extends IpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public DefaultWorkbenchAdapterProvider() {
        workbenchAdapterMap = new HashMap<Class<? extends IpsElement>, IpsElementWorkbenchAdapter>();
        registerAdapters();
    }

    @Override
    public Map<Class<? extends IpsElement>, IpsElementWorkbenchAdapter> getAdapterMap() {
        return workbenchAdapterMap;
    }

    private void register(Class<? extends IpsElement> adaptableClass, IpsElementWorkbenchAdapter adapter) {
        workbenchAdapterMap.put(adaptableClass, adapter);
    }

    protected void registerAdapters() {
        // defaultWorkbenchAdapter = new DefaultWorkbenchAdapter();
        initContainers();
        initSrcFiles();
        initIpsObjects();
        initIpsObjectParts();
    }

    private void initContainers() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsModel.gif", true); //$NON-NLS-1$
        register(IpsModel.class, new DefaultIpsElementWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsProject.gif", true); //$NON-NLS-1$
        register(IpsProject.class, new DefaultIpsElementWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragmentRoot.gif", true); //$NON-NLS-1$
        register(IpsPackageFragmentRoot.class, new DefaultIpsElementWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
        register(ArchiveIpsPackageFragmentRoot.class, new DefaultIpsElementWorkbenchAdapter(imageDescriptor));
        register(AbstractIpsPackageFragment.class, new IpsPackageFragmentWorkbenchAdapter());
    }

    private void initSrcFiles() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsSrcFile.gif", //$NON-NLS-1$
                true);
        register(AbstractIpsSrcFile.class, new DefaultIpsElementWorkbenchAdapter(imageDescriptor));
    }

    private void initIpsObjects() {
        // register(IpsObject.class, defaultWorkbenchAdapter);
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "BusinessFunction.gif", true); //$NON-NLS-1$
        DefaultIpsObjectWorkbenchAdapter bfWA = new DefaultIpsObjectWorkbenchAdapter(imageDescriptor);
        register(BusinessFunction.class, bfWA);
        register(BusinessFunctionImpl.class, bfWA);
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("EnumContent.gif", true); //$NON-NLS-1$
        register(EnumContent.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("EnumType.gif", true); //$NON-NLS-1$
        register(EnumType.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("PolicyCmptType.gif", true); //$NON-NLS-1$
        register(PolicyCmptType.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ProductCmptType.gif", true); //$NON-NLS-1$
        register(ProductCmptType.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        register(ProductCmpt.class, new ProductCmptWorkbenchAdapter());
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableStructure.gif", true); //$NON-NLS-1$
        register(TableStructure.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableContents.gif", true); //$NON-NLS-1$
        register(TableContents.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TestCaseType.gif", true); //$NON-NLS-1$
        register(TestCaseType.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TestCase.gif", true); //$NON-NLS-1$
        register(TestCase.class, new DefaultIpsObjectWorkbenchAdapter(imageDescriptor));
    }

    private void initIpsObjectParts() {
        // some common workbench adapter

        AttributeWorkbenchAdapter attributeWA = new AttributeWorkbenchAdapter();

        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Formula.gif", true); //$NON-NLS-1$
        DefaultIpsObjectPartWorkbenchAdapter formulaWA = new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor);

        // Type
        register(Method.class, new MethodWorkbenchAdapter());
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Parameter.gif", true); //$NON-NLS-1$
        register(Parameter.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // BF
        // no Image for BFElement yet
        // register(BFElement.class, defaultWorkbenchAdapter);
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("obj16/ControlFlow.gif", true); //$NON-NLS-1$
        register(ControlFlow.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // Enums
        register(EnumAttribute.class, new EnumAttributeWorkbenchAdapter());
        // no Image for yet
        // register(EnumAttributeReference.class, defaultWorkbenchAdapter);
        // no Image for yet
        // register(EnumAttributeValue.class, defaultWorkbenchAdapter);
        // no Image for yet
        // register(EnumValue.class, defaultWorkbenchAdapter);

        // generation
        register(IpsObjectGeneration.class, new IpsObjectGenerationWorkbenchAdapter());

        // PolicyCmptType
        register(PolicyCmptTypeAssociation.class, new AssociationWorkbenchAdapter());
        register(PolicyCmptTypeAttribute.class, attributeWA);
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ValidationRuleDef.gif", true); //$NON-NLS-1$
        register(ValidationRule.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // ProductCmptType
        register(ProductCmptTypeAssociation.class, new AssociationWorkbenchAdapter());
        register(ProductCmptTypeAttribute.class, attributeWA);
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableStructure.gif", true); //$NON-NLS-1$
        IpsElementWorkbenchAdapter tableStructureUsageWA = new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor);
        register(TableStructureUsage.class, tableStructureUsageWA);
        register(TableStructureReference.class, tableStructureUsageWA);

        // ProductCmpt
        register(AttributeValue.class, new AttributeValueWorkbenchAdapter());
        register(ConfigElement.class, attributeWA);
        register(Formula.class, formulaWA);
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ProductCmptLink.gif", true); //$NON-NLS-1$
        register(ProductCmptLink.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableContentsUsage.gif", true); //$NON-NLS-1$
        register(TableContentUsage.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // TableStructure
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableColumn.gif", true); //$NON-NLS-1$
        register(Column.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableRange.gif", true); //$NON-NLS-1$
        register(ColumnRange.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableKey.gif", true); //$NON-NLS-1$
        register(Key.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
        // no Image for TableAccessFuction yet
        // register(TableAccessFunction.class, defaultWorkbenchAdapter);

        // TableContents
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TableRow.gif", true); //$NON-NLS-1$
        register(Row.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // TestCaseType
        register(TestAttribute.class, new TestAttributeWorkbenchAdapter());
        register(TestPolicyCmptTypeParameter.class, new TestPolicyCmptTypeParameterWorkbenchAdapter());
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ValidationRuleDef.gif", true); //$NON-NLS-1$
        register(TestRuleParameter.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Datatype.gif", true); //$NON-NLS-1$
        register(TestValueParameter.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // TestCase
        register(TestAttributeValue.class, new TestAttributeValueWorkbenchAdapter());
        register(TestPolicyCmpt.class, new TestPolicyCmptWorkbenchAdapter());
        register(TestPolicyCmptLink.class, new TestPolicyCmptLinkWorkbenchAdapter());
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ValidationRuleDef.gif", true); //$NON-NLS-1$
        register(TestRule.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TestValue.gif", true); //$NON-NLS-1$
        register(TestValue.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        register(FormulaTestCase.class, formulaWA);
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Parameter.gif", true); //$NON-NLS-1$
        register(FormulaTestInputValue.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));

        // ValueSet
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ValueSet.gif", true); //$NON-NLS-1$
        register(ValueSet.class, new DefaultIpsObjectPartWorkbenchAdapter(imageDescriptor));
    }

}
