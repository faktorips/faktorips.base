/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RemoveTemplateUsageChangeTest extends AbstractStdBuilderTest {

    @Test
    public void testPerform_HandlesNullProgressMonitor() {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        productCmpt.setTemplate("a.Template");
        productCmpt.getIpsSrcFile().save(null);
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());

        change.perform(null);

        assertThat(productCmpt.getTemplate(), is(nullValue()));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
    }

    @Test
    public void testPerform() {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        productCmpt.setTemplate("a.Template");
        productCmpt.getIpsSrcFile().save(null);
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        change.perform(progressMonitor);

        verify(progressMonitor).beginTask("", 1);
        ArgumentCaptor<String> taskNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(progressMonitor).setTaskName(taskNameCaptor.capture());
        assertThat(taskNameCaptor.getValue(), containsString("a.Template"));
        assertThat(taskNameCaptor.getValue(), containsString("my.Product"));
        verify(progressMonitor).done();
        assertThat(productCmpt.getTemplate(), is(nullValue()));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
    }

    @Test
    public void testPerform_Undo() throws CoreException {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        productCmpt.setTemplate("a.Template");
        productCmpt.getIpsSrcFile().save(null);
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        Change undo = change.perform(progressMonitor);

        verify(progressMonitor).beginTask("", 1);
        ArgumentCaptor<String> taskNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(progressMonitor).setTaskName(taskNameCaptor.capture());
        assertThat(taskNameCaptor.getValue(), containsString("a.Template"));
        assertThat(taskNameCaptor.getValue(), containsString("my.Product"));
        verify(progressMonitor).done();
        assertThat(productCmpt.getTemplate(), is(nullValue()));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));

        progressMonitor = mock(IProgressMonitor.class);
        undo.perform(progressMonitor);

        verify(progressMonitor).beginTask("", 1);
        verify(progressMonitor).setTaskName(taskNameCaptor.capture());
        assertThat(taskNameCaptor.getValue(), containsString("a.Template"));
        assertThat(taskNameCaptor.getValue(), containsString("my.Product"));
        verify(progressMonitor).done();
        assertThat(productCmpt.getTemplate(), is("a.Template"));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
    }

    @Test
    public void testPerform_Removes0to0Links() {
        var ipsProject = newIpsProject();
        var policyCmptType = newPolicyAndProductCmptType(ipsProject, "my.PolicyType", "my.ProductType");
        var productCmptType = policyCmptType.findProductCmptType(ipsProject);
        var targetPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "a.TargetPolicyType", "a.TargetProductType");
        var targetProductCmptType = targetPolicyCmptType.findProductCmptType(ipsProject);
        newComposition(policyCmptType, targetPolicyCmptType);
        IProductCmptTypeAssociation aggregation = newAggregation(productCmptType, targetProductCmptType);

        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        var targetProductCmpt = newProductCmpt(targetProductCmptType, "my.Target");
        var anotherTargetProductCmpt = newProductCmpt(targetProductCmptType, "another.Target");
        IProductCmptLink undefinedLink = productCmpt.newLink(aggregation);
        undefinedLink.setTarget(targetProductCmpt.getQualifiedName());
        undefinedLink.setCardinality(Cardinality.UNDEFINED);
        IProductCmptLink anotherLink = productCmpt.newLink(aggregation);
        anotherLink.setTarget(anotherTargetProductCmpt.getQualifiedName());
        anotherLink.setCardinality(new Cardinality(0, 1, 0));
        productCmpt.setTemplate("a.Template");
        productCmpt.getIpsSrcFile().save(null);
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        Change undo = change.perform(progressMonitor);

        assertThat(undo, is(nullValue()));
        verify(progressMonitor).beginTask("", 1);
        ArgumentCaptor<String> taskNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(progressMonitor).setTaskName(taskNameCaptor.capture());
        assertThat(taskNameCaptor.getValue(), containsString("a.Template"));
        assertThat(taskNameCaptor.getValue(), containsString("my.Product"));
        verify(progressMonitor).done();
        assertThat(productCmpt.getTemplate(), is(nullValue()));
        assertThat(productCmpt.getLinksAsList(), contains(anotherLink));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
    }

    @Test
    public void testPerform_Removes0to0LinksOnGeneration() {
        var ipsProject = newIpsProject();
        var policyCmptType = newPolicyAndProductCmptType(ipsProject, "my.PolicyType", "my.ProductType");
        var productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setChangingOverTime(true);
        var targetPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "a.TargetPolicyType", "a.TargetProductType");
        var targetProductCmptType = targetPolicyCmptType.findProductCmptType(ipsProject);
        newComposition(policyCmptType, targetPolicyCmptType);
        IProductCmptTypeAssociation aggregation = newAggregation(productCmptType, targetProductCmptType);
        aggregation.setChangingOverTime(true);

        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2025, 0, 1));
        var targetProductCmpt = newProductCmpt(targetProductCmptType, "my.Target");
        var anotherTargetProductCmpt = newProductCmpt(targetProductCmptType, "another.Target");
        IProductCmptLink undefinedLink = generation.newLink(aggregation);
        undefinedLink.setTarget(targetProductCmpt.getQualifiedName());
        undefinedLink.setCardinality(Cardinality.UNDEFINED);
        IProductCmptLink anotherLink = generation.newLink(aggregation);
        anotherLink.setTarget(anotherTargetProductCmpt.getQualifiedName());
        anotherLink.setCardinality(new Cardinality(0, 1, 0));
        productCmpt.setTemplate("a.Template");
        productCmpt.getIpsSrcFile().save(null);
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        Change undo = change.perform(progressMonitor);

        assertThat(undo, is(nullValue()));
        verify(progressMonitor).beginTask("", 1);
        ArgumentCaptor<String> taskNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(progressMonitor).setTaskName(taskNameCaptor.capture());
        assertThat(taskNameCaptor.getValue(), containsString("a.Template"));
        assertThat(taskNameCaptor.getValue(), containsString("my.Product"));
        verify(progressMonitor).done();
        assertThat(productCmpt.getTemplate(), is(nullValue()));
        assertThat(generation.getLinksAsList(), contains(anotherLink));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
    }

    @Test
    public void testPerform_AlreadyDeleted() {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        productCmpt.setTemplate("a.Template");
        productCmpt.getIpsSrcFile().save(null);
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());
        productCmpt.getIpsSrcFile().delete();
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        Change undo = change.perform(progressMonitor);

        assertThat(undo, is(nullValue()));
    }

    @Test
    public void testGetName() {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());

        String name = change.getName();

        assertThat(name, is("my.Product"));
    }

    @Test
    public void testGetModifiedResource() {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var productCmpt = newProductCmpt(productCmptType, "my.Product");
        var change = new RemoveTemplateUsageChange(productCmpt.getIpsSrcFile());

        IResource modifiedResource = change.getModifiedResource();

        assertThat(modifiedResource, is(sameInstance(productCmpt.getIpsSrcFile().getCorrespondingFile().unwrap())));
    }

}
