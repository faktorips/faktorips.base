/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class InferTemplatePmoTest extends AbstractIpsPluginTest {

    @Test
    public void testGetEarliestValidFrom() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IProductCmptType type = newProductCmptType(ipsProject, "type");
        InferTemplatePmo pmo = new InferTemplatePmo();
        pmo.setIpsProject(ipsProject);
        List<IProductCmpt> cmpts = Lists.newArrayList();
        pmo.setProductCmptsToInferTemplateFrom(cmpts);

        assertThat(pmo.getEarliestValidFrom(), is(nullValue()));

        cmpts.add(newProductCmpt(type, "p1", 2015, Calendar.JANUARY, 1));

        assertThat(pmo.getEarliestValidFrom(), is(new GregorianCalendar(2015, Calendar.JANUARY, 1)));

        cmpts.add(newProductCmpt(type, "p2", 2015, Calendar.JUNE, 1));
        cmpts.add(newProductCmpt(type, "p3", 2014, Calendar.DECEMBER, 31));

        assertThat(pmo.getEarliestValidFrom(), is(new GregorianCalendar(2014, Calendar.DECEMBER, 31)));
    }

    private IProductCmpt newProductCmpt(IProductCmptType type, String qName, int year, int month, int day)
            throws CoreException {
        IProductCmpt cmpt = newProductCmpt(type, qName);
        cmpt.setValidFrom(new GregorianCalendar(year, month, day));
        return cmpt;
    }

}
