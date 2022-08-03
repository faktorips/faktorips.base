/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IImageHandling;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.IIpsElementDecoratorsProvider;
import org.faktorips.devtools.model.decorators.IIpsElementDecoratorsProviders;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.enums.EnumAttribute;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.ipsobject.AbstractIpsSrcFile;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.method.Parameter;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAssociation;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeMethod;
import org.faktorips.devtools.model.internal.pctype.ValidationRule;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredDefault;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.internal.productcmpt.Expression;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.internal.productcmpt.TableContentUsage;
import org.faktorips.devtools.model.internal.productcmpt.ValidationRuleConfig;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.model.internal.productcmpttype.TableStructureUsage;
import org.faktorips.devtools.model.internal.productcmpttype.TableStructureUsage.TableStructureReference;
import org.faktorips.devtools.model.internal.tablecontents.Row;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.Column;
import org.faktorips.devtools.model.internal.tablestructure.ColumnRange;
import org.faktorips.devtools.model.internal.tablestructure.Index;
import org.faktorips.devtools.model.internal.tablestructure.Key;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.internal.testcase.TestAttributeValue;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmpt;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmptLink;
import org.faktorips.devtools.model.internal.testcase.TestRule;
import org.faktorips.devtools.model.internal.testcase.TestValue;
import org.faktorips.devtools.model.internal.testcasetype.TestAttribute;
import org.faktorips.devtools.model.internal.testcasetype.TestCaseType;
import org.faktorips.devtools.model.internal.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.internal.testcasetype.TestRuleParameter;
import org.faktorips.devtools.model.internal.testcasetype.TestValueParameter;
import org.faktorips.devtools.model.internal.valueset.ValueSet;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.extensions.CachingSupplier;

public class IpsDecorators implements IIpsDecorators {

    public static final String POLICY_CMPT_TYPE_IMAGE = "PolicyCmptType.gif"; //$NON-NLS-1$
    public static final String PRODUCT_CMPT_TYPE_IMAGE = "ProductCmptType.gif"; //$NON-NLS-1$

    private static IpsDecorators theInstance = new IpsDecorators();

    private final IImageHandling imageHandling = new ImageHandling(IpsModelDecoratorsPluginActivator.getBundle());
    private final Map<Class<? extends IIpsElement>, IIpsElementDecorator> decorators = new HashMap<>();
    private final Supplier<IIpsElementDecorator> templateDecorator = CachingSupplier
            .caching(ProductCmptDecorator::forTemplates);

    private IpsDecorators() {
        // set this here, because the SimpleIpsElementDecorator will access it in its constructor
        theInstance = this;
        decorators.put(AbstractIpsSrcFile.class, new IpsSrcFileDecorator());
        decorators.put(Column.class, new SimpleIpsElementDecorator("TableColumn.gif")); //$NON-NLS-1$
        decorators.put(ColumnRange.class, new SimpleIpsElementDecorator("TableRange.gif")); //$NON-NLS-1$
        decorators.put(EnumContent.class, new SimpleIpsElementDecorator("EnumContent.gif")); //$NON-NLS-1$
        decorators.put(EnumType.class, new SimpleIpsElementDecorator("EnumType.gif")); //$NON-NLS-1$
        decorators.put(Expression.class, new SimpleIpsElementDecorator("Formula.gif")); //$NON-NLS-1$
        decorators.put(IpsModel.class, new SimpleIpsElementDecorator("IpsModel.gif")); //$NON-NLS-1$
        decorators.put(IpsPackageFragmentRoot.class, new SimpleIpsElementDecorator("IpsPackageFragmentRoot.gif")); //$NON-NLS-1$
        decorators.put(IpsProject.class, new SimpleIpsElementDecorator("IpsProject.gif")); //$NON-NLS-1$
        decorators.put(Parameter.class, new SimpleIpsElementDecorator("Parameter.gif")); //$NON-NLS-1$
        decorators.put(PolicyCmptType.class, new SimpleIpsElementDecorator(POLICY_CMPT_TYPE_IMAGE));
        decorators.put(PolicyCmptTypeMethod.class, new MethodDecorator());
        decorators.put(ProductCmptGeneration.class, new SimpleIpsElementDecorator("ProductCmptGeneration.gif")); //$NON-NLS-1$
        decorators.put(ProductCmptType.class, new SimpleIpsElementDecorator(PRODUCT_CMPT_TYPE_IMAGE));
        decorators.put(ProductCmptTypeMethod.class, new ProductCmptTypeMethodDecorator());
        decorators.put(Row.class, new SimpleIpsElementDecorator("TableRow.gif")); //$NON-NLS-1$
        decorators.put(TableContents.class, new SimpleIpsElementDecorator("TableContents.gif")); //$NON-NLS-1$
        decorators.put(TableContentUsage.class, new SimpleIpsElementDecorator("TableContentsUsage.gif")); //$NON-NLS-1$
        decorators.put(TableStructure.class, new SimpleIpsElementDecorator("TableStructure.gif")); //$NON-NLS-1$
        decorators.put(TableStructureReference.class, new SimpleIpsElementDecorator("TableStructure.gif")); //$NON-NLS-1$
        decorators.put(TableStructureUsage.class, new SimpleIpsElementDecorator("TableStructure.gif")); //$NON-NLS-1$
        decorators.put(TestCase.class, new SimpleIpsElementDecorator("TestCase.gif")); //$NON-NLS-1$
        decorators.put(TestCaseType.class, new SimpleIpsElementDecorator("TestCaseType.gif")); //$NON-NLS-1$
        decorators.put(TestRule.class, new SimpleIpsElementDecorator("ValidationRuleDef.gif")); //$NON-NLS-1$
        decorators.put(TestRuleParameter.class, new SimpleIpsElementDecorator("ValidationRuleDef.gif")); //$NON-NLS-1$
        decorators.put(TestValue.class, new SimpleIpsElementDecorator("TestValue.gif")); //$NON-NLS-1$
        decorators.put(TestValueParameter.class, new SimpleIpsElementDecorator("Datatype.gif")); //$NON-NLS-1$
        decorators.put(ValidationRule.class, new SimpleIpsElementDecorator("ValidationRuleDef.gif")); //$NON-NLS-1$
        decorators.put(ValueSet.class, new SimpleIpsElementDecorator("ValueSet.gif")); //$NON-NLS-1$

        decorators.put(LibraryIpsPackageFragmentRoot.class, new LibraryIpsPackageFragmentRootDecorator());
        decorators.put(AbstractIpsPackageFragment.class, new IpsPackageFragmentDecorator());
        decorators.put(EnumAttribute.class, new EnumAttributeDecorator());
        decorators.put(IpsObjectGeneration.class, new IpsObjectGenerationDecorator());
        decorators.put(PolicyCmptTypeAssociation.class, new AssociationDecorator());
        decorators.put(ProductCmptTypeAssociation.class, new AssociationDecorator());
        AttributeDecorator attributeDecorator = new AttributeDecorator();
        decorators.put(PolicyCmptTypeAttribute.class, attributeDecorator);
        decorators.put(ProductCmptTypeAttribute.class, attributeDecorator);
        decorators.put(AttributeValue.class, new AttributeValueDecorator());
        ConfigElementDecorator configElementDecorator = new ConfigElementDecorator();
        decorators.put(ConfiguredValueSet.class, configElementDecorator);
        decorators.put(ConfiguredDefault.class, configElementDecorator);
        KeyDecorator keyDecorator = new KeyDecorator();
        decorators.put(Index.class, keyDecorator);
        decorators.put(Key.class, keyDecorator);
        decorators.put(TableStructureUsage.class, new TableStructureUsageDecorator());
        decorators.put(TableContentUsage.class, new TableContentUsageDecorator());
        decorators.put(TestAttribute.class, new TestAttributeDecorator());
        decorators.put(TestPolicyCmptTypeParameter.class, new TestPolicyCmptTypeParameterDecorator());
        decorators.put(TestAttributeValue.class, new TestAttributeValueDecorator());
        decorators.put(TestPolicyCmpt.class, new TestPolicyCmptDecorator());
        decorators.put(TestPolicyCmptLink.class, new TestPolicyCmptLinkDecorator());
        decorators.put(ProductCmptLink.class, new ProductCmptLinkDecorator());
        decorators.put(ValidationRule.class, new ValidationRuleDecorator());
        decorators.put(ValidationRuleConfig.class, new ValidationRuleConfigDecorator());
        decorators.put(ValidationRuleConfig.class, new ValidationRuleConfigDecorator());
        decorators.put(ProductCmpt.class, new ProductCmptDecorator());

        for (IIpsElementDecoratorsProvider ipsElementDecoratorsProvider : IIpsElementDecoratorsProviders.get()
                .getIpsElementDecoratorsProviders()) {
            decorators.putAll(ipsElementDecoratorsProvider.getDecoratorsByElementClass());
        }
    }

    /**
     * Returns the {@link IpsDecorators} singleton instance.
     *
     * @see IIpsDecorators#get
     * @deprecated <em>This method should only be called when explicitly depending on implementation
     *                 details, otherwise use {@link IIpsDecorators#get}!</em>
     */
    @Deprecated
    public static final IpsDecorators get() {
        return theInstance;
    }

    @Override
    public IIpsElementDecorator getDecorator(Class<? extends IIpsElement> ipsElementClass) {
        IIpsElementDecorator decorator = decorators.get(ipsElementClass);
        if (decorator != null) {
            return decorator;
        }
        Class<?> superclass = ipsElementClass.getSuperclass();
        if (superclass != null && IIpsElement.class.isAssignableFrom(superclass)) {
            @SuppressWarnings("unchecked")
            Class<? extends IIpsElement> superIpsElementClass = (Class<? extends IIpsElement>)superclass;
            return getDecorator(superIpsElementClass);
        }
        return IIpsElementDecorator.MISSING_ICON_PROVIDER;
    }

    @Override
    public IIpsElementDecorator getDecorator(IpsObjectType ipsObjectType) {
        // special case: PRODUCT_TEMPLATE uses the same implementing class as PRODUCT_CMPT
        if (IpsObjectType.PRODUCT_TEMPLATE.equals(ipsObjectType)) {
            return templateDecorator.get();
        }
        return getDecorator(ipsObjectType.getImplementingClass());
    }

    public static IImageHandling getImageHandling() {
        return theInstance.imageHandling;
    }

    @Override
    public Collection<Class<? extends IIpsElement>> getDecoratedClasses() {
        return Collections.unmodifiableSet(decorators.keySet());
    }

}
