/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DefaultRuntimeIdInitStrategyTest extends IpsPluginTest {
	
	DefaultTestContent content;
	DefaultRuntimeIdInitStrategy strategy;
	String prefix;
	
	/**
	 * {@inheritDoc}
	 */
    protected void setUp() throws Exception {
        super.setUp();
        content = new DefaultTestContent();
        strategy = new DefaultRuntimeIdInitStrategy();
        prefix = content.getProject().getIpsProject().getRuntimeIdPrefix();
    }
   
    public void testGetRuntimeId() throws Exception {
    	IProductCmpt cmpt = newProductCmpt(content.getProject(), "TestProductCmpt");
    	
    	String id = strategy.getRuntimeId(cmpt);
    	assertEquals(prefix + cmpt.getName() + "1", id);
    	
    	IProductCmpt cmpt2 = newProductCmpt(content.getProject(), "TestProductCmpt1");
    	id = strategy.getRuntimeId(cmpt2);
    	assertEquals(prefix + cmpt.getName() + "11", id);
    	
    	cmpt2 = newProductCmpt(content.getProject(), "TestProductCmpt2");
    	id = strategy.getRuntimeId(cmpt);
    	assertEquals(prefix + cmpt.getName() + "3", id);
    }
}
