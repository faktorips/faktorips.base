package org.faktorips.fl;

import java.math.BigDecimal;
import java.util.Locale;

import org.faktorips.fl.functions.Abs;
import org.faktorips.fl.functions.And;
import org.faktorips.fl.functions.If;
import org.faktorips.fl.functions.Or;
import org.faktorips.fl.functions.Round;
import org.faktorips.fl.functions.WholeNumber;
import org.faktorips.util.LocalizedStringsSet;


/**
 * A <code>FunctionResolver</code> that supports Excel functions. The
 * functions are available in different languages.
 */
public class ExcelFunctionsResolver extends DefaultFunctionResolver {

    private LocalizedStringsSet localizedStrings;
    
    // the locale used for function names and descriptions.
    private Locale locale;
    
    /**
     * Creates a new resolver that contains a set of functions that are similiar
     * by name and argument list as those provided by Microsoft's Excel.
     * 
     * @param fctNameLocale The locale that determines the language of the function names.
     */
    public ExcelFunctionsResolver(Locale locale) {
        super();
        this.locale = locale;
        localizedStrings = new LocalizedStringsSet("org.faktorips.fl.ExcelFunctions", getClass().getClassLoader());
        add(new Abs(getFctName("abs"), getFctDescription("abs")));
        add(new If(getFctName("if"), getFctDescription("if")));
        add(new Or(getFctName("or"), getFctDescription("or")));
        add(new And(getFctName("and"), getFctDescription("and")));
        add(new Round(getFctName("round"), getFctDescription("round"), BigDecimal.ROUND_HALF_UP));
        add(new Round(getFctName("roundup"), getFctDescription("roundup"), BigDecimal.ROUND_UP));
        add(new Round(getFctName("rounddown"), getFctDescription("rounddown"), BigDecimal.ROUND_UP));
        add(new WholeNumber(getFctName("wholenumber"), getFctDescription("wholenumber")));
    }
    
    private String getFctName(String key) {
        return localizedStrings.getString(key + ".name", locale);
    }
    
    private String getFctDescription(String key) {
        return localizedStrings.getString(key + ".description", locale);
    }
    
    public String toString() {
        return "ExcelFunctionResolver";
    }
    
    class NameDescription {
        
        NameDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        String name;
        String description;
    }

}
