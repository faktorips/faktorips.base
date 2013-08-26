/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.fl;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.fl.parser.FlParser;
import org.faktorips.fl.parser.FlParserConstants;
import org.faktorips.fl.parser.FlParserTokenManager;
import org.faktorips.fl.parser.JavaCharStream;
import org.faktorips.fl.parser.ParseException;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.Token;
import org.faktorips.fl.parser.TokenMgrError;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.Message;

/**
 * A compiler to compile expressions. This abstract class is target language agnostic and is by
 * default implemented by the {@link JavaExprCompiler}.
 * <p>
 * This class is not thread safe!
 */
public abstract class ExprCompiler<T extends CodeFragment> {

    /**
     * The prefix for all compiler messages.
     */
    public static final String PREFIX = "FLC-"; //$NON-NLS-1$

    /**
     * An internal compiler error occurred during compilation. This message is generated if the
     * compiler fails because of a bug, there is nothing wrong with the expression.
     */
    public static final String INTERNAL_ERROR = PREFIX + "InternalError"; //$NON-NLS-1$

    /**
     * The expression has a syntax error, it does not confirm to the grammar.
     * <p>
     * Example: 2 + + b does not conform to the grammar.
     */
    public static final String SYNTAX_ERROR = PREFIX + "SyntaxError"; //$NON-NLS-1$

    /**
     * The expression has a lexical error.
     */
    public static final String LEXICAL_ERROR = PREFIX + "LexicalError"; //$NON-NLS-1$

    /**
     * The operation like +, *, - can't be done on the provided types.
     * <p>
     * Example: You can't multiply (*) two money values.
     */
    public static final String UNDEFINED_OPERATOR = PREFIX + "UndefinedOperator"; //$NON-NLS-1$

    /**
     * An identifier can't be resolved.
     * <p>
     * Example: a * 2
     * <p>
     * In the expression a is an identifier and it is possible that it can't be resolved by the
     * {IdentifierResolver} the compiler uses.
     */
    public static final String UNDEFINED_IDENTIFIER = PREFIX + "UndefinedIdentifier"; //$NON-NLS-1$

    /**
     * A qualifier can't be resolved.
     * <p>
     * Example: a.b(qual)
     * <p>
     * In the expression a and b are identifiers, qual is a qualifier which does not correspond to
     * any product component's unqualified name.
     */
    public static final String UNKNOWN_QUALIFIER = PREFIX + "UnknownQualifier"; //$NON-NLS-1$

    /**
     * An identifier is resolved to an association but the association's target can't be found.
     * <p>
     * Example: a.b.c * 2
     * <p>
     * In the expression a, b, c are identifiers. If the {IdentifierResolver} the compiler uses
     * can't identify the target types of associations a and b then an error message
     * {@link ExprCompiler#NO_ASSOCIATION_TARGET} is returned.
     */
    public static final String NO_ASSOCIATION_TARGET = PREFIX + "NoAssociationTarget"; //$NON-NLS-1$

    /**
     * An identifier is resolved to an 1to1 association but an index is also provided.
     * <p>
     * Example: a.b[0]
     * <p>
     * In the expression a and b are identifiers. The identifier b is resolved to a 1to1
     * association.
     */
    public static final String NO_INDEX_FOR_1TO1_ASSOCIATION = PREFIX + "NoIndexFor1to1Association"; //$NON-NLS-1$

    /**
     * An identifier is resolved to an association with index and qualifier.
     * <p>
     * Example: a.b[0]["pack.MyB"]
     * <p>
     * In the expression a and b are identifiers. The identifier b is resolved to an association.
     */
    public static final String INDEX_AND_QUALIFIER_CAN_NOT_BE_COMBINED = PREFIX + "IndexAndQualifierCanNotBeCombined"; //$NON-NLS-1$

    /**
     * The expression contains a call to an undefined function.
     */
    public static final String UNDEFINED_FUNCTION = PREFIX + "UndefinedFunction"; //$NON-NLS-1$

    /**
     * The expression contains a function call to a function with wrong argument types.
     */
    public static final String WRONG_ARGUMENT_TYPES = PREFIX + "WrongArgumentTypes"; //$NON-NLS-1$

    /**
     * The expression contains a literal that is identified by the parser as a money literal that
     * doesn't have a valid currency.
     */
    public static final String WRONG_MONEY_LITERAL = PREFIX + "Money"; //$NON-NLS-1$

    /**
     * The Expression calls a function, which could be resolved to several functions.
     */
    public static final String AMBIGUOUS_FUNCTION_CALL = PREFIX + "AmbiguousFunctionCall"; //$NON-NLS-1$

    /**
     * The expression contains a the expression <code>null</code>.
     */
    public static final String NULL_NOT_ALLOWED = PREFIX + "NullNotAllowed"; //$NON-NLS-1$

    private static final LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(
            "org.faktorips.fl.Messages", ExprCompiler.class.getClassLoader()); //$NON-NLS-1$

    // locale that is used for the locale dependant messages generated by the compiler
    private Locale locale;

    // Resolver for identifiers
    private IdentifierResolver<T> identifierResolver;

    // list of function resolvers
    private List<FunctionResolver<T>> functionResolvers = new ArrayList<FunctionResolver<T>>(2);

    // ConversionCodeGenerator that defines the implizit datatype conversion performed
    // by the compiler and can generate the appropriate Java sourcecode.
    private ConversionCodeGenerator<T> conversionCg;

    // Map containing a list of available binary operations per operator.
    private Map<String, List<BinaryOperation<T>>> binaryOperations = new HashMap<String, List<BinaryOperation<T>>>();

    // Map containing a list of available unary operations per operator.
    private Map<String, List<UnaryOperation<T>>> unaryOperations = new HashMap<String, List<UnaryOperation<T>>>();

    // the parser (generated by JavaCC)
    private FlParser parser;

    // true, if the expression's type should always be an object and not a primitive.
    private boolean ensureResultIsObject = true;

    private DatatypeHelperProvider datatypeHelperProvider;

    /**
     * Creates a new compiler. Messages returned by the compiler are generated using the
     * {@link Locale#getDefault() default locale}.
     * 
     * A {@link ConversionCodeGenerator}, {@link DatatypeHelperProvider} and
     * {@link IdentifierResolver} must be set via the corresponding setters.
     */
    public ExprCompiler() {
        this(Locale.getDefault());
    }

    /**
     * Creates a new compiler.
     * 
     * A {@link ConversionCodeGenerator}, {@link DatatypeHelperProvider} and
     * {@link IdentifierResolver} must be set via the corresponding setters.
     * 
     * @param locale The locale that is used to generate locale dependent messages.
     */
    public ExprCompiler(Locale locale) {
        this.locale = locale;
        parser = new FlParser(new ByteArrayInputStream("".getBytes())); //$NON-NLS-1$
        registerDefaults();
    }

    /**
     * Creates a new Compiler.
     * 
     * @param locale the {@link Locale} used to generate locale dependent messages
     * @param identifierResolver the {@link IdentifierResolver} used to convert formula language
     *            identifiers to target language code
     * @param conversionCg the {@link ConversionCodeGenerator} used to convert between
     *            {@link Datatype data types}
     * @param datatypeHelperProvider the {@link DatatypeHelperProvider} used to get
     *            {@link DatatypeHelper DatatypeHelpers} that generate code for the creation and
     *            processing of values in their respective {@link Datatype data types}
     */
    public ExprCompiler(Locale locale, IdentifierResolver<T> identifierResolver,
            ConversionCodeGenerator<T> conversionCg, DatatypeHelperProvider datatypeHelperProvider) {
        this(locale);
        this.identifierResolver = identifierResolver;
        this.conversionCg = conversionCg;
        this.datatypeHelperProvider = datatypeHelperProvider;
    }

    /**
     * Verifies if the provided string is a valid identifier according to the identifier definition
     * of the Fl-Parser.
     */
    public static final boolean isValidIdentifier(String identifier) {
        JavaCharStream s = new JavaCharStream(new StringReader(identifier));
        FlParserTokenManager manager = new FlParserTokenManager(s);
        Token token = manager.getNextToken();
        if (token.kind == FlParserConstants.IDENTIFIER) {
            if (manager.getNextToken().kind == 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Registers the default operations.
     */
    protected abstract void registerDefaults();

    /**
     * Registers the binary operation.
     */
    public void register(BinaryOperation<T> op) {
        List<BinaryOperation<T>> operatorOperations = binaryOperations.get(op.getOperator());
        if (operatorOperations == null) {
            operatorOperations = new ArrayList<BinaryOperation<T>>(20);
            binaryOperations.put(op.getOperator(), operatorOperations);
        }
        operatorOperations.add(op);
        op.setCompiler(this);
    }

    /**
     * Registers the unary operation.
     */
    public void register(UnaryOperation<T> op) {
        List<UnaryOperation<T>> operatorOperations = unaryOperations.get(op.getOperator());
        if (operatorOperations == null) {
            operatorOperations = new ArrayList<UnaryOperation<T>>(20);
            unaryOperations.put(op.getOperator(), operatorOperations);
        }
        operatorOperations.add(op);
    }

    /**
     * Sets the {@link BinaryOperation BinaryOperations} the compiler uses. Overwrites all
     * operations previously registered.
     * 
     * @throws IllegalArgumentException if operations is {@code null}.
     */
    public void setBinaryOperations(BinaryOperation<T>[] operations) {
        ArgumentCheck.notNull(operations);
        binaryOperations = new HashMap<String, List<BinaryOperation<T>>>();
        for (BinaryOperation<T> operation : operations) {
            register(operation);
        }
    }

    /**
     * Sets the {@link UnaryOperation UnaryOperations} the compiler uses. Overwrites all operations
     * previously registered.
     * 
     * @throws IllegalArgumentException if operations is {@code null}.
     */
    public void setUnaryOperations(UnaryOperation<T>[] operations) {
        ArgumentCheck.notNull(operations);
        unaryOperations = new HashMap<String, List<UnaryOperation<T>>>();
        for (UnaryOperation<T> operation : operations) {
            register(operation);
        }
    }

    /**
     * Returns the compiler's {@code EnsureResultIsObject} property.
     * 
     * @see #setEnsureResultIsObject(boolean)
     */
    public boolean getEnsureResultIsObject() {
        return this.ensureResultIsObject;
    }

    /**
     * Sets the compiler's {@code EnsureResultIsObject} property. If set to {@code true}, the
     * compiler will check if an expression's type is a Java primitive before returning the result.
     * If the type is a primitive the compiler will convert it to the appropriate wrapper class.
     * E.g. the expression <code>2+4</code> is of type primitive int. If this property is set to
     * true the compiler would wrap the resulting source code with a <code>Integer(..)</code>.
     */
    public void setEnsureResultIsObject(boolean newValue) {
        this.ensureResultIsObject = newValue;
    }

    /**
     * Returns the resolver the compiler uses to resolve identifiers.
     */
    public IdentifierResolver<T> getIdentifierResolver() {
        return identifierResolver;
    }

    /**
     * Sets the {@link IdentifierResolver} the compiler uses to resolve identifiers.
     * 
     * @throws IllegalArgumentException if resolver is null.
     */
    public void setIdentifierResolver(IdentifierResolver<T> resolver) {
        ArgumentCheck.notNull(resolver);
        this.identifierResolver = resolver;
    }

    /**
     * Returns the {@link ConversionCodeGenerator} that defines the compiler's implicit conversions,
     * e.g. convert a primitive int to an Integer.
     */
    public ConversionCodeGenerator<T> getConversionCodeGenerator() {
        return conversionCg;
    }

    /**
     * Sets the {@link ConversionCodeGenerator} that the compiler uses for implicit conversions,
     * e.g. convert a primitive int to an Integer.
     * 
     * @throws IllegalArgumentException if ccg is null.
     */
    public void setConversionCodeGenerator(ConversionCodeGenerator<T> ccg) {
        ArgumentCheck.notNull(ccg);
        conversionCg = ccg;
    }

    /**
     * Returns the {@link Locale} the compiler uses for it's {@link Message messages}.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the {@link Locale} the compiler uses to generate it's {@link Message messages}.
     * 
     * @throws IllegalArgumentException if locale is null.
     */
    public void setLocale(Locale locale) {
        ArgumentCheck.notNull(locale);
        this.locale = locale;
    }

    /**
     * Adds the function resolver to the ones used by the compiler to resolve function calls in
     * expressions.
     * 
     * @throws IllegalArgumentException if fctResolver is null.
     */
    public void add(FunctionResolver<T> fctResolver) {
        ArgumentCheck.notNull(fctResolver);
        functionResolvers.add(fctResolver);
        FlFunction<T>[] functions = fctResolver.getFunctions();
        for (FlFunction<T> function : functions) {
            function.setCompiler(this);
        }
    }

    /**
     * Removes the function resolver from the ones used by the compiler to resolve function calls.
     * If the resolver hasn't been added before, this method does nothing.
     * 
     * @throws IllegalArgumentException if fctResolver is null.
     */
    public void remove(FunctionResolver<T> fctResolver) {
        ArgumentCheck.notNull(fctResolver);
        functionResolvers.remove(fctResolver);
    }

    /**
     * Returns an iterator to access the added function resolvers.
     */
    Iterator<FunctionResolver<T>> getFunctionResolvers() {
        return functionResolvers.iterator();
    }

    /**
     * Return the functions supported by the compiler.
     */
    public FlFunction<T>[] getFunctions() {
        List<FlFunction<T>> functions = new ArrayList<FlFunction<T>>();
        for (Iterator<FunctionResolver<T>> it = getFunctionResolvers(); it.hasNext();) {
            FunctionResolver<T> resolver = it.next();
            FlFunction<T>[] resolverFunctions = resolver.getFunctions();
            for (FlFunction<T> resolverFunction : resolverFunctions) {
                functions.add(resolverFunction);
            }
        }
        @SuppressWarnings("unchecked")
        FlFunction<T>[] flFunctions = new FlFunction[functions.size()];
        return functions.toArray(flFunctions);
    }

    /**
     * Returns a Set of ambiguous {@link FlFunction FlFunctions}, which the parser could not
     * differentiate.
     * <p>
     * Maybe the rolename of a table equals the qualified name of a table structure in the root
     * package.
     */
    public LinkedHashSet<FlFunction<T>> getAmbiguousFunctions(final FlFunction<T>[] functions) {

        LinkedHashSet<FlFunction<T>> ambiguousFunctions = new LinkedHashSet<FlFunction<T>>();
        for (int i = 0; i < functions.length; i++) {
            FlFunction<T> flFunction = functions[i];

            for (int j = i + 1; j < functions.length; j++) {
                FlFunction<T> comparedFlFunction = functions[j];

                if (flFunction.isSame(comparedFlFunction)) {
                    ambiguousFunctions.add(comparedFlFunction);
                    ambiguousFunctions.add(flFunction);
                }
            }
        }
        return ambiguousFunctions;
    }

    /**
     * Compiles the given expression string into {@link CodeFragment source code}. If the
     * compilation is not successful, the {@link CompilationResult result} contains {@link Message
     * messages} that describe the error/problem that has occurred. If the compilation is
     * successful, the result contains {@link CodeFragment source code} that represents the
     * expression along with the expression's {@link Datatype}. In this case the result does not
     * contain any {@link Message#ERROR error} messages, but may contain {@link Message#WARNING
     * warnings} or {@link Message#INFO informations}.
     */
    public CompilationResult<T> compile(String expr) {
        SimpleNode rootNode;
        // parse the expression
        try {
            rootNode = parse(expr);
        } catch (ParseException pe) {
            return parseExceptionToResult(pe);
            // CSOFF: IllegalCatch
        } catch (Exception pe) {
            // CSON: IllegalCatch
            return newCompilationResultImpl(Message.newError(INTERNAL_ERROR,
                    LOCALIZED_STRINGS.getString(INTERNAL_ERROR, getLocale())));
        } catch (TokenMgrError e) {
            String text = LOCALIZED_STRINGS.getString(LEXICAL_ERROR, getLocale(), e.getMessage());
            return newCompilationResultImpl(Message.newError(LEXICAL_ERROR, text));
        }
        // parse ok, generate the sourcecode via the visitor visiting the parse tree
        AbstractCompilationResult<T> result;
        try {
            ParseTreeVisitor<T> visitor = newParseTreeVisitor();
            @SuppressWarnings("unchecked")
            AbstractCompilationResult<T> compilationResult = (AbstractCompilationResult<T>)rootNode.jjtAccept(visitor,
                    null);
            result = compilationResult;
            // CSOFF: IllegalCatch
        } catch (Exception pe) {
            // CSON: IllegalCatch
            return newCompilationResultImpl(Message.newError(INTERNAL_ERROR,
                    LOCALIZED_STRINGS.getString(INTERNAL_ERROR, getLocale())));
        }
        if (result.failed()) {
            return result;
        }
        try {
            Datatype resultType = result.getDatatype();
            if (!getEnsureResultIsObject() || !resultType.isPrimitive()) {
                return result;
            }
            // convert primitive to wrapper object
            T converted = convertPrimitiveToWrapper(resultType, result.getCodeFragment());
            AbstractCompilationResult<T> finalResult = newCompilationResultImpl(converted,
                    ((ValueDatatype)resultType).getWrapperType());
            finalResult.addIdentifiersUsed(result.getIdentifiersUsedAsSet());
            return finalResult;
            // CSOFF: IllegalCatch
        } catch (Exception pe) {
            // CSON: IllegalCatch
            return newCompilationResultImpl(Message.newError(INTERNAL_ERROR,
                    LOCALIZED_STRINGS.getString(INTERNAL_ERROR, getLocale())));
        }
    }

    protected abstract T convertPrimitiveToWrapper(Datatype resultType, T codeFragment);

    protected abstract ParseTreeVisitor<T> newParseTreeVisitor();

    protected abstract AbstractCompilationResult<T> newCompilationResultImpl(Message message);

    protected abstract AbstractCompilationResult<T> newCompilationResultImpl(T sourcecode, Datatype datatype);

    protected SimpleNode parse(String expr) throws ParseException {
        parser.ReInit(new StringReader(expr));
        return parser.start();
    }

    protected CompilationResult<T> parseExceptionToResult(ParseException e) {
        String expected = ""; //$NON-NLS-1$
        for (int[] expectedTokenSequence : e.expectedTokenSequences) {
            expected += e.tokenImage[expectedTokenSequence[0]] + " "; //$NON-NLS-1$
        }
        Object[] replacements = new Object[] { e.currentToken.next.toString(),
                new Integer(e.currentToken.next.beginLine), new Integer(e.currentToken.next.beginColumn), expected };
        return newCompilationResultImpl(Message.newError(SYNTAX_ERROR,
                LOCALIZED_STRINGS.getString(SYNTAX_ERROR, getLocale(), replacements)));
    }

    BinaryOperation<T>[] getBinaryOperations(String operator) {
        List<BinaryOperation<T>> operatorOperations = binaryOperations.get(operator);
        if (operatorOperations == null) {
            @SuppressWarnings("unchecked")
            BinaryOperation<T>[] binaryOperations = new BinaryOperation[0];
            return binaryOperations;
        }
        @SuppressWarnings("unchecked")
        BinaryOperation<T>[] binaryOperations = new BinaryOperation[operatorOperations.size()];
        return operatorOperations.toArray(binaryOperations);
    }

    UnaryOperation<T>[] getUnaryOperations(String operator) {
        List<UnaryOperation<T>> operatorOperations = unaryOperations.get(operator);
        if (operatorOperations == null) {
            @SuppressWarnings("unchecked")
            UnaryOperation<T>[] unaryOperations = new UnaryOperation[0];
            return unaryOperations;
        }
        @SuppressWarnings("unchecked")
        UnaryOperation<T>[] unaryOperations = new UnaryOperation[operatorOperations.size()];
        return operatorOperations.toArray(unaryOperations);
    }

    /**
     * @return Returns the {@link DatatypeHelperProvider}.
     */
    public DatatypeHelperProvider getDatatypeHelperProvider() {
        return datatypeHelperProvider;
    }

    /**
     * @param provider The {@link DatatypeHelperProvider} to set.
     */
    public void setDatatypeHelperProvider(DatatypeHelperProvider provider) {
        this.datatypeHelperProvider = provider;
    }

    /**
     * Returns the {@link DatatypeHelper code generation helper} for the given type or {@code null}
     * if no helper is available.
     */
    public DatatypeHelper getDatatypeHelper(Datatype type) {
        if (datatypeHelperProvider == null || type == null) {
            return null;
        }
        return datatypeHelperProvider.getDatatypeHelper(type);
    }

    public static LocalizedStringsSet getLocalizedStrings() {
        return LOCALIZED_STRINGS;
    }
}
