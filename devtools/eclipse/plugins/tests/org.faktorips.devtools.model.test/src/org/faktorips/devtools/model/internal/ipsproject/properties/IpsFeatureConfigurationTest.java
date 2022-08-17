/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.internal.ipsproject.properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IpsFeatureConfigurationTest extends AbstractIpsPluginTest {

    @Test
    public void testGet_NotSet() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();

        String value = featureConfiguration.get("foo");

        assertThat(value, is(nullValue()));
    }

    @Test
    public void testGet() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
        featureConfiguration.set("foo", "bar");

        String value = featureConfiguration.get("foo");

        assertThat(value, is("bar"));
    }

    @Test
    public void testSet() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();

        featureConfiguration.set("foo", "bar");

        assertThat(featureConfiguration.get("foo"), is("bar"));
    }

    @Test
    public void testSet_Overwrites() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
        featureConfiguration.set("foo", "baz");

        featureConfiguration.set("foo", "bar");

        assertThat(featureConfiguration.get("foo"), is("bar"));
    }

    @Test
    public void testSet_ValueNull() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();

        featureConfiguration.set("foo", null);

        assertThat(featureConfiguration.get("foo"), is(nullValue()));
    }

    @Test
    public void testSet_ValueNull_Removes() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
        featureConfiguration.set("foo", "bar");

        featureConfiguration.set("foo", null);

        assertThat(featureConfiguration.get("foo"), is(nullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testSet_NameNull() {
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();

        featureConfiguration.set(null, "bar");
    }

    @Test
    public void testToXml() throws ParserConfigurationException {
        Document document = getDocumentBuilder().newDocument();
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
        featureConfiguration.set("foo", "bar");
        featureConfiguration.set("x", "y");

        Element featureConfigurationElement = featureConfiguration.toXml(document);

        assertThat(IpsFeatureConfiguration.FEATURE_CONFIGURATION_ELEMENT, is(featureConfigurationElement.getTagName()));
        NodeList propertyElements = featureConfigurationElement
                .getElementsByTagName(IpsFeatureConfiguration.PROPERTY_ELEMENT);
        assertThat(propertyElements.getLength(), is(2));
        assertThat(((Element)propertyElements.item(0)).getAttribute(IpsFeatureConfiguration.NAME_ATTRIBUTE), is("foo"));
        assertThat(((Element)propertyElements.item(0)).getAttribute(IpsFeatureConfiguration.VALUE_ATTRIBUTE),
                is("bar"));
        assertThat(((Element)propertyElements.item(1)).getAttribute(IpsFeatureConfiguration.NAME_ATTRIBUTE), is("x"));
        assertThat(((Element)propertyElements.item(1)).getAttribute(IpsFeatureConfiguration.VALUE_ATTRIBUTE), is("y"));
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        Element element = doc.getDocumentElement();
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();

        featureConfiguration.initFromXml(element);

        assertThat(featureConfiguration.get("foo"), is("bar"));
        assertThat(featureConfiguration.get("x"), is("y"));
        assertThat(featureConfiguration.get("baz"), is(nullValue()));
    }

    @Test
    public void testInitFromXml_ClearsProperties() {
        Document doc = getTestDocument();
        Element element = doc.getDocumentElement();
        IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
        featureConfiguration.set("foo", "baz");
        featureConfiguration.set("baz", "foobar");

        featureConfiguration.initFromXml(element);

        assertThat(featureConfiguration.get("foo"), is("bar"));
        assertThat(featureConfiguration.get("x"), is("y"));
        assertThat(featureConfiguration.get("baz"), is(nullValue()));
    }

}
