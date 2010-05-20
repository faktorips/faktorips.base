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

package org.faktorips.util;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;

/**
 * @author Jan Ortmann
 */
public class StringUtilTest extends TestCase {

    public StringUtilTest(String name) {
        super(name);
    }

    public void testQuote() {
        assertEquals("\"hello\"", StringUtil.quote("hello"));
        assertNull(StringUtil.quote(null));
    }

    public void testUnqualifiedClassName() {
        assertEquals("Test", StringUtil.unqualifiedName("com.Test"));
        assertEquals("Test", StringUtil.unqualifiedName("com.ips.Test"));
        assertEquals("Test", StringUtil.unqualifiedName("Test"));
    }

    public void testGetLine() {
        String lineSeparator = SystemUtils.LINE_SEPARATOR;
        String text = "blabla";
        assertEquals("blabla", StringUtil.getLine(text, 0, lineSeparator));

        text = "blabla" + lineSeparator + "2.line";

        assertEquals("blabla", StringUtil.getLine(text, 0, lineSeparator));
        assertEquals("2.line", StringUtil
                .getLine(text, 6 + SystemUtils.LINE_SEPARATOR.getBytes().length, lineSeparator));
    }

    public void testGetLines() {
        String[] result;

        result = StringUtil.getLines("blabla", SystemUtils.LINE_SEPARATOR);
        assertEquals(1, result.length);
        assertEquals("blabla", result[0]);

        result = StringUtil.getLines("blabla" + SystemUtils.LINE_SEPARATOR, SystemUtils.LINE_SEPARATOR);
        assertEquals(2, result.length);
        assertEquals("blabla", result[0]);
        assertEquals("", result[1]);

        result = StringUtil.getLines("blabla" + SystemUtils.LINE_SEPARATOR + "2.line", SystemUtils.LINE_SEPARATOR);
        assertEquals(2, result.length);
        assertEquals("blabla", result[0]);
        assertEquals("2.line", result[1]);

        result = StringUtil.getLines("blabla" + SystemUtils.LINE_SEPARATOR + "2.line" + SystemUtils.LINE_SEPARATOR,
                SystemUtils.LINE_SEPARATOR);
        assertEquals(3, result.length);
        assertEquals("blabla", result[0]);
        assertEquals("2.line", result[1]);
        assertEquals("", result[2]);
    }

    public void testGetSystemLineseparator() {
        assertEquals(System.getProperty("line.separator"), StringUtil.getSystemLineSeparator());
    }

    public void testGetFileExtension() {
        String ext = StringUtil.getFileExtension("readme.txt");
        assertEquals("txt", ext);

        ext = StringUtil.getFileExtension("readme");
        assertNull(ext);
    }

    public void testToCamlCase() {
        String testString = null;
        assertEquals("", StringUtil.toCamelCase(testString, true));
        assertEquals("", StringUtil.toCamelCase(testString, false));

        testString = "";
        assertEquals("", StringUtil.toCamelCase(testString, true));
        assertEquals("", StringUtil.toCamelCase(testString, false));

        testString = "asd";
        assertEquals("Asd", StringUtil.toCamelCase(testString, true));
        assertEquals("asd", StringUtil.toCamelCase(testString, false));

        testString = "ASD";
        assertEquals("Asd", StringUtil.toCamelCase(testString, true));
        assertEquals("asd", StringUtil.toCamelCase(testString, false));

        testString = "asd-def";
        assertEquals("AsdDef", StringUtil.toCamelCase(testString, true));
        assertEquals("asdDef", StringUtil.toCamelCase(testString, false));

        testString = "asd_def";
        assertEquals("AsdDef", StringUtil.toCamelCase(testString, true));
        assertEquals("asdDef", StringUtil.toCamelCase(testString, false));

        testString = "asd.def";
        assertEquals("AsdDef", StringUtil.toCamelCase(testString, true));
        assertEquals("asdDef", StringUtil.toCamelCase(testString, false));

        testString = "asd,def";
        assertEquals("AsdDef", StringUtil.toCamelCase(testString, true));
        assertEquals("asdDef", StringUtil.toCamelCase(testString, false));

        testString = "asd def";
        assertEquals("AsdDef", StringUtil.toCamelCase(testString, true));
        assertEquals("asdDef", StringUtil.toCamelCase(testString, false));

        testString = "asd-def qwer";
        assertEquals("AsdDefQwer", StringUtil.toCamelCase(testString, true));
        assertEquals("asdDefQwer", StringUtil.toCamelCase(testString, false));

        testString = "asd-1234";
        assertEquals("Asd1234", StringUtil.toCamelCase(testString, true));
        assertEquals("asd1234", StringUtil.toCamelCase(testString, false));
    }

    public void testCamelCaseToUnderscore() {
        String testString = null;
        assertEquals("", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "";
        assertEquals("", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "Asd";
        assertEquals("Asd", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("Asd", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "ASD";
        assertEquals("ASD", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("A_S_D", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "asd,def.ghi_jkl-mno pqr";
        assertEquals("asd_def_ghi_jkl_mno_pqr", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("asd_def_ghi_jkl_mno_pqr", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "Asd,.-_ Def";
        assertEquals("Asd_Def", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("Asd_Def", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "asDE-F1234";
        assertEquals("as_DE_F1234", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("as_D_E_F1234", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "a.,- b-cdEF";
        assertEquals("a_b_cd_EF", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("a_b_cd_E_F", StringUtil.camelCaseToUnderscore(testString, true));

        testString = "HausratVertrag2IJ";
        assertEquals("Hausrat_Vertrag2_IJ", StringUtil.camelCaseToUnderscore(testString, false));
        assertEquals("Hausrat_Vertrag2_I_J", StringUtil.camelCaseToUnderscore(testString, true));
    }

}
