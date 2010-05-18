/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.pds;

import java.io.InputStream;

import org.faktorips.runtime.IModificationChecker;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.w3c.dom.Element;

public interface IProductDataProvider extends IModificationChecker {

    public IReadonlyTableOfContents loadToc();

    public Element getProductCmptData(IProductCmptTocEntry tocEntry);

    public Element getProductCmptGenerationData(GenerationTocEntry tocEntry);

    public Element getTestcaseElement(ITestCaseTocEntry tocEntry);

    public InputStream getTableContentAsStream(IEnumContentTocEntry tocEntry);

    public InputStream getXmlAsStream(ITableContentTocEntry tocEntry);

}
