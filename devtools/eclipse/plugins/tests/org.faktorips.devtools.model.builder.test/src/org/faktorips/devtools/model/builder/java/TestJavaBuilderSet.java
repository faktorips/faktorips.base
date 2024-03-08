/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.abstracttest.builder.TestBuilderSetConfig;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;

public class TestJavaBuilderSet extends JavaBuilderSet {

    private TestBuilderSetConfig config;

    public TestJavaBuilderSet() {
        setId(TestIpsArtefactBuilderSet.ID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_PROPERTY_GENERATOR_LOCALE, Locale.GERMAN.getLanguage());
        config = new TestBuilderSetConfig(properties);
    }

    public void init() {
        initialize(config);
    }

    public void setProperty(String key, Object value) {
        config.getProperties().put(key, value);
    }

    @Override
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        fail("Not implemented in test builder");
        return null;
    }

    @Override
    public boolean usesUnifiedValueSets() {
        return true;
    }

    @Override
    protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() throws IpsException {
        return new LinkedHashMap<>();
    }

}
