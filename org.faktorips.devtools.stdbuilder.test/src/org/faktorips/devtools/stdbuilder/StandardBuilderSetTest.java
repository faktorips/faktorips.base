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
import org.faktorips.abstracttest.builder.TestBuilderSetConfig;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.IntegerHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.devtools.core.internal.model.datatype.AbstractDateHelperFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.junit.Test;

public class StandardBuilderSetTest extends AbstractIpsPluginTest {

    @Test
    public void testGetDatatypeHelper_DefaultHelper() throws CoreException {
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

    @Test
    public void testGetDatatypeHelper_LocalDateHelpersWithDefaultConfiguration() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        StandardBuilderSet builderSet = new StandardBuilderSet();
        builderSet.setIpsProject(ipsProject);

        assertThat(builderSet.getDatatypeHelper(LocalDateDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.LocalDateHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalDateTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.LocalDateTimeHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.LocalTimeHelper.class)));
    }

    @Test
    public void testGetDatatypeHelper_LocalDateHelpersWithJava8Configuration() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        StandardBuilderSet builderSet = new StandardBuilderSet();
        builderSet.setIpsProject(ipsProject);

        // Although builderSet's IPS project is ipsProject, ipsProject.getIpsArtefactBuilderSet
        // returns a TestIpsArtefactBuilderSet. The factory to which helper creation is delegated
        // gets the IPS project as an argument. The factory obtains the builder set and the
        // configuration from the project, i.e. it will use the TestIpsArtefactBuilderSet (and its
        // configuration).
        //
        // By setting the config property for the helper variant to Java8 on the
        // TestIpsArtefactBuilderSet's config, the factory should return Java8 helpers
        //
        // Jacko says: "Because it's bad, it's bad..."
        TestIpsArtefactBuilderSet testBuilderSet = (TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        TestBuilderSetConfig testConfig = testBuilderSet.getConfig();
        testConfig.getProperties().put(AbstractDateHelperFactory.CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT, "java8");

        assertThat(builderSet.getDatatypeHelper(LocalDateDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.LocalDateHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalDateTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.LocalDateTimeHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.LocalTimeHelper.class)));
    }

}
