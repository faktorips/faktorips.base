/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 *
 * @author Markus Blum
 */
public class IpsPackageSortDefDeltaTest extends AbstractIpsPluginTest {

    private List packages = new ArrayList();
    private List sortDefs = new ArrayList();

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot rootPackage = ipsProject.getIpsPackageFragmentRoots()[0];

        packages.add(rootPackage.createPackageFragment("products.hausrat", true, null));
        List list = new ArrayList();
        list.add("unfall");
        list.add("kranken");
        list.add("haftpflicht");
        list.add("hausrat");
        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
        sortDef.setSegmentNames((String[])list.toArray(new String[list.size()]));
        sortDefs.add(sortDef);
        list.clear();

        packages.add(rootPackage.getIpsPackageFragment("products"));
        sortDefs.add(new IpsPackageFragmentDefaultSortDefinition());

        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);


        packages.add(rootPackage.createPackageFragment("products.kranken.leistungsarten", true, null));
        rootPackage.createPackageFragment("products.kranken.vertragsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.gruppenarten", true, null);

        list.add("leistungsarten");
        list.add("vertragsarten");
        list.add("gruppenarten");
        sortDef.setSegmentNames((String[])list.toArray(new String[list.size()]));
        sortDefs.add(sortDef);
        list.clear();

    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {

        packages.clear();
        sortDefs.clear();

        super.tearDown();
    }

    public void testFix() throws CoreException {

        // write empty delta

        IpsPackageSortDefDelta deltaEmpty = new IpsPackageSortDefDelta(new IpsPackageFragment[0] , new IIpsPackageFragmentSortDefinition[0]);
        deltaEmpty.fix();

        // write correct delta to file system
        IpsPackageSortDefDelta delta = new IpsPackageSortDefDelta((IIpsPackageFragment[])packages.toArray(new IIpsPackageFragment[packages.size()]), (IIpsPackageFragmentSortDefinition[])sortDefs.toArray(new IIpsPackageFragmentSortDefinition[sortDefs.size()]));
        delta.fix();
    }

    public void testCreate() {
        boolean checkException = false;
        try {
            IpsPackageSortDefDelta delta = new IpsPackageSortDefDelta(null, (IIpsPackageFragmentSortDefinition[])sortDefs.toArray(new IIpsPackageFragmentSortDefinition[sortDefs.size()]));

        } catch (Exception e) {
            checkException = true;
        }

        assertTrue(checkException);

        checkException = false;
        try {
            IpsPackageSortDefDelta delta = new IpsPackageSortDefDelta((IIpsPackageFragment[])packages.toArray(new IIpsPackageFragment[packages.size()]), null);

        } catch (Exception e) {
            checkException = true;
        }

        assertTrue(checkException);

        checkException = false;
        try {
            sortDefs.add(new IpsPackageFragmentDefaultSortDefinition());
            IpsPackageSortDefDelta delta = new IpsPackageSortDefDelta((IIpsPackageFragment[])packages.toArray(new IIpsPackageFragment[packages.size()]), null);

        } catch (Exception e) {
            checkException = true;
        }

        assertTrue(checkException);

        sortDefs.remove(sortDefs.size()-1);
        IpsPackageSortDefDelta delta = new IpsPackageSortDefDelta((IIpsPackageFragment[])packages.toArray(new IIpsPackageFragment[packages.size()]), (IIpsPackageFragmentSortDefinition[])sortDefs.toArray(new IIpsPackageFragmentSortDefinition[sortDefs.size()]));

        assertNotNull(delta);

        sortDefs.set(sortDefs.size()-1, null);
        delta = new IpsPackageSortDefDelta((IIpsPackageFragment[])packages.toArray(new IIpsPackageFragment[packages.size()]), (IIpsPackageFragmentSortDefinition[])sortDefs.toArray(new IIpsPackageFragmentSortDefinition[sortDefs.size()]));

        assertNotNull(delta);
}
}
