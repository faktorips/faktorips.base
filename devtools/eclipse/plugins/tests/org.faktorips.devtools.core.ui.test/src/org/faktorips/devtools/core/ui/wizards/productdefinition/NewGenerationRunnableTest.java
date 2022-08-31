/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewGenerationRunnableTest extends AbstractIpsPluginTest {

    @Mock
    private ITimedIpsObject timedIpsObject1;

    @Mock
    private ITimedIpsObject timedIpsObject2;

    @Mock
    private IIpsSrcFile ipsSrcFile1;

    @Mock
    private IIpsSrcFile ipsSrcFile2;

    @Mock
    private IProgressMonitor monitor;

    private List<ITimedIpsObject> timedIpsObjects;

    private NewGenerationPMO pmo;

    private NewGenerationRunnable runnable;

    private AutoCloseable openMocks;

    @Override
    @Before
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(timedIpsObject1.getIpsSrcFile()).thenReturn(ipsSrcFile1);
        when(timedIpsObject2.getIpsSrcFile()).thenReturn(ipsSrcFile2);
        timedIpsObjects = Arrays.asList(timedIpsObject1, timedIpsObject2);

        pmo = new NewGenerationPMO();
        runnable = new NewGenerationRunnable(pmo, timedIpsObjects);
    }

    @After
    public void releaseMocks() throws Exception {
        openMocks.close();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // nothing to tear down
    }

    @Test
    public void testExecute_CancelStateIsPolled()
            throws IpsException, InvocationTargetException, InterruptedException {
        when(monitor.isCanceled()).thenReturn(false, true);

        runnable.execute(monitor);

        verify(timedIpsObject1).newGeneration(any(GregorianCalendar.class));
        verify(timedIpsObject2, never()).newGeneration(any(GregorianCalendar.class));
    }

    @Test
    public void testExecute_UpdateProgressMonitor() throws IpsException, InvocationTargetException,
            InterruptedException {

        runnable.execute(monitor);

        InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).beginTask(
                NLS.bind(Messages.NewGenerationRunnable_taskName, IpsPlugin.getDefault().getIpsPreferences()
                        .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(true)),
                timedIpsObjects.size());
        inOrder.verify(monitor).worked(1);
        inOrder.verify(monitor).worked(1);
        inOrder.verify(monitor).done();
    }

    @Test
    public void testExecute_CreateNewGenerations() throws IpsException, InvocationTargetException,
            InterruptedException {

        GregorianCalendar validFrom = new GregorianCalendar(1986, 4, 16);
        pmo.setValidFrom(validFrom);

        runnable.execute(monitor);

        verify(timedIpsObject1).newGeneration(validFrom);
        verify(timedIpsObject2).newGeneration(validFrom);
    }

    @Test
    public void testExecute_CreateNewGenerationEvenIfAGenerationWithThatDateAlreadyExists() throws IpsException,
            InvocationTargetException, InterruptedException {

        GregorianCalendar validFrom = new GregorianCalendar(1986, 4, 16);
        pmo.setValidFrom(validFrom);

        IIpsObjectGeneration generation1 = mock(IIpsObjectGeneration.class);
        IIpsObjectGeneration generation2 = mock(IIpsObjectGeneration.class);
        when(timedIpsObject1.getGenerationByEffectiveDate(validFrom)).thenReturn(generation1);
        when(timedIpsObject2.getGenerationByEffectiveDate(validFrom)).thenReturn(generation2);

        runnable.execute(monitor);

        verify(timedIpsObject1).newGeneration(validFrom);
        verify(timedIpsObject2).newGeneration(validFrom);
    }

    @Test
    public void testExecute_DoNotCreateNewGenerationIfGenerationWithThatDateExistsAndPMOIsConfiguredToSkip()
            throws IpsException, InvocationTargetException, InterruptedException {

        GregorianCalendar validFrom = new GregorianCalendar(1986, 4, 16);
        pmo.setValidFrom(validFrom);
        pmo.setSkipExistingGenerations(true);

        IIpsObjectGeneration generation1 = mock(IIpsObjectGeneration.class);
        when(timedIpsObject1.getGenerationByEffectiveDate(validFrom)).thenReturn(generation1);

        runnable.execute(monitor);

        verify(timedIpsObject1, never()).newGeneration(validFrom);
        verify(timedIpsObject2).newGeneration(validFrom);
    }

    @Test
    public void testExecute_UpdateProgressMonitorEvenIfObjectIsSkipped() throws IpsException,
            InvocationTargetException, InterruptedException {

        GregorianCalendar validFrom = new GregorianCalendar(1986, 4, 16);
        pmo.setValidFrom(validFrom);
        pmo.setSkipExistingGenerations(true);

        IIpsObjectGeneration generation1 = mock(IIpsObjectGeneration.class);
        when(timedIpsObject1.getGenerationByEffectiveDate(validFrom)).thenReturn(generation1);

        runnable.execute(monitor);

        verify(monitor, times(2)).worked(1);
    }

    @Test
    public void testExecute_SaveIfSourceFileWasNotDirtyBefore() throws IpsException, InvocationTargetException,
            InterruptedException {

        pmo.setValidFrom(new GregorianCalendar(1986, 4, 16));
        when(ipsSrcFile1.isDirty()).thenReturn(true);

        runnable.execute(monitor);

        verify(ipsSrcFile1, never()).save(monitor);
        verify(ipsSrcFile2).save(monitor);
    }

}
