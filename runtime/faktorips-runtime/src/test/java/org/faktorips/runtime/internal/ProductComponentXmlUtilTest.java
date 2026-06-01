/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasKey;

import java.util.List;
import java.util.Map;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductComponentXmlUtilTest extends XmlAbstractTestCase {

    @Test
    public void testGetPropertyElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, Element> map = ProductComponentXmlUtil.getPropertyElements(genEl);
        assertThat(map.size(), is(6));

        assertThat(map.get("attribute1"), is(nullValue()));

        Element attr1El = map.get("@default_attribute1");
        assertThat(attr1El.getNodeName(), is("ConfiguredDefault"));
        assertThat(attr1El.getAttribute("attribute"), is("attribute1"));
        assertThat(attr1El.getAttribute("value"), is("2"));
        Element attr1VSEl = map.get("@valueSet_attribute1");
        assertThat(attr1VSEl.getNodeName(), is("ConfiguredValueSet"));
        assertThat(attr1VSEl.getAttribute("attribute"), is("attribute1"));

        Element attr2El = map.get("@default_attribute2");
        assertThat(attr2El.getNodeName(), is("ConfiguredDefault"));
        assertThat(attr2El.getAttribute("attribute"), is("attribute2"));
        assertThat(attr2El.getAttribute("value"), is("m"));
        Element attr2VSEl = map.get("@valueSet_attribute2");
        assertThat(attr2VSEl.getNodeName(), is("ConfiguredValueSet"));
        assertThat(attr2VSEl.getAttribute("attribute"), is("attribute2"));

        Element attr3El = map.get("attribute3");
        assertThat(attr3El.getNodeName(), is(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE));
        assertThat(attr3El.getAttribute("attribute"), is("attribute3"));
        assertThat(attr3El.getAttribute("value"), is("42"));

        Element tsuEl = map.get("rateTable");
        assertThat(tsuEl, is(notNullValue()));
    }

    @Test
    public void testGetLinkElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, List<Element>> map = ProductComponentXmlUtil.getLinkElements(genEl);
        assertThat(map.size(), is(4));

        List<Element> list1 = map.get("relation1");
        assertThat(list1.size(), is(2));
        Element rel1aEl = list1.get(0);
        assertThat(rel1aEl, is(notNullValue()));
        assertThat(rel1aEl.getNodeName(), is("Link"));
        assertThat(rel1aEl.getAttribute("target"), is("target1a"));

        Element rel1bEl = list1.get(1);
        assertThat(rel1bEl, is(notNullValue()));
        assertThat(rel1bEl.getNodeName(), is("Link"));
        assertThat(rel1bEl.getAttribute("target"), is("target1b"));

        List<Element> list2 = map.get("relation2");
        assertThat(list2.size(), is(1));
        Element rel2El = list2.get(0);
        assertThat(rel2El, is(notNullValue()));
        assertThat(rel2El.getNodeName(), is("Link"));
        assertThat(rel2El.getAttribute("target"), is("target2"));
    }

    @Test
    public void testGetPolicyLinkCardinalityElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, Element> map = ProductComponentXmlUtil.getPolicyLinkCardinalityElements(genEl);
        assertThat(map.size(), is(2));

        Element coverageEl = map.get("coverage");
        assertThat(coverageEl, is(notNullValue()));
        assertThat(coverageEl.getNodeName(), is("PolicyLinkCardinality"));
        assertThat(coverageEl.getAttribute("minCardinality"), is("1"));
        assertThat(coverageEl.getAttribute("maxCardinality"), is("5"));

        Element riderEl = map.get("rider");
        assertThat(riderEl, is(notNullValue()));
        assertThat(riderEl.getNodeName(), is("PolicyLinkCardinality"));
        assertThat(riderEl.getAttribute("minCardinality"), is("0"));
        assertThat(riderEl.getAttribute("maxCardinality"), is("*"));
    }

    @Test
    public void testGetAvailableFormulars() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, String> availableFormulas = ProductComponentXmlUtil.getAvailableFormulars(genEl);

        assertThat(availableFormulas.size(), is(3));
        assertThat(availableFormulas, hasKey("testFormula"));
        assertThat(availableFormulas.get("testFormula"), is(not(emptyOrNullString())));

        assertThat(availableFormulas, hasKey("emptyFormula"));
        assertThat(availableFormulas.get("emptyFormula"), is(emptyOrNullString()));

        assertThat(availableFormulas, hasKey("whitespaceFormula"));
        assertThat(availableFormulas.get("whitespaceFormula"), is(emptyOrNullString()));
    }
}
