/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.IntegerHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.stdbuilder.dthelper.LocalDateHelperVariant;
import org.junit.Test;

public class StandardBuilderSetTest extends AbstractStdBuilderTest {

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
        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();

        assertThat(builderSet.getDatatypeHelper(LocalDateDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.LocalDateHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalDateTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.LocalDateTimeHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.LocalTimeHelper.class)));
        assertThat(builderSet.getDatatypeHelper(MonthDayDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.joda.MonthDayHelper.class)));
    }

    @Test
    public void testGetDatatypeHelper_LocalDateHelpersWithJava8Configuration() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        setGeneratorProperty(ipsProject, StandardBuilderSet.CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT,
                LocalDateHelperVariant.JAVA8.toString().toLowerCase());

        assertThat(builderSet.getDatatypeHelper(LocalDateDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.LocalDateHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalDateTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.LocalDateTimeHelper.class)));
        assertThat(builderSet.getDatatypeHelper(LocalTimeDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.LocalTimeHelper.class)));
        assertThat(builderSet.getDatatypeHelper(MonthDayDatatype.DATATYPE),
                is(instanceOf(org.faktorips.codegen.dthelpers.java8.MonthDayHelper.class)));
    }

    @Test
    public void testGetConfig_AllowsAccessToProjectSettingForNamingConvention() {
        assertThat(
                builderSet.getConfig().getPropertyValueAsString(
                        StandardBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION),
                is(ipsProject.getProperties().getChangesOverTimeNamingConventionIdForGeneratedCode()));
    }

}
