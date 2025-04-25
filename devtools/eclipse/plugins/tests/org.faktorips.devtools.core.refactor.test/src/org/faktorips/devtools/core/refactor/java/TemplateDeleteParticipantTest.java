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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class TemplateDeleteParticipantTest extends AbstractStdBuilderTest {

    @Test
    public void testInitialize_SomeElement() {
        var templateDeleteParticipant = new TemplateDeleteParticipant();

        boolean initialized = templateDeleteParticipant.initialize("Some Object");

        assertThat(initialized, is(false));
    }

    @Test
    public void testInitialize_SomeResource() {
        var ipsProject = newIpsProject();
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        var templateDeleteParticipant = new TemplateDeleteParticipant();

        boolean initialized = templateDeleteParticipant.initialize(javaProject.getProject());

        assertThat(initialized, is(false));
    }

    @Test
    public void testInitialize_SomeIpsSrcFile() {
        var ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var templateDeleteParticipant = new TemplateDeleteParticipant();

        boolean initialized = templateDeleteParticipant.initialize(productCmptType.getIpsSrcFile());

        assertThat(initialized, is(false));
    }

    @Test
    public void testInitialize_SomeIpsSrcFileResource() {
        var ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var templateDeleteParticipant = new TemplateDeleteParticipant();

        boolean initialized = templateDeleteParticipant
                .initialize(productCmptType.getIpsSrcFile().getCorrespondingFile().unwrap());

        assertThat(initialized, is(false));
    }

    @Test
    public void testInitialize_SomeTemplateResource() {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var template = newProductTemplate(productCmptType, "my.Template");
        var templateDeleteParticipant = new TemplateDeleteParticipant();

        boolean initialized = templateDeleteParticipant
                .initialize(template.getIpsSrcFile().getCorrespondingFile().unwrap());

        assertThat(initialized, is(true));
    }

    @Test
    public void testCreateChange_Uninitialized() throws OperationCanceledException, CoreException {
        var templateDeleteParticipant = new TemplateDeleteParticipant();

        var change = templateDeleteParticipant.createChange(null);

        assertThat(change, is(nullValue()));
    }

    @Test
    public void testCreateChange_NoUsages() throws OperationCanceledException, CoreException {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var template = newProductTemplate(productCmptType, "my.Template");
        var templateDeleteParticipant = new TemplateDeleteParticipant();
        templateDeleteParticipant.initialize(template.getIpsSrcFile().getCorrespondingFile().unwrap());

        var change = templateDeleteParticipant.createChange(null);

        assertThat(change, is(nullValue()));
    }

    @Test
    public void testCreateChange_WithUsages() throws OperationCanceledException, CoreException {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var template = newProductTemplate(productCmptType, "my.Template");
        var product1 = newProductCmpt(productCmptType, "my.Product");
        product1.setTemplate("my.Template");
        product1.getIpsSrcFile().save(null);
        var product2 = newProductCmpt(productCmptType, "another.Product");
        product2.setTemplate("my.Template");
        product2.getIpsSrcFile().save(null);
        var templateDeleteParticipant = new TemplateDeleteParticipant();
        templateDeleteParticipant.initialize(template.getIpsSrcFile().getCorrespondingFile().unwrap());

        var change = templateDeleteParticipant.createChange(null);

        assertThat(change, is(instanceOf(CompositeChange.class)));
        Change[] children = ((CompositeChange)change).getChildren();
        assertThat(children.length, is(2));
        assertThat(children[0], is(instanceOf(RemoveTemplateUsageChange.class)));
        assertThat(children[1], is(instanceOf(RemoveTemplateUsageChange.class)));
        // "another" before "my"
        assertThat(children[0].getModifiedElement(),
                is(equalTo(product2.getIpsSrcFile().getCorrespondingFile().unwrap())));
        assertThat(children[1].getModifiedElement(),
                is(equalTo(product1.getIpsSrcFile().getCorrespondingFile().unwrap())));
    }

    @Test
    public void testCreateChange_IgnoresProgressMonitor() throws OperationCanceledException, CoreException {
        var ipsProject = newIpsProject();
        var productCmptType = newProductCmptType(ipsProject, "my.ProductType");
        var template = newProductTemplate(productCmptType, "my.Template");
        var product1 = newProductCmpt(productCmptType, "my.Product");
        product1.setTemplate("my.Template");
        product1.getIpsSrcFile().save(null);
        var product2 = newProductCmpt(productCmptType, "another.Product");
        product2.setTemplate("my.Template");
        product2.getIpsSrcFile().save(null);
        var templateDeleteParticipant = new TemplateDeleteParticipant();
        templateDeleteParticipant.initialize(template.getIpsSrcFile().getCorrespondingFile().unwrap());
        IProgressMonitor progressMonitor = mock(IProgressMonitor.class);

        templateDeleteParticipant.createChange(progressMonitor);

        verifyNoInteractions(progressMonitor);
    }
}
