/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertydef;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link GeneratedTagPropertyDef} and its inner classes {@link GeneratedTagPropertyDef.StartTag}
 * and {@link GeneratedTagPropertyDef.EndTag}.
 * <p>
 * Validates that {@code validateValue} correctly enforces the constraint that the start tag and end
 * tag must either both be set or both be empty, and that the error message arguments are ordered so
 * that {@code {0}} always refers to the start tag and {@code {1}} always refers to the end tag.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GeneratedTagPropertyDefTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsArtefactBuilderSet builderSet;

    @Mock
    private IIpsArtefactBuilderSetConfig config;

    private final GeneratedTagPropertyDef.StartTag startTag = new GeneratedTagPropertyDef.StartTag();
    private final GeneratedTagPropertyDef.EndTag endTag = new GeneratedTagPropertyDef.EndTag();

    @Before
    public void setUp() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "string"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("name", "generatedStartTag"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("label", ""); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("description", ""); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("defaultValue", ""); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("disableValue", ""); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("discreteValues", List.of()); //$NON-NLS-1$
        properties.put("jdkComplianceLevels", List.of()); //$NON-NLS-1$
        startTag.initialize(null, properties);
        endTag.initialize(null, properties);
    }

    @Test
    public void testStartTag_bothEmpty_returnsNull() {
        Message message = startTag.validateValue(ipsProject, "");

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testStartTag_bothSet_returnsNull() {
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(builderSet.getConfig()).thenReturn(config);
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATED_END_TAG)).thenReturn("END");

        Message message = startTag.validateValue(ipsProject, "START");

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testStartTag_onlyStartSet_returnsError() {
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(builderSet.getConfig()).thenReturn(config);
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATED_END_TAG)).thenReturn("");

        Message message = startTag.validateValue(ipsProject, "START");

        assertThat(message.getSeverity(), is(Message.ERROR));
        assertThat(message.getCode(), is(GeneratedTagPropertyDef.MSGCODE_ONLY_ONE_TAG_SET));
        assertThat(message.getText(), containsString("START"));
    }

    @Test
    public void testStartTag_onlyEndSet_returnsNull() {
        Message message = startTag.validateValue(ipsProject, "");

        assertThat(message, is(nullValue()));
    }

    @Test
    public void testEndTag_onlyStartSet_returnsNull() {
        Message message = endTag.validateValue(ipsProject, "");

        assertThat(message, is(nullValue()));
    }

    /**
     * Verifies that {@link GeneratedTagPropertyDef.EndTag#messageArgs} places the sibling
     * (start-tag) value at position {0} and the end-tag value at position {1}, so the generated
     * error message always reads "start tag ({0}) and end tag ({1})".
     */
    @Test
    public void testEndTag_onlyEndSet_returnsError_withCorrectArgOrder() {
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(builderSet.getConfig()).thenReturn(config);
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATED_START_TAG)).thenReturn("");

        Message message = endTag.validateValue(ipsProject, "END");

        assertThat(message.getSeverity(), is(Message.ERROR));
        assertThat(message.getCode(), is(GeneratedTagPropertyDef.MSGCODE_ONLY_ONE_TAG_SET));
        // {0} = siblingValue (start tag) = "", {1} = value (end tag) = "END"
        assertThat(message.getText(), containsString("end tag (END)"));
        assertThat(message.getText(), containsString("start tag ()"));
    }

    @Test
    public void testStartTag_nullValue_returnsNull() {
        Message message = startTag.validateValue(ipsProject, null);

        assertThat(message, is(nullValue()));
    }
}
