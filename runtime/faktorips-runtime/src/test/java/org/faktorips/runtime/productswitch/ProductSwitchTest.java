/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productswitch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentLink;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.PolicyAssociation;
import org.faktorips.runtime.model.type.ProductCmptType;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.runtime.productswitch.ProductSwitchResults.FailedProductSwitch;
import org.faktorips.runtime.productswitch.ProductSwitchResults.ProductSwitchResult;
import org.faktorips.runtime.productswitch.ProductSwitchResults.SuccessfulProductSwitch;
import org.faktorips.runtime.util.ProductComponentLinks;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

public class ProductSwitchTest {

    @Test
    public void testAdvancedSwitch() {

        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "b 2", "b", "2");
        repository.putProductComponent(childProduct2);
        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        TestGrandChildProduct grandchildProduct1 = new TestGrandChildProduct(repository, "d 1", "d", "1");
        repository.putProductComponent(grandchildProduct1);
        TestGrandChildProduct grandchildProduct2 = new TestGrandChildProduct(repository, "d 2", "d", "2");
        repository.putProductComponent(grandchildProduct2);
        childProduct1.addTestGrandChildProduct(grandchildProduct1);
        childProduct2.addTestGrandChildProduct(grandchildProduct2);
        TestChild achild = childProduct1.createPolicyComponent();
        root.addTestChild(achild);
        TestGrandChild grandChild = grandchildProduct1.createPolicyComponent();
        achild.addTestGrandChild(grandChild);

        PolicyAssociation childAsso = IpsModel.getPolicyCmptType(root).getAssociation("TestChild");
        AdvancedProductFinder defaultFinder = (parent, oldParentProdCmpt, child, parentToChild) -> ProductFinderResult
                .empty("so empty");

        ProductSwitchResults results = ProductSwitch.from(root)
                .switchAt(childAsso, AdvancedProductFinder.BY_KIND_ID)
                .switchOthers(defaultFinder)
                .to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = results.asMap();
        assertThat(results.size(), is(3));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(asMap.get(achild), isSuccessfulSwitch(childProduct1, childProduct2));
        assertThat(results.failureAsMap().get(grandChild), isFailedSwitch());
    }

    @Test
    public void testTo_SwitchesRootsProduct() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();

        ProductSwitchResults switchResult = ProductSwitch.from(root).to(rootProduct2);

        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(switchResult.getResultFor(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
    }

    @Test
    public void testTo_SwitchesSingleChild() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProduct2);
        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        TestChild child = childProduct1.createPolicyComponent();
        root.addTestChild(child);

        ProductSwitchResults switchResult = ProductSwitch.from(root).to(rootProduct2);

        Map<IConfigurableModelObject, SuccessfulProductSwitch> asSuccessfulMap = switchResult.successfulAsMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asSuccessfulMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProduct2));
        assertThat(asSuccessfulMap.get(child), isSuccessfulSwitch(childProduct1, childProduct2));
    }

    @Test
    public void testTo_SwitchesWithAttributeName() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProduct2);
        TestChildProduct childProduct3 = new TestChildProduct(repository, "d 3", "d", "3");
        repository.putProductComponent(childProduct3);

        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        rootProduct2.addTestChildProduct(childProduct3);

        TestChild child = childProduct1.createPolicyComponent();
        root.addTestChild(child);
        rootProduct1.setSwitchType("root");
        rootProduct2.setSwitchType("root");
        childProduct1.setSwitchType("switchChild");
        childProduct2.setSwitchType("c2");
        childProduct3.setSwitchType("switchChild");

        ProductSwitchResults switchResult = ProductSwitch.from(root)
                .matchingBy("SwitchType").to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProduct3));
        assertThat(asMap.get(child), isSuccessfulSwitch(childProduct1, childProduct3));
    }

    @Test
    public void testTo_SwitchesWithPredicate() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();

        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProduct2);
        TestChildProduct childProduct3 = new TestChildProduct(repository, "d 3", "d", "3");
        repository.putProductComponent(childProduct3);

        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        rootProduct2.addTestChildProduct(childProduct3);

        TestChild child = childProduct1.createPolicyComponent();
        root.addTestChild(child);
        rootProduct1.setSwitchType("root");
        rootProduct2.setSwitchType("root");
        childProduct1.setSwitchType("switchChild");
        childProduct2.setSwitchType("c2");
        childProduct3.setSwitchType("switchChild");

        ProductSwitchResults switchResult = ProductSwitch.from(root)
                .with((oldP, newP) -> {
                    if (oldP instanceof TestRootProduct) {
                        return ((TestRootProduct)newP).getSwitchType().equals(((TestRootProduct)oldP).getSwitchType());
                    }
                    if (oldP instanceof TestChildProduct) {
                        return ((TestChildProduct)newP).getSwitchType()
                                .equals(((TestChildProduct)oldP).getSwitchType());
                    }
                    return false;
                }).to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProduct3));
        assertThat(asMap.get(child), isSuccessfulSwitch(childProduct1, childProduct3));
    }

    @Test
    public void testTo_SwitchesWithProductMatcher() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProduct2);
        TestChildProduct childProduct3 = new TestChildProduct(repository, "b 3", "b", "3");
        repository.putProductComponent(childProduct3);

        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        rootProduct2.addTestChildProduct(childProduct3);

        TestChild child = childProduct1.createPolicyComponent();
        root.addTestChild(child);
        rootProduct1.setSwitchType("root");
        rootProduct2.setSwitchType("root");
        childProduct1.setSwitchType("switchChild");
        childProduct2.setSwitchType("c2");
        childProduct3.setSwitchType("switchChild");

        ProductSwitchResults switchResult = ProductSwitch.from(root)
                .with((MatchingProductFinder)(modelObject, oldProducts, newProducts) -> {

                    if (oldProducts.size() == 1 && newProducts.size() == 1) {
                        return ProductFinderResult.of(newProducts.get(0));
                    }

                    List<IProductComponent> matchingKindId = newProducts.stream()
                            .filter(newChildProduct -> ProductSwitch.BY_KIND_ID.test(
                                    modelObject.getProductComponent(),
                                    newChildProduct))
                            .collect(Collectors.toList());

                    if (matchingKindId.size() == 1) {
                        return ProductFinderResult.of(matchingKindId.get(0));
                    }
                    return ProductFinderResult.error("an error");

                }).to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProduct3));
        assertThat(asMap.get(child), isSuccessfulSwitch(childProduct1, childProduct3));
    }

    @Test
    public void testTo_DoesNotSwitchChildIfProductDoesNotMatch() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProductA1 = new TestChildProduct(repository, "a 1", "a", "1");
        repository.putProductComponent(childProductA1);
        TestChildProduct childProductB1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProductB1);
        TestChildProduct childProductC2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProductC2);
        rootProduct1.addTestChildProduct(childProductA1);
        rootProduct1.addTestChildProduct(childProductB1);
        rootProduct2.addTestChildProduct(childProductC2);
        TestChild child = childProductA1.createPolicyComponent();
        root.addTestChild(child);

        ProductSwitchResults switchResult = ProductSwitch.from(root).to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProductA1));
        assertThat(asMap.get(child), isFailedSwitch());
        assertThat(switchResult.containsFailures(), is(true));
    }

    @Test
    public void testTo_SwitchesChildWithDifferentVersion() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProductA1 = new TestChildProduct(repository, "a 1", "a", "1");
        repository.putProductComponent(childProductA1);
        TestChildProduct childProductB1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProductB1);
        TestChildProduct childProductA2 = new TestChildProduct(repository, "a 2", "a", "2");
        repository.putProductComponent(childProductA2);
        rootProduct1.addTestChildProduct(childProductA1);
        rootProduct1.addTestChildProduct(childProductB1);
        rootProduct2.addTestChildProduct(childProductA2);
        TestChild child = childProductA1.createPolicyComponent();
        root.addTestChild(child);

        ProductSwitchResults switchResult = ProductSwitch.from(root).to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProductA2));
        assertThat(asMap.get(child), isSuccessfulSwitch(childProductA1, childProductA2));
    }

    @Test
    public void testTo_SwitchesRecursive() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProduct2);
        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        TestGrandChildProduct grandchildProduct1 = new TestGrandChildProduct(repository, "d 1", "d", "1");
        repository.putProductComponent(grandchildProduct1);
        TestGrandChildProduct grandchildProduct2 = new TestGrandChildProduct(repository, "d 2", "d", "2");
        repository.putProductComponent(grandchildProduct2);
        childProduct1.addTestGrandChildProduct(grandchildProduct1);
        childProduct2.addTestGrandChildProduct(grandchildProduct2);
        TestChild child = childProduct1.createPolicyComponent();
        root.addTestChild(child);
        TestGrandChild grandChild = grandchildProduct1.createPolicyComponent();
        child.addTestGrandChild(grandChild);

        ProductSwitchResults switchResult = ProductSwitch.from(root).to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(root.getTestChild(0).getProductComponent(), is(childProduct2));
        assertThat(asMap.get(child), isSuccessfulSwitch(childProduct1, childProduct2));
        assertThat(root.getTestChild(0).getTestGrandChild(0).getProductComponent(), is(grandchildProduct2));
        assertThat(asMap.get(grandChild),
                isSuccessfulSwitch(grandchildProduct1, grandchildProduct2));
        assertThat(switchResult.containsFailures(), is(false));

    }

    @Test
    public void testTo_matchingBy_ProductCmptType() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestRootProduct rootProduct1 = new TestRootProduct(repository, "r 1", "r", "1");
        TestRootProduct rootProduct2 = new TestRootProduct(repository, "r 2", "r", "2");
        TestRoot root = rootProduct1.createPolicyComponent();
        TestChildProduct childProduct1 = new TestChildProduct(repository, "b 1", "b", "1");
        repository.putProductComponent(childProduct1);
        TestChildProduct childProduct2 = new TestChildProduct(repository, "c 2", "c", "2");
        repository.putProductComponent(childProduct2);
        TestChildProduct childProduct3 = new TestChildProduct(repository, "d 3", "d", "3");
        repository.putProductComponent(childProduct3);
        rootProduct1.addTestChildProduct(childProduct1);
        rootProduct2.addTestChildProduct(childProduct2);
        rootProduct2.addTestChildProduct(childProduct3);
        TestGrandChildProduct grandchildProduct1 = new TestGrandChildProduct(repository, "d 1", "d", "1");
        repository.putProductComponent(grandchildProduct1);
        TestGrandChildProduct grandchildProduct2 = new TestGrandChildProduct(repository, "d 2", "d", "2");
        repository.putProductComponent(grandchildProduct2);
        childProduct1.addTestGrandChildProduct(grandchildProduct1);
        childProduct1.addTestGrandChildProduct(grandchildProduct2);
        TestChild child = childProduct1.createPolicyComponent();
        root.addTestChild(child);
        TestGrandChild grandChild = grandchildProduct1.createPolicyComponent();
        child.addTestGrandChild(grandChild);
        rootProduct1.setSwitchType("root");
        rootProduct2.setSwitchType("root");
        childProduct1.setSwitchType("childProduct");
        childProduct2.setSwitchType("childProduct");
        childProduct3.setSwitchType("NotchildProduct");
        grandchildProduct1.setSwitchType("grandchildProduct1");
        grandchildProduct2.setSwitchType("grandchildProduct2");
        ProductCmptType ChildProductCmptType = IpsModel.getProductCmptType(childProduct1);

        ProductSwitchResults switchResult = ProductSwitch.from(root)
                .matchingBy(ChildProductCmptType, "SwitchType")
                .to(rootProduct2);

        Map<IConfigurableModelObject, ProductSwitchResult> asMap = switchResult.asMap();
        assertThat(switchResult.size(), is(3));
        assertThat(root.getProductComponent(), is(rootProduct2));
        assertThat(asMap.get(root), isSuccessfulSwitch(rootProduct1, rootProduct2));
        assertThat(asMap.get(child), isSuccessfulSwitch(childProduct1, childProduct2));
        assertThat(asMap.get(grandChild), isFailedSwitch());
        assertThat(switchResult.containsFailures(), is(true));
    }

    @Test
    public void testTo_DerivedUnion() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestChildProduct f1 = new TestChildProduct(repository, "f 1", "f", "1");
        repository.putProductComponent(f1);
        TestChildProduct f2 = new TestChildProduct(repository, "f 2", "f", "2");
        repository.putProductComponent(f2);
        TestChildProduct o1 = new TestChildProduct(repository, "o 1", "o", "1");
        repository.putProductComponent(o1);
        TestChildProduct o2 = new TestChildProduct(repository, "o 2", "o", "2");
        repository.putProductComponent(o2);
        DerivedUnionParentType p1 = new DerivedUnionParentType(repository, "p 1", "p", "1");
        p1.setFirstChildType(f1);
        p1.addOtherChildType(o1);
        repository.putProductComponent(p1);
        DerivedUnionParentType p2 = new DerivedUnionParentType(repository, "p 2", "p", "2");
        p2.setFirstChildType(f2);
        p2.addOtherChildType(o2);
        repository.putProductComponent(p2);

        DerivedUnionParent parent = p1.createDerivedUnionParent();
        parent.newFirstChild(f1);
        parent.newOtherChild(o1);

        ProductSwitchResults results = ProductSwitch.from(parent).to(p2);

        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent)).getOldProduct(), is(p1));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent)).getNewProduct(), is(p2));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getFirstChild())).getOldProduct(), is(f1));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getFirstChild())).getNewProduct(), is(f2));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getOtherChild(0))).getOldProduct(), is(o1));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getOtherChild(0))).getNewProduct(), is(o2));
    }

    @Test
    public void testTo_DerivedUnion_Advanced() {
        InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();
        TestChildProduct f1 = new TestChildProduct(repository, "f 1", "f", "1");
        repository.putProductComponent(f1);
        TestChildProduct f2 = new TestChildProduct(repository, "f 2", "f", "2");
        repository.putProductComponent(f2);
        TestChildProduct o1 = new TestChildProduct(repository, "o 1", "o", "1");
        repository.putProductComponent(o1);
        TestChildProduct o2 = new TestChildProduct(repository, "o 2", "o", "2");
        repository.putProductComponent(o2);
        DerivedUnionParentType p1 = new DerivedUnionParentType(repository, "p 1", "p", "1");
        p1.setFirstChildType(f1);
        p1.addOtherChildType(o1);
        repository.putProductComponent(p1);
        DerivedUnionParentType p2 = new DerivedUnionParentType(repository, "p 2", "p", "2");
        p2.setFirstChildType(f2);
        p2.addOtherChildType(o2);
        repository.putProductComponent(p2);

        DerivedUnionParent parent = p1.createDerivedUnionParent();
        parent.newFirstChild(f1);
        parent.newOtherChild(o1);

        ProductSwitchResults results = ProductSwitch.from(parent)
                .with(
                        ((AdvancedProductFinder)(__, ___, ____, _____) -> ProductFinderResult
                                .empty("just to trigger the AdvancedProductFinderSwitch"))
                                        .or(AdvancedProductFinder.BY_KIND_ID))
                .to(p2);

        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent)).getOldProduct(), is(p1));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent)).getNewProduct(), is(p2));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getFirstChild())).getOldProduct(), is(f1));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getFirstChild())).getNewProduct(), is(f2));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getOtherChild(0))).getOldProduct(), is(o1));
        assertThat(((SuccessfulProductSwitch)results.getResultFor(parent.getOtherChild(0))).getNewProduct(), is(o2));
    }

    public static Matcher<ProductSwitchResult> isSuccessfulSwitch(IProductComponent from, IProductComponent to) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is a successful switch from ");
                description.appendValue(from);
                description.appendText(" to ");
                description.appendValue(to);
            }

            @Override
            protected boolean matchesSafely(ProductSwitchResult item) {
                return (item instanceof SuccessfulProductSwitch)
                        && ((SuccessfulProductSwitch)item).getOldProduct() == from
                        && ((SuccessfulProductSwitch)item).getNewProduct() == to;
            }

            @Override
            protected void describeMismatchSafely(ProductSwitchResult item, Description mismatchDescription) {
                if (!(item instanceof SuccessfulProductSwitch)) {
                    mismatchDescription.appendText("was unsuccessful");
                    return;
                }
                IProductComponent oldProduct = ((SuccessfulProductSwitch)item).getOldProduct();
                if (oldProduct != from) {
                    mismatchDescription.appendText("oldProduct was ");
                    mismatchDescription.appendValue(oldProduct);
                }
                IProductComponent newProduct = ((SuccessfulProductSwitch)item).getNewProduct();
                if (newProduct != to) {
                    mismatchDescription.appendText("newProduct was ");
                    mismatchDescription.appendValue(newProduct);
                }
            }
        };
    }

    public static Matcher<ProductSwitchResult> isFailedSwitch() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is a failed switch");
            }

            @Override
            protected boolean matchesSafely(ProductSwitchResult item) {
                return (item instanceof FailedProductSwitch);
            }

            @Override
            protected void describeMismatchSafely(ProductSwitchResult item, Description mismatchDescription) {
                mismatchDescription.appendText("was successful from ");
                mismatchDescription.appendValue(((SuccessfulProductSwitch)item).getOldProduct());
                mismatchDescription.appendText(" to ");
                mismatchDescription.appendValue(((SuccessfulProductSwitch)item).getNewProduct());
            }
        };
    }

    @IpsPolicyCmptType(name = "TestRoot")
    @IpsAssociations({ "TestChild" })
    @IpsConfiguredBy(TestRootProduct.class)
    public static class TestRoot implements IConfigurableModelObject {

        private IProductComponent productComponent;
        private final List<TestChild> testChildren = new ArrayList<>();

        public TestRoot(TestRootProduct productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return MessageLists.emptyMessageList();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productComponent;
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return Calendar.getInstance();
        }

        @Override
        public void initialize() {
            // nothing to do
        }

        @IpsAssociation(name = "TestChild", pluralName = "TestChildren", kind = AssociationKind.Composition, targetClass = TestChild.class, min = 0, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = TestRootProduct.class, name = "TestChildProduct")
        public List<? extends TestChild> getTestChildren() {
            return Collections.unmodifiableList(testChildren);
        }

        public TestChild getTestChild(int i) {
            return testChildren.get(i);
        }

        @IpsAssociationAdder(association = "TestChild")
        public void addTestChild(TestChild objectToAdd) {
            addTestChildInternal(objectToAdd);
        }

        public void addTestChildInternal(TestChild objectToAdd) {
            if (objectToAdd == null) {
                throw new NullPointerException("Can't add null to association TestChild of " + this);
            }
            if (testChildren.contains(objectToAdd)) {
                return;
            }
            testChildren.add(objectToAdd);
        }

        @IpsAssociationRemover(association = "TestChild")
        public void removeTestChild(TestChild objectToRemove) {
            if (objectToRemove == null) {
                return;
            }
            testChildren.remove(objectToRemove);
        }
    }

    @IpsProductCmptType(name = "TestRootProduct")
    @IpsAssociations({ "TestChildProduct" })
    @IpsAttributes({ "SwitchType" })
    @IpsConfigures(TestRoot.class)
    public static class TestRootProduct extends ProductComponent {

        private Map<String, IProductComponentLink<TestChildProduct>> testChildProducts = new LinkedHashMap<>(0);
        private String switchType;

        public TestRootProduct(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @Override
        public TestRoot createPolicyComponent() {
            return new TestRoot(this);
        }

        @IpsAssociationAdder(association = "TestChildProduct")
        public void addTestChildProduct(TestChildProduct target) {
            testChildProducts.put(target.getId(), new ProductComponentLink<>(this, target, "TestChildProduct"));
        }

        @IpsAssociationAdder(association = "TestChildProduct", withCardinality = true)
        public void addTestChildProduct(TestChildProduct target, CardinalityRange cardinality) {
            testChildProducts.put(target.getId(),
                    new ProductComponentLink<>(this, target, cardinality, "TestChildProduct"));
        }

        @IpsAssociationRemover(association = "TestChildProduct")
        public void removeTestChildProduct(TestChildProduct target) {
            testChildProducts.remove(target.getId());
        }

        @IpsAssociationLinks(association = "TestChildProduct")
        public Collection<IProductComponentLink<TestChildProduct>> getLinksForTestChildProducts() {
            return Collections.unmodifiableCollection(testChildProducts.values());
        }

        @IpsAttribute(name = "SwitchType", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getSwitchType() {
            return switchType;
        }

        @IpsAttributeSetter("SwitchType")
        public void setSwitchType(String switchType) {
            this.switchType = switchType;
        }

        @IpsAssociation(name = "TestChildProduct", pluralName = "TestChildProducts", kind = AssociationKind.Composition, targetClass = TestChildProduct.class, min = 0, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = TestRoot.class, name = "TestChild")
        public List<? extends TestChildProduct> getTestChildProducts() {
            List<TestChildProduct> result = new ArrayList<>(testChildProducts.size());
            for (IProductComponentLink<TestChildProduct> testChildProduct : testChildProducts.values()) {
                if (!testChildProduct.getCardinality().isEmpty()) {
                    result.add(testChildProduct.getTarget());
                }
            }
            return result;
        }

    }

    @IpsPolicyCmptType(name = "TestChild")
    @IpsAssociations({ "TestGrandChild" })
    @IpsAttributes({ "SwitchType" })
    @IpsConfiguredBy(TestChildProduct.class)
    public static class TestChild implements IConfigurableModelObject {

        private IProductComponent productComponent;
        private final List<TestGrandChild> testGrandChildren = new ArrayList<>();
        private String switchType;

        public TestChild(TestChildProduct productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return MessageLists.emptyMessageList();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productComponent;
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return Calendar.getInstance();
        }

        @Override
        public void initialize() {
            // nothing to do
        }

        @IpsAssociation(name = "TestGrandChild", pluralName = "TestCGrandhildren", kind = AssociationKind.Composition, targetClass = TestChild.class, min = 0, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = TestChildProduct.class, name = "TestGrandChildProduct")
        public List<? extends TestGrandChild> getTestGrandChildren() {
            return Collections.unmodifiableList(testGrandChildren);
        }

        public TestGrandChild getTestGrandChild(int i) {
            return testGrandChildren.get(i);
        }

        @IpsAssociationAdder(association = "TestGrandChild")
        public void addTestGrandChild(TestGrandChild objectToAdd) {
            addTestGrandChildInternal(objectToAdd);
        }

        @IpsAttribute(name = "SwitchType", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getSwitchType() {
            return switchType;
        }

        @IpsAttributeSetter("SwitchType")
        public void setSwitchType(String switchType) {
            this.switchType = switchType;
        }

        public void addTestGrandChildInternal(TestGrandChild objectToAdd) {
            if (objectToAdd == null) {
                throw new NullPointerException("Can't add null to association TestGrandChild of " + this);
            }
            if (testGrandChildren.contains(objectToAdd)) {
                return;
            }
            testGrandChildren.add(objectToAdd);
        }

        @IpsAssociationRemover(association = "TestGrandChild")
        public void removeTestGrandChild(TestGrandChild objectToRemove) {
            if (objectToRemove == null) {
                return;
            }
            testGrandChildren.remove(objectToRemove);
        }

    }

    @IpsProductCmptType(name = "TestChildProduct")
    @IpsAssociations({ "TestGrandChildProduct" })
    @IpsAttributes({ "SwitchType" })
    @IpsConfigures(TestChild.class)
    public static class TestChildProduct extends ProductComponent {

        private Map<String, IProductComponentLink<TestGrandChildProduct>> testGrandChildProducts = new LinkedHashMap<>(
                0);

        private String switchType;

        public TestChildProduct(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @Override
        public TestChild createPolicyComponent() {
            return new TestChild(this);
        }

        @IpsAssociationAdder(association = "TestGrandChildProduct")
        public void addTestGrandChildProduct(TestGrandChildProduct target) {
            testGrandChildProducts.put(target.getId(),
                    new ProductComponentLink<>(this, target, "TestGrandChildProduct"));
        }

        @IpsAssociationAdder(association = "TestGrandChildProduct", withCardinality = true)
        public void addTestGrandChildProduct(TestGrandChildProduct target, CardinalityRange cardinality) {
            testGrandChildProducts.put(target.getId(),
                    new ProductComponentLink<>(this, target, cardinality, "TestGrandChildProduct"));
        }

        @IpsAssociationRemover(association = "TestGrandChildProduct")
        public void removeTestGrandChildProduct(TestGrandChildProduct target) {
            testGrandChildProducts.remove(target.getId());
        }

        @IpsAttribute(name = "SwitchType", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getSwitchType() {
            return switchType;
        }

        @IpsAssociationLinks(association = "TestGrandChildProduct")
        public Collection<IProductComponentLink<TestGrandChildProduct>> getLinksForTestGrandChildProducts() {
            return Collections.unmodifiableCollection(testGrandChildProducts.values());
        }

        @IpsAssociation(name = "TestGrandChildProduct", pluralName = "TestGrandChildProducts", kind = AssociationKind.Composition, targetClass = TestChildProduct.class, min = 0, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = TestChild.class, name = "TestGrandChild")
        public List<? extends TestGrandChildProduct> getTestGrandChildProducts() {
            List<TestGrandChildProduct> result = new ArrayList<>(testGrandChildProducts.size());
            for (IProductComponentLink<TestGrandChildProduct> testGrandChildProduct : testGrandChildProducts.values()) {
                if (!testGrandChildProduct.getCardinality().isEmpty()) {
                    result.add(testGrandChildProduct.getTarget());
                }
            }
            return result;
        }

        @IpsAttributeSetter("SwitchType")
        public void setSwitchType(String switchType) {
            this.switchType = switchType;
        }

    }

    @IpsPolicyCmptType(name = "TestGrandChild")
    public static class TestGrandChild implements IConfigurableModelObject {

        private IProductComponent productComponent;

        public TestGrandChild(TestGrandChildProduct productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return MessageLists.emptyMessageList();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productComponent;
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return Calendar.getInstance();
        }

        @Override
        public void initialize() {
            // nothing to do
        }

    }

    @IpsProductCmptType(name = "TestGrandChildProduct")
    @IpsAttributes({ "SwitchType" })
    public static class TestGrandChildProduct extends ProductComponent {

        private String switchType;

        public TestGrandChildProduct(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @Override
        public TestGrandChild createPolicyComponent() {
            return new TestGrandChild(this);
        }

        @IpsAttribute(name = "SwitchType", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getSwitchType() {
            return switchType;
        }

        @IpsAttributeSetter("SwitchType")
        public void setSwitchType(String switchType) {
            this.switchType = switchType;
        }

    }

    @IpsPolicyCmptType(name = "DerivedUnionParent")
    @IpsAssociations({ "Child", "FirstChild", "OtherChild" })
    @IpsConfiguredBy(DerivedUnionParentType.class)
    public class DerivedUnionParent implements IConfigurableModelObject {
        public static final String ASSOCIATION_CHILDREN = "children";
        public static final String ASSOCIATION_FIRST_CHILD = "firstChild";
        public static final String ASSOCIATION_OTHER_CHILDREN = "otherChildren";

        private ProductConfiguration productConfiguration;
        private TestChild firstChild = null;
        private List<TestChild> otherChildren = new ArrayList<>();

        public DerivedUnionParent(DerivedUnionParentType productCmpt) {
            productConfiguration = new ProductConfiguration(productCmpt);
        }

        @IpsAssociation(name = "FirstChild", pluralName = "", kind = AssociationKind.Composition, targetClass = TestChild.class, min = 0, max = 1)
        @IpsSubsetOfDerivedUnion("Child")
        @IpsMatchingAssociation(source = DerivedUnionParentType.class, name = "FirstChildType")
        public TestChild getFirstChild() {
            return firstChild;
        }

        @IpsAssociationAdder(association = "FirstChild")
        public void setFirstChild(TestChild newObject) {
            firstChild = newObject;
        }

        public TestChild newFirstChild(TestChildProduct childType) {
            TestChild newFirstChild = childType.createPolicyComponent();
            setFirstChild(newFirstChild);
            newFirstChild.initialize();
            return newFirstChild;
        }

        @IpsAssociation(name = "OtherChild", pluralName = "OtherChildren", kind = AssociationKind.Composition, targetClass = TestChild.class, min = 0, max = Integer.MAX_VALUE)
        @IpsSubsetOfDerivedUnion("Child")
        @IpsMatchingAssociation(source = DerivedUnionParentType.class, name = "OtherChildType")
        public List<? extends TestChild> getOtherChildren() {
            return Collections.unmodifiableList(otherChildren);
        }

        public TestChild getOtherChild(int index) {
            return otherChildren.get(index);
        }

        @IpsAssociationAdder(association = "OtherChild")
        public void addOtherChild(TestChild objectToAdd) {
            if (objectToAdd == null) {
                throw new NullPointerException("Can't add null to association OtherChild of " + this);
            }
            if (otherChildren.contains(objectToAdd)) {
                return;
            }
            otherChildren.add(objectToAdd);
        }

        public TestChild newOtherChild(TestChildProduct childType) {
            TestChild newOtherChild = childType.createPolicyComponent();
            addOtherChild(newOtherChild);
            newOtherChild.initialize();
            return newOtherChild;
        }

        public int getNumOfOtherChildren() {
            return otherChildren.size();
        }

        @IpsAssociation(name = "Child", pluralName = "Children", kind = AssociationKind.Composition, targetClass = TestChild.class, min = 0, max = Integer.MAX_VALUE)
        @IpsDerivedUnion
        @IpsMatchingAssociation(source = DerivedUnionParentType.class, name = "ChildType")
        public List<TestChild> getChildren() {
            List<TestChild> result = new ArrayList<>(getNumOfChildrenInternal());
            if (getFirstChild() != null) {
                result.add(getFirstChild());
            }
            result.addAll(getOtherChildren());
            return result;
        }

        public int getNumOfChildren() {
            return getNumOfChildrenInternal();
        }

        private int getNumOfChildrenInternal() {
            int num = 0;
            num += firstChild == null ? 0 : 1;
            num += getNumOfOtherChildren();
            return num;
        }

        @Override
        public void initialize() {
            // begin-user-code
            // end-user-code
        }

        @Override
        public IProductComponent getProductComponent() {
            return productConfiguration.getProductComponent();
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            productConfiguration.setProductComponent(productComponent);
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return Calendar.getInstance();
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return MessageLists.emptyMessageList();
        }

    }

    @IpsProductCmptType(name = "DerivedUnionParentType")
    @IpsAssociations({ "ChildType", "FirstChildType", "OtherChildType" })
    @IpsConfigures(DerivedUnionParent.class)
    public class DerivedUnionParentType extends ProductComponent {

        private IProductComponentLink<TestChildProduct> firstChildType = null;
        private Map<String, IProductComponentLink<TestChildProduct>> otherChildTypes = new LinkedHashMap<>(0);

        public DerivedUnionParentType(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @IpsAssociation(name = "FirstChildType", pluralName = "", kind = AssociationKind.Composition, targetClass = TestChildProduct.class, min = 0, max = 1)
        @IpsSubsetOfDerivedUnion("ChildType")
        @IpsMatchingAssociation(source = DerivedUnionParent.class, name = "FirstChild")
        public TestChildProduct getFirstChildType() {
            return firstChildType != null ? firstChildType.getTarget() : null;
        }

        @IpsAssociationAdder(association = "FirstChildType")
        public void setFirstChildType(TestChildProduct target) {
            firstChildType = (target == null ? null : new ProductComponentLink<>(this, target, "FirstChildType"));
        }

        @IpsAssociationAdder(association = "FirstChildType", withCardinality = true)
        public void setFirstChildType(TestChildProduct target, CardinalityRange cardinality) {
            firstChildType = (target == null ? null
                    : new ProductComponentLink<>(this, target, cardinality, "FirstChildType"));
        }

        @IpsAssociationLinks(association = "FirstChildType")
        public IProductComponentLink<TestChildProduct> getLinkForFirstChildType() {
            return firstChildType;
        }

        @IpsAssociation(name = "OtherChildType", pluralName = "OtherChildTypes", kind = AssociationKind.Composition, targetClass = TestChildProduct.class, min = 0, max = Integer.MAX_VALUE)
        @IpsSubsetOfDerivedUnion("ChildType")
        @IpsMatchingAssociation(source = DerivedUnionParent.class, name = "OtherChild")
        public List<? extends TestChildProduct> getOtherChildTypes() {
            List<TestChildProduct> result = new ArrayList<>(otherChildTypes.size());
            for (IProductComponentLink<TestChildProduct> otherChildType : otherChildTypes.values()) {
                if (!otherChildType.getCardinality().isEmpty()) {
                    result.add(otherChildType.getTarget());
                }
            }
            return result;
        }

        public TestChildProduct getOtherChildType(int index) {
            return ProductComponentLinks.getTarget(index, otherChildTypes);
        }

        @IpsAssociationAdder(association = "OtherChildType")
        public void addOtherChildType(TestChildProduct target) {
            otherChildTypes.put(target.getId(), new ProductComponentLink<>(this, target, "OtherChildType"));
        }

        @IpsAssociationAdder(association = "OtherChildType", withCardinality = true)
        public void addOtherChildType(TestChildProduct target, CardinalityRange cardinality) {
            otherChildTypes.put(target.getId(),
                    new ProductComponentLink<>(this, target, cardinality, "OtherChildType"));
        }

        @IpsAssociationLinks(association = "OtherChildType")
        public Collection<IProductComponentLink<TestChildProduct>> getLinksForOtherChildTypes() {
            return Collections.unmodifiableCollection(otherChildTypes.values());
        }

        public int getNumOfOtherChildTypes() {
            return otherChildTypes.size();
        }

        @IpsAssociation(name = "ChildType", pluralName = "ChildTypes", kind = AssociationKind.Composition, targetClass = TestChildProduct.class, min = 0, max = Integer.MAX_VALUE)
        @IpsDerivedUnion
        @IpsMatchingAssociation(source = DerivedUnionParent.class, name = "Child")
        public List<TestChildProduct> getChildTypes() {
            List<TestChildProduct> result = new ArrayList<>(getNumOfChildTypesInternal());
            if (firstChildType != null) {
                result.add(getFirstChildType());
            }
            result.addAll(getOtherChildTypes());
            return result;
        }

        public int getNumOfChildTypes() {
            return getNumOfChildTypesInternal();
        }

        private int getNumOfChildTypesInternal() {
            int num = 0;
            num += firstChildType == null ? 0 : 1;
            num += getNumOfOtherChildTypes();
            return num;
        }

        public DerivedUnionParent createDerivedUnionParent() {
            DerivedUnionParent policy = new DerivedUnionParent(this);
            policy.initialize();
            return policy;
        }

        @Override
        public DerivedUnionParent createPolicyComponent() {
            return createDerivedUnionParent();
        }

        @Override
        public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
            List<IProductComponentLink<? extends IProductComponent>> list = new ArrayList<>();
            if (getLinkForFirstChildType() != null) {
                list.add(getLinkForFirstChildType());
            }
            list.addAll(getLinksForOtherChildTypes());
            return list;
        }
    }

}
