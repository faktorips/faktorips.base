/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import org.faktorips.devtools.htmlexport.generators.AbstractTextGenerator;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;

public class BaseFrameHtmlGenerator extends AbstractTextGenerator {
    private String title;
    private String colDefinition;
    private String rowsDefinition;
    private HtmlUtil htmlUtil = new HtmlUtil();

    public BaseFrameHtmlGenerator(String title, String colDefinition, String rowsDefinition) {
        super();
        this.title = title;
        this.colDefinition = colDefinition;
        this.rowsDefinition = rowsDefinition;
    }

    @Override
    public String generateText() {
        return htmlUtil.createDocFrame(title, colDefinition, rowsDefinition);
    }

}
