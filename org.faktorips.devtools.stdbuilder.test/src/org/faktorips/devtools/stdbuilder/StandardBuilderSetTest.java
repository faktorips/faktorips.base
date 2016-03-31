/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.IntegerHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.junit.Test;

public class StandardBuilderSetTest extends AbstractIpsPluginTest {

    @Test
    public void testGetDatatypeHelper() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        StandardBuilderSet builderSet = new StandardBuilderSet();
        builderSet.setIpsProject(ipsProject);

        assertThat(builderSet.getDatatypeHelper(Datatype.INTEGER), is(instanceOf(IntegerHelper.class)));

        // project settings which "predefined datatypes" (i.e. those registered using the
        // datatypeDefinition extension point) are used in the projects can be ignored, helpers for
        // all registered datatypes can be returned
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName() });
        ipsProject.setProperties(props);
        assertThat(builderSet.getDatatypeHelper(Datatype.DECIMAL), is(instanceOf(DecimalHelper.class)));
        assertThat(builderSet.getDatatypeHelper(Datatype.MONEY), is(instanceOf(MoneyHelper.class)));
    }

}
