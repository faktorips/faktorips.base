/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class IRuntimeRepositoryLookupTest {

    @Test
    void testByToC() throws IOException, ClassNotFoundException {
        var lookup = IRuntimeRepositoryLookup
                .byToC("org/faktorips/runtime/testrepository/faktorips-repository-toc.xml");

        var repository = lookup.getRuntimeRepository();
        var productComponent = repository.getProductComponent("home.HomeBasic");
        var policyComponent = productComponent.createPolicyComponent();

        byte[] serialized = serialize(policyComponent);
        var policyComponent2 = deserialize(serialized);

        assertThat(policyComponent2.getProductComponent(), is(sameInstance(productComponent)));
    }

    private byte[] serialize(IConfigurableModelObject obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }

    private IConfigurableModelObject deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (IConfigurableModelObject)ois.readObject();
        }
    }

}
