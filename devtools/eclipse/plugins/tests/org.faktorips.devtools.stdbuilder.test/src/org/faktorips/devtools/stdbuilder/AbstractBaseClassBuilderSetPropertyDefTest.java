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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.junit.Test;

public class AbstractBaseClassBuilderSetPropertyDefTest {

    @Test
    public void testValidateValue_valid() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getClassLoaderForJavaProject(any(ClassLoader.class))).thenReturn(getClass().getClassLoader());

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject,
                TestSubClass.class.getName());

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testValidateValue_usesSameClass() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getClassLoaderForJavaProject(any(ClassLoader.class))).thenReturn(getClass().getClassLoader());

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject,
                TestSuperClass.class.getName());

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testValidateValue_noValue() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject, "");

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testValidateValue_nullValue() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject, null);

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testValidateValue_noSubclass() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getClassLoaderForJavaProject(any(ClassLoader.class))).thenReturn(getClass().getClassLoader());

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject, String.class.getName());

        assertThat(message.getCode(), is(AbstractBaseClassBuilderSetPropertyDef.MSGCODE_NOT_SUBCLASS));
    }

    @Test
    public void testValidateValue_noClass() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getClassLoaderForJavaProject(any(ClassLoader.class))).thenReturn(getClass().getClassLoader());
        IJavaProject javaProject = mock(IJavaProject.class);
        when(ipsProject.getJavaProject()).thenReturn(Wrappers.wrap(javaProject).as(AJavaProject.class));

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject, "foobar");

        assertThat(message.getCode(), is(AbstractBaseClassBuilderSetPropertyDef.MSGCODE_CANT_LOAD_JAVA_CLASS));
    }

    @Test
    public void testValidateValue_noClassButAJavaType() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getClassLoaderForJavaProject(any(ClassLoader.class))).thenReturn(getClass().getClassLoader());
        IJavaProject javaProject = mock(IJavaProject.class);
        when(ipsProject.getJavaProject()).thenReturn(Wrappers.wrap(javaProject).as(AJavaProject.class));
        IType type = mock(IType.class);
        when(type.exists()).thenReturn(true);
        when(javaProject.findType("foo.Bar")).thenReturn(type);

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject, "foo.Bar");

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testValidateValue_noClassButANonExistantJavaType() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getClassLoaderForJavaProject(any(ClassLoader.class))).thenReturn(getClass().getClassLoader());
        IJavaProject javaProject = mock(IJavaProject.class);
        when(ipsProject.getJavaProject()).thenReturn(Wrappers.wrap(javaProject).as(AJavaProject.class));
        IType type = mock(IType.class);
        when(javaProject.findType("foo.Bar")).thenReturn(type);

        Message message = new TestBaseClassBuilderSetPropertyDef().validateValue(ipsProject, "foo.Bar");

        assertThat(message.getCode(), is(AbstractBaseClassBuilderSetPropertyDef.MSGCODE_CANT_LOAD_JAVA_CLASS));
    }

    private static class TestSuperClass {

    }

    private static class TestSubClass extends TestSuperClass {

    }

    private static class TestBaseClassBuilderSetPropertyDef extends AbstractBaseClassBuilderSetPropertyDef {

        public TestBaseClassBuilderSetPropertyDef() {
            Map<String, Object> properties = new HashMap<>();
            properties.put("type", "string");
            initialize(null, properties);
        }

        @Override
        protected Class<TestSuperClass> getRequiredSuperClass() {
            return TestSuperClass.class;
        }

    }

}
