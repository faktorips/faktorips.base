/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsObjectPathSearchContextTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsObjectPathEntry entry;
    private IpsObjectPath path;
    private IpsObjectPathSearchContext searchContext;

    @Override
    @Before
    public void setUp() throws CoreException {
        ipsProject = newIpsProject("ipsProject");
        searchContext = new IpsObjectPathSearchContext();
        path = new IpsObjectPath(ipsProject);
        entry = new IpsProjectRefEntry(path);
    }

    @Test
    public void testInitialCall() {
        assertTrue(searchContext.isInitialCall());

        searchContext.setSubsequentCall();

        assertFalse(searchContext.isInitialCall());
    }

    @Test
    public void testConsiderContentsOf_InitialCall() {
        boolean considerContents = searchContext.considerContentsOf(entry);

        assertTrue(considerContents);
    }

    @Test
    public void testConsiderContentsOf_NotInitialCallButReexportTrue() {
        searchContext.setSubsequentCall();

        boolean considerContentsOf = searchContext.considerContentsOf(entry);

        assertTrue(considerContentsOf);
    }

    @Test
    public void testConsiderContentsOf_NotInitialCallNoReexport() {
        searchContext.setSubsequentCall();
        entry.setReexported(false);

        boolean considerContentsOf = searchContext.considerContentsOf(entry);

        assertFalse(considerContentsOf);
    }

    @Test
    public void testVisit() {
        boolean visit = searchContext.visit(entry);
        assertTrue(visit);

        boolean visitAgain = searchContext.visit(entry);
        assertFalse(visitAgain);
    }

    @Test
    public void testVisitAndConsiderContentsOf_NoReexport() {
        searchContext.setSubsequentCall();
        entry.setReexported(false);

        boolean visitAndConsiderContent = searchContext.visitAndConsiderContentsOf(entry);

        assertFalse(visitAndConsiderContent);
    }

    @Test
    public void testVisitAndConsiderContentsOf_AlreadyVisited() {
        searchContext.visit(entry);

        boolean visitAndConsiderContent = searchContext.visitAndConsiderContentsOf(entry);

        assertFalse(visitAndConsiderContent);
    }

    @Test
    public void testVisitAndConsiderContentsOf() {

        boolean visitAndConsiderContent = searchContext.visitAndConsiderContentsOf(entry);

        assertTrue(visitAndConsiderContent);
    }
}
