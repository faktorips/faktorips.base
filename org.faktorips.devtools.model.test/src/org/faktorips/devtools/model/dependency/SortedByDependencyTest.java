/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.dependency;

import static org.faktorips.abstracttest.matcher.IpsElementNamesMatcher.containsSubsetInOrder;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Ignore;
import org.junit.Test;

public class SortedByDependencyTest extends AbstractIpsPluginTest {
    @Test
    public void testSortByInstanceOf_SingleCmpt() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmpt productCmpt = newProductCmpt(ipsProject, "a.A");

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles();
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(mapToIpsObject(allIpsSrcFiles));

        assertThat(sorted, hasItems((IIpsObject)productCmpt));
    }

    @Test
    public void testSortByInstanceOf_SomeIndependantCmpt() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmpt productCmpt1 = newProductCmpt(ipsProject, "a.A1");
        ProductCmpt productCmpt2 = newProductCmpt(ipsProject, "a.A2");
        ProductCmpt productCmpt3 = newProductCmpt(ipsProject, "a.A3");

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles();
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(mapToIpsObject(allIpsSrcFiles));

        assertThat(sorted, hasItems((IIpsObject)productCmpt1, productCmpt2, productCmpt3));
    }

    @Test
    public void testSortByInstanceOf_SomeModelTypesAndDependencies() {
        IIpsProject ipsProject = newIpsProject();
        PolicyCmptType pcType = newPolicyAndProductCmptType(ipsProject, "m.AP", "m.AT");
        ProductCmptType childType = newProductCmptType(ipsProject, "m.CP", "m.CT");
        IProductCmptType type = pcType.findProductCmptType(ipsProject);
        newAggregation(type, childType, true);
        newAggregation(childType, type, true);
        ProductCmpt productCmpt1 = newProductCmpt(type, "a.A1");
        ProductCmpt productCmpt2 = newProductCmpt(type, "a.A2");
        ProductCmpt productCmpt3 = newProductCmpt(type, "a.A3");

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles();
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(mapToIpsObject(allIpsSrcFiles));

        assertThat(sorted, containsSubsetInOrder("m.AT", "a.A1"));
        assertThat(sorted, containsSubsetInOrder("m.AT", "a.A2"));
        assertThat(sorted, containsSubsetInOrder("m.AT", "a.A3"));
        assertThat(sorted, hasItems((IIpsObject)productCmpt1, productCmpt2, productCmpt3));
    }

    @Test
    public void testSortByInstanceOf_SameProject() {
        IIpsProject ipsProject = newIpsProject();
        {
            ProductCmpt template1 = newProductTemplate(ipsProject, "t.A1");
            ProductCmpt template2 = newProductTemplate(ipsProject, "t.A2");
            template2.setTemplate(template1.getQualifiedName());
            ProductCmpt template3 = newProductTemplate(ipsProject, "t.A3");
            template3.setTemplate(template2.getQualifiedName());

            ProductCmpt productCmpt = newProductCmpt(ipsProject, "a.A");
            productCmpt.setTemplate(template3.getQualifiedName());
        }
        {
            ProductCmpt template1 = newProductTemplate(ipsProject, "x.A1");
            ProductCmpt template2 = newProductTemplate(ipsProject, "x.A2");
            template2.setTemplate(template1.getQualifiedName());
            ProductCmpt template3 = newProductTemplate(ipsProject, "x.A3");
            template3.setTemplate(template1.getQualifiedName());

            ProductCmpt productCmpt = newProductCmpt(ipsProject, "b.A");
            productCmpt.setTemplate(template3.getQualifiedName());
        }

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles();
        Collection<IIpsObject> allIpsObject = mapToIpsObject(allIpsSrcFiles);
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(allIpsObject);

        assertThat(allIpsObject, hasItems(sorted.toArray(new IIpsObject[0])));
        assertThat(sorted, hasItems(allIpsObject.toArray(new IIpsObject[0])));
        assertThat(sorted, containsSubsetInOrder("t.A1", "t.A2", "t.A3", "a.A"));
        assertThat(sorted, containsSubsetInOrder("x.A1", "x.A2", "x.A3", "b.A"));
    }

    @Test
    public void testSortByInstanceOf_Partly_DifferentProjects() {
        IIpsProject ipsProject1 = newIpsProject();
        IIpsProject ipsProject2 = newIpsProject();
        IIpsProject ipsProject3 = newIpsProject();

        {
            ProductCmpt template1 = newProductTemplate(ipsProject1, "t.A1");
            ProductCmpt template2 = newProductTemplate(ipsProject1, "t.A2");
            template2.setTemplate(template1.getQualifiedName());
            ProductCmpt template3 = newProductTemplate(ipsProject2, "t.A3");
            template3.setTemplate(template2.getQualifiedName());

            ProductCmpt productCmpt1 = newProductCmpt(ipsProject2, "a.A1");
            productCmpt1.setTemplate(template3.getQualifiedName());
            ProductCmpt productCmpt2 = newProductCmpt(ipsProject1, "a.A2");
            productCmpt2.setTemplate(template1.getQualifiedName());
        }
        {
            ProductCmpt template1 = newProductTemplate(ipsProject1, "x.A1");
            ProductCmpt template2 = newProductTemplate(ipsProject2, "x.A2");
            template2.setTemplate(template1.getQualifiedName());
            ProductCmpt template3 = newProductTemplate(ipsProject2, "x.A3");
            template3.setTemplate(template1.getQualifiedName());

            ProductCmpt productCmpt = newProductCmpt(ipsProject3, "b.A");
            productCmpt.setTemplate(template3.getQualifiedName());
        }

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject2.findAllIpsSrcFiles();
        Collection<IIpsObject> allIpsObjects = mapToIpsObject(allIpsSrcFiles);
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(allIpsObjects);

        assertThat(allIpsObjects, hasItems(sorted.toArray(new IIpsObject[0])));
        assertThat(sorted, hasItems(allIpsObjects.toArray(new IIpsObject[0])));
        assertThat(sorted, containsSubsetInOrder("t.A3", "a.A1"));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Three templates, referencing each other in a cycle. This combination is not valid but the
     * algorithm should not break.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Both templates should be in the output in no specific order.
     */
    @Test
    public void testSortByInstanceOf_Cycle() {
        IIpsProject ipsProject1 = newIpsProject();

        ProductCmpt productCmpt = newProductCmpt(ipsProject1, "p.P");
        ProductCmpt template1 = newProductTemplate(ipsProject1, "t.A1");
        ProductCmpt template2 = newProductTemplate(ipsProject1, "t.A2");
        ProductCmpt template3 = newProductTemplate(ipsProject1, "t.A3");
        template2.setTemplate(template1.getQualifiedName());
        template3.setTemplate(template2.getQualifiedName());
        template1.setTemplate(template3.getQualifiedName());

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject1.findAllIpsSrcFiles();
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(mapToIpsObject(allIpsSrcFiles));

        assertThat(sorted, hasItems((IIpsObject)template1, template2, template3, productCmpt));
        assertThat(sorted, containsSubsetInOrder("p.P", "t.A1"));
        assertThat(sorted, containsSubsetInOrder("p.P", "t.A2"));
        assertThat(sorted, containsSubsetInOrder("p.P", "t.A3"));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Just like the test {@link #testSortByInstanceOf_Cycle()} but now there is a product component
     * that references one of the cyclic templates as its template. The order of the cyclic
     * templates could no be set but the single product component should be after its template.
     * <p>
     * <strong>Ignore:</strong> The test is ignored at the moment because the algorithm does not
     * reflect this behavior. It seems to be to complex for an invalid case.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The product component comes after the templates
     */
    @Ignore
    @Test
    public void testSortByInstanceOf_WithDependencyToCycle() {
        IIpsProject ipsProject1 = newIpsProject();

        ProductCmpt template1 = newProductTemplate(ipsProject1, "t.A1");
        ProductCmpt template2 = newProductTemplate(ipsProject1, "t.A2");
        ProductCmpt template3 = newProductTemplate(ipsProject1, "t.A3");
        template2.setTemplate(template1.getQualifiedName());
        template3.setTemplate(template2.getQualifiedName());
        template1.setTemplate(template3.getQualifiedName());

        ProductCmpt cmpt = newProductCmpt(ipsProject1, "a.P1");
        cmpt.setTemplate(template1.getQualifiedName());

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject1.findAllIpsSrcFiles();
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(mapToIpsObject(allIpsSrcFiles));

        assertThat(sorted, hasItems((IIpsObject)template1, template2, template3));
        assertThat(sorted, containsSubsetInOrder("t.A1", "a.P1"));
    }

    /**
     * Creates a lot of templates (int size = 10000) and adds some random dependencies to some of
     * them. The test is ignored because it may takes some time. Jan. 2019 (4 Core i7) about 10sec.
     * 
     * The time for the sort algorithm is about 100 ms
     */
    @Ignore
    @Test
    public void testSortByInstanceOf_PerformanceHint() {
        int size = 10000;

        IIpsProject ipsProject = newIpsProject();
        ArrayList<IIpsObject> input = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            ProductCmpt newProductTemplate = newProductTemplate(ipsProject, "a.A" + i);
            int rand = random.nextInt(i / 2 + 1);
            if (rand < input.size() / 2) {
                newProductTemplate.setTemplate(input.get(rand).getQualifiedNameType().getName());
            }
            input.add(newProductTemplate);
        }
        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles();
        Collection<IIpsObject> allIpsObject = mapToIpsObject(allIpsSrcFiles);

        long nanoTime = System.nanoTime();
        Set<IIpsObject> sorted = SortedByDependency.sortByInstanceOf(allIpsObject);
        long diff = System.nanoTime() - nanoTime;
        double msec = diff / 1000000.0;
        System.out.println(msec);

        assertThat(sorted, hasItems(input.toArray(new IIpsObject[size])));
    }

    private Collection<IIpsObject> mapToIpsObject(Collection<IIpsSrcFile> ipsSrcFiles) {
        LinkedHashSet<IIpsObject> result = new LinkedHashSet<>();
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            result.add(ipsSrcFile.getIpsObject());
        }
        return result;
    }

}
