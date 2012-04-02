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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.mockito.Matchers.any;
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
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

    @Override
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(timedIpsObject1.getIpsSrcFile()).thenReturn(ipsSrcFile1);
        when(timedIpsObject2.getIpsSrcFile()).thenReturn(ipsSrcFile2);
        timedIpsObjects = Arrays.asList(timedIpsObject1, timedIpsObject2);

        pmo = new NewGenerationPMO();
        runnable = new NewGenerationRunnable(pmo, timedIpsObjects);
    }

    @Test
    public void testExecute_CancelStateIsPolled() throws CoreException, InvocationTargetException, InterruptedException {
        when(monitor.isCanceled()).thenReturn(false, true);

        runnable.execute(monitor);

        verify(timedIpsObject1).newGeneration(any(GregorianCalendar.class));
        verify(timedIpsObject2, never()).newGeneration(any(GregorianCalendar.class));
    }

    @Test
    public void testExecute_UpdateProgressMonitor() throws CoreException, InvocationTargetException,
            InterruptedException {

        runnable.execute(monitor);

        InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).beginTask(Messages.NewGenerationRunnable_taskName, timedIpsObjects.size());
        inOrder.verify(monitor).worked(1);
        inOrder.verify(monitor).worked(1);
        inOrder.verify(monitor).done();
    }

    @Test
    public void testExecute_CreateNewGenerations() throws CoreException, InvocationTargetException,
            InterruptedException {

        GregorianCalendar validFrom = new GregorianCalendar(1986, 4, 16);
        pmo.setValidFrom(validFrom);

        runnable.execute(monitor);

        verify(timedIpsObject1).newGeneration(validFrom);
        verify(timedIpsObject2).newGeneration(validFrom);
    }

    @Test
    public void testExecute_CreateNewGenerationEvenIfAGenerationWithThatDateAlreadyExists() throws CoreException,
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
            throws CoreException, InvocationTargetException, InterruptedException {

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
    public void testExecute_UpdateProgressMonitorEvenIfObjectIsSkipped() throws CoreException,
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
    public void testExecute_SaveIfSourceFileWasNotDirtyBefore() throws CoreException, InvocationTargetException,
            InterruptedException {

        pmo.setValidFrom(new GregorianCalendar(1986, 4, 16));
        when(ipsSrcFile1.isDirty()).thenReturn(true);

        runnable.execute(monitor);

        verify(ipsSrcFile1, never()).save(true, monitor);
        verify(ipsSrcFile2).save(true, monitor);
    }

}
