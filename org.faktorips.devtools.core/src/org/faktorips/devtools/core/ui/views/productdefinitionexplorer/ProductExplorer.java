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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import junit.framework.TestCase;

import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorer;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;

/**
 * A <code>ModelExplorer</code> that displays productdefinition projects along with all
 * contained <code>ProductCmpt</code>s and <code>TableContents</code>.
 * 
 * @author Stefan Widmaier
 */
public class ProductExplorer extends ModelExplorer {
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productDefinitionExplorer"; //$NON-NLS-1$
    
	public ProductExplorer() {
		super();
	}

	protected ModelExplorerConfiguration createConfig() {
		return new ModelExplorerConfiguration(new Class[] { ProductCmpt.class,
				TableContents.class, TestCase.class, TestCaseType.class}, new Class[0],
				ModelExplorerConfiguration.ALLOW_PRODUCTDEFINITION_PROJECTS |
				ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS |
				ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
	}

}
