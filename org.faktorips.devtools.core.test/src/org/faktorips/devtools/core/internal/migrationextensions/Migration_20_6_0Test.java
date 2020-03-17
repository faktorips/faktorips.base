/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.Severity;
import org.junit.Before;
import org.junit.Test;

public class Migration_20_6_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Migration_20_6_0 migration;

    @Before
    public void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        migration = new Migration_20_6_0(ipsProject, "irrelevant");
    }

    @Test
    public void testProperties() throws InvocationTargetException, CoreException, InterruptedException {
        migration.migrate(new NullProgressMonitor());
        IIpsProjectProperties properties = ipsProject.getProperties();

        assertThat(properties.getDuplicateProductComponentSeverity(), is(Severity.WARNING));
    }

}
