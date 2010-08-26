/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

public abstract class PersistenceIpsTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;
    protected PolicyCmptType policyCmptType;

    public PersistenceIpsTest() {
        super();
    }

    public PersistenceIpsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newPersistentIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "Policy1");
    }

    public IIpsProject newPersistentIpsProject() throws CoreException {
        ipsProject = newIpsProject();
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPersistenceSupport(true);
        ipsProject.setProperties(props);
        return ipsProject;
    }

}
