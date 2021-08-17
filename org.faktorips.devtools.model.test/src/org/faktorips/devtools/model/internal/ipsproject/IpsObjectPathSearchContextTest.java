/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsObjectPathSearchContextTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsProject refProject;
    private IIpsObjectPathEntry entry;
    private IIpsObjectPathEntry entryFromRefProject;
    private IpsObjectPath path;
    private IpsObjectPath pathFromRefProject;
    private IpsObjectPathSearchContext searchContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("ipsProject");
        searchContext = new IpsObjectPathSearchContext(ipsProject);
        path = new IpsObjectPath(ipsProject);
        entry = new IpsProjectRefEntry(path);

        refProject = newIpsProject("refProject");
        pathFromRefProject = new IpsObjectPath(refProject);
        entryFromRefProject = new IpsProjectRefEntry(pathFromRefProject);
    }

    @Test
    public void testInitialCall() {
        assertTrue(searchContext.isInitialCall(entry));

        assertFalse(searchContext.isInitialCall(entryFromRefProject));
    }

    @Test
    public void testConsiderContentsOf_InitialCall() {
        boolean considerContents = searchContext.considerContentsOf(entry);

        assertTrue(considerContents);
    }

    @Test
    public void testConsiderContentsOf_NotInitialCallButReexportTrue() {
        boolean considerContents = searchContext.considerContentsOf(entryFromRefProject);

        assertTrue(considerContents);
    }

    @Test
    public void testConsiderContentsOf_NotInitialCallNoReexport() {
        entryFromRefProject.setReexported(false);

        boolean considerContents = searchContext.considerContentsOf(entryFromRefProject);

        assertFalse(considerContents);
    }

    @Test
    public void testConsiderContentsOf_EntryIsNull() {
        boolean considerContents = searchContext.considerContentsOf(null);

        assertFalse(considerContents);
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
        entryFromRefProject.setReexported(false);

        boolean visitAndConsiderContent = searchContext.visitAndConsiderContentsOf(entryFromRefProject);

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

    @Test
    public void testGetCopy() {
        IpsObjectPathSearchContext searchContextCopy = searchContext.getCopy();

        assertNotSame(searchContext, searchContextCopy);
    }

    @Test
    public void testGetCopy_visitSameEntry() {
        assertTrue(searchContext.getCopy().visit(entry));
        assertTrue(searchContext.visit(entry));
    }
}
