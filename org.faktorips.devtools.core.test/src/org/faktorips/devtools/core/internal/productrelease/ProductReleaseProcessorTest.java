/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.productrelease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.productrelease.IReleaseAndDeploymentOperation;
import org.faktorips.devtools.core.productrelease.ITargetSystem;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ProductReleaseProcessorTest extends AbstractIpsPluginTest {

    private ProductReleaseProcessor productReleaseProcessor;
    private IIpsProject ipsProject;
    private MessageList messageList;
    private IReleaseAndDeploymentOperation releaseAndDeploymentOperation;
    private ObservableProgressMessages observableMessages;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = mock(IIpsProject.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));

        when(ipsProject.getReadOnlyProperties().getReleaseExtensionId()).thenReturn("releaseExtensionId");
        when(ipsProject.getIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[0]);

        IConfigurationElement configElement = mock(IConfigurationElement.class);
        when(configElement.getAttribute("id")).thenReturn("releaseExtensionId");

        IExtensionRegistry extensionRegistry = mock(IExtensionRegistry.class);
        when(extensionRegistry.getConfigurationElementsFor(null, null)).thenReturn(
                new IConfigurationElement[] { configElement });

        observableMessages = new ObservableProgressMessages();
        productReleaseProcessor = spy(new ProductReleaseProcessor(ipsProject, observableMessages));

        messageList = new MessageList();
        Observer observer = new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                messageList.add((Message)arg);
            }
        };
        observableMessages.addObserver(observer);

    }

    @Test
    public void testStartReleaseBuilder() throws Exception {
        productReleaseProcessor.startReleaseBuilder("abc", null, new NullProgressMonitor());
        assertTrue(messageList.containsErrorMsg());
        assertEquals(1, messageList.size());
        assertEquals(Messages.ReleaseAndDeploymentOperation_exception_noDeploymentExtension, messageList.getMessage(0)
                .getText());

        releaseAndDeploymentOperation = mock(IReleaseAndDeploymentOperation.class);
        when(productReleaseProcessor.getReleaseAndDeploymentOperation()).thenReturn(releaseAndDeploymentOperation);

        try {
            productReleaseProcessor.startReleaseBuilder("abc", null, new NullProgressMonitor());
        } catch (InterruptedException e) {
            assertEquals(Messages.ProductReleaseProcessor_error_custom_validation_failed, e.getMessage());
        }

        releaseAndDeploymentOperation = mock(IReleaseAndDeploymentOperation.class);
        when(productReleaseProcessor.getReleaseAndDeploymentOperation()).thenReturn(releaseAndDeploymentOperation);
        when(releaseAndDeploymentOperation.preCommit(any(IIpsProject.class), any(IProgressMonitor.class))).thenReturn(
                true);

        ArrayList<ITargetSystem> targetSystems = new ArrayList<ITargetSystem>();
        targetSystems.add(new DefaultTargetSystem("test123"));

        productReleaseProcessor.startReleaseBuilder("abc", targetSystems, new NullProgressMonitor());

        verify(releaseAndDeploymentOperation).preCommit(eq(ipsProject), any(IProgressMonitor.class));

        verify(releaseAndDeploymentOperation).buildReleaseAndDeployment(eq(ipsProject), eq("abc"), eq(targetSystems),
                any(IProgressMonitor.class));

        verify(releaseAndDeploymentOperation).additionalResourcesToCommit(ipsProject);

    }

    @Test
    public void testTeamOperationsFactory() throws InterruptedException, CoreException {
        IpsPlugin ipsPlugin = spy(IpsPlugin.getDefault());
        SingletonMockHelper singletonMockHelper = new SingletonMockHelper();
        try {
            singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
            ITeamOperationsFactory teamOperationsFactory = mock(ITeamOperationsFactory.class);
            when(ipsPlugin.getTeamOperationsFactories()).thenReturn(Collections.singleton(teamOperationsFactory));

            releaseAndDeploymentOperation = mock(IReleaseAndDeploymentOperation.class);
            when(releaseAndDeploymentOperation.preCommit(any(IIpsProject.class), any(IProgressMonitor.class)))
                    .thenReturn(true);
            ArrayList<ITargetSystem> targetSystems = new ArrayList<ITargetSystem>();
            targetSystems.add(new DefaultTargetSystem("test123"));

            productReleaseProcessor = spy(new ProductReleaseProcessor(ipsProject, observableMessages));
            when(productReleaseProcessor.getReleaseAndDeploymentOperation()).thenReturn(releaseAndDeploymentOperation);
            productReleaseProcessor.startReleaseBuilder("abc", targetSystems, new NullProgressMonitor());

            verify(teamOperationsFactory).canCreateTeamOperationsFor(ipsProject);
            verify(teamOperationsFactory, never()).createTeamOperations(any(ObservableProgressMessages.class));

            when(teamOperationsFactory.canCreateTeamOperationsFor(ipsProject)).thenReturn(true);
            ITeamOperations teamOperations = mock(ITeamOperations.class);
            when(teamOperations.isProjectSynchronized(any(IProject.class), any(IProgressMonitor.class))).thenReturn(
                    true);
            when(teamOperationsFactory.createTeamOperations(any(ObservableProgressMessages.class))).thenReturn(
                    teamOperations);

            productReleaseProcessor = spy(new ProductReleaseProcessor(ipsProject, observableMessages));
            when(productReleaseProcessor.getReleaseAndDeploymentOperation()).thenReturn(releaseAndDeploymentOperation);
            productReleaseProcessor.startReleaseBuilder("abc", targetSystems, new NullProgressMonitor());

            verify(teamOperationsFactory, atLeastOnce()).createTeamOperations(any(ObservableProgressMessages.class));
            verify(teamOperations).commitFiles(any(IProject.class), (IResource[])any(),
                    eq(Messages.ReleaseAndDeploymentOperation_commit_comment + "abc"), any(IProgressMonitor.class));
            verify(teamOperations).tagProject(any(IProject.class), eq("abc"), any(IProgressMonitor.class));
        } finally {
            singletonMockHelper.reset();
        }
    }
}
