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

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.bf.BFElement;
import org.faktorips.devtools.core.internal.model.bf.BusinessFunction;
import org.faktorips.devtools.core.internal.model.bf.ControlFlow;
import org.faktorips.devtools.core.internal.model.businessfct.BusinessFunctionImpl;
import org.faktorips.devtools.core.internal.model.enums.EnumAttribute;
import org.faktorips.devtools.core.internal.model.enums.EnumAttributeReference;
import org.faktorips.devtools.core.internal.model.enums.EnumAttributeValue;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.enums.EnumValue;
import org.faktorips.devtools.core.internal.model.ipsobject.AbstractIpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
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
import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.Column;
import org.faktorips.devtools.core.internal.model.tablestructure.ColumnRange;
import org.faktorips.devtools.core.internal.model.tablestructure.Key;
import org.faktorips.devtools.core.internal.model.tablestructure.TableAccessFunction;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestAttributeValue;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcase.TestObject;
import org.faktorips.devtools.core.internal.model.testcase.TestPolicyCmpt;
import org.faktorips.devtools.core.internal.model.testcase.TestPolicyCmptLink;
import org.faktorips.devtools.core.internal.model.testcase.TestRule;
import org.faktorips.devtools.core.internal.model.testcase.TestValue;
import org.faktorips.devtools.core.internal.model.testcasetype.TestAttribute;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.internal.model.testcasetype.TestParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Parameter;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;

public class IpsWorkbenchAdapterFactory implements IAdapterFactory {

    private DefaultWorkbenchAdapter defaultWorkbenchAdapter;

    private Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> workbenchAdapterMap;

    public IpsWorkbenchAdapterFactory() {
        defaultWorkbenchAdapter = new DefaultWorkbenchAdapter();
        workbenchAdapterMap = new HashMap<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter>();
        init();
    }

    private void init() {
        initContainers();
        initSrcFiles();
        initIpsObjects();
        initIpsObjectParts();
    }

    private void initContainers() {
        workbenchAdapterMap.put(IpsModel.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(IpsProject.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(IpsPackageFragmentRoot.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ArchiveIpsPackageFragmentRoot.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(AbstractIpsPackageFragment.class, defaultWorkbenchAdapter);
    }

    private void initSrcFiles() {
        workbenchAdapterMap.put(AbstractIpsSrcFile.class, new IpsSrcFileWorkbenchAdapter());
    }

    private void initIpsObjects() {
        workbenchAdapterMap.put(IpsObject.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(BusinessFunction.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(BusinessFunctionImpl.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(EnumContent.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(EnumType.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(PolicyCmptType.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ProductCmptType.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ProductCmpt.class, new ProductCmptWorkbenchAdapter());
        workbenchAdapterMap.put(TableStructure.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TableContents.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestCaseType.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestCase.class, defaultWorkbenchAdapter);
    }

    private void initIpsObjectParts() {
        // Type
        workbenchAdapterMap.put(Method.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(Parameter.class, defaultWorkbenchAdapter);

        // BF
        workbenchAdapterMap.put(BFElement.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ControlFlow.class, defaultWorkbenchAdapter);

        // Enums
        workbenchAdapterMap.put(EnumAttribute.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(EnumAttributeReference.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(EnumAttributeValue.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(EnumValue.class, defaultWorkbenchAdapter);

        // generation
        workbenchAdapterMap.put(IpsObjectGeneration.class, defaultWorkbenchAdapter);

        // PolicyCmptType
        workbenchAdapterMap.put(PolicyCmptTypeAssociation.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(PolicyCmptTypeAttribute.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ValidationRule.class, defaultWorkbenchAdapter);

        // ProductCmptType
        workbenchAdapterMap.put(ProductCmptTypeAssociation.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ProductCmptTypeAttribute.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TableStructureUsage.class, defaultWorkbenchAdapter);

        // ProductCmpt
        workbenchAdapterMap.put(AttributeValue.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ConfigElement.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(Formula.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(FormulaTestCase.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(FormulaTestInputValue.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ProductCmptLink.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TableContentUsage.class, defaultWorkbenchAdapter);

        // TableStructure
        workbenchAdapterMap.put(Column.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(ColumnRange.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(Key.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TableAccessFunction.class, defaultWorkbenchAdapter);

        // TableContents
        workbenchAdapterMap.put(Row.class, defaultWorkbenchAdapter);

        // TestCaseType
        workbenchAdapterMap.put(TestAttribute.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestParameter.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestPolicyCmptTypeParameter.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestRuleParameter.class, defaultWorkbenchAdapter);

        // TestCase
        workbenchAdapterMap.put(TestAttributeValue.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestObject.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestPolicyCmpt.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestPolicyCmptLink.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestRule.class, defaultWorkbenchAdapter);
        workbenchAdapterMap.put(TestValue.class, defaultWorkbenchAdapter);

        // ValueSet
        workbenchAdapterMap.put(ValueSet.class, defaultWorkbenchAdapter);
    }

    @SuppressWarnings("unchecked")
    // AdapterFactory does not use generics
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        return getAdapter(adaptableObject.getClass(), adapterType);
    }

    @SuppressWarnings("unchecked")
    // AdapterFactory does not use generics
    public Object getAdapter(Class adaptableClass, Class adapterType) {
        // first check for direct match, faster
        IpsElementWorkbenchAdapter result = null;
        while (result == null) {
            result = workbenchAdapterMap.get(adaptableClass);
            if (result == null) {
                adaptableClass = adaptableClass.getSuperclass();
                if (adaptableClass == null) {
                    // TODO maybe return null if no adapter was found
                    return null;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    // AdapterFactory does not use generics
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
    }

}
