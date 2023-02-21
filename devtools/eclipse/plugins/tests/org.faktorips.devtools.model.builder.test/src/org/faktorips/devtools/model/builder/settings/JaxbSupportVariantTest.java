/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.builder.settings;

import static org.faktorips.devtools.model.builder.JaxbSupportVariant.ClassicJAXB;
import static org.faktorips.devtools.model.builder.JaxbSupportVariant.JakartaXmlBinding;
import static org.faktorips.devtools.model.builder.JaxbSupportVariant.None;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.junit.Test;

public class JaxbSupportVariantTest {

    @Test
    public void testOf() {
        assertThat(JaxbSupportVariant.of(null), is(None));
        assertThat(JaxbSupportVariant.of(""), is(None));
        assertThat(JaxbSupportVariant.of("  "), is(None));
        assertThat(JaxbSupportVariant.of("false"), is(None));
        assertThat(JaxbSupportVariant.of("FALSE"), is(None));
        assertThat(JaxbSupportVariant.of("JakartaXmlBinding"), is(JakartaXmlBinding));
        assertThat(JaxbSupportVariant.of(" JakartaXmlBinding "), is(JakartaXmlBinding));
        assertThat(JaxbSupportVariant.of("jakartaxmlbinding"), is(JakartaXmlBinding));
        assertThat(JaxbSupportVariant.of("ClassicJAXB"), is(ClassicJAXB));
        assertThat(JaxbSupportVariant.of("classicJaxb"), is(ClassicJAXB));
        assertThat(JaxbSupportVariant.of("true"), is(ClassicJAXB));
        assertThat(JaxbSupportVariant.of("FooBar"), is(ClassicJAXB));
    }

}
