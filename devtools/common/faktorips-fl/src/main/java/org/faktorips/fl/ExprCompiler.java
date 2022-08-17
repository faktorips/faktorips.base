/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.fl.parser.FlParser;
import org.faktorips.fl.parser.FlParserConstants;
import org.faktorips.fl.parser.FlParserTokenManager;
import org.faktorips.fl.parser.JavaCharStream;
import org.faktorips.fl.parser.ParseException;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.Token;
import org.faktorips.fl.parser.TokenMgrError;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

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
     * The expression contains a data type that can not be instantiated by this compiler.
     */
    public static final String DATATYPE_CREATION_ERROR = PREFIX + "DatatypeCreationError"; //$NON-NLS-1$

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
    private List<FunctionResolver<T>> functionResolvers = new ArrayList<>(2);

    // ConversionCodeGenerator that defines the implizit datatype conversion performed
    // by the compiler and can generate the appropriate Java sourcecode.
    private ConversionCodeGenerator<T> conversionCg;

    // Map containing a list of available binary operations per operator.
    private Map<String, List<BinaryOperation<T>>> binaryOperations = new HashMap<>();

    // Map containing a list of available unary operations per operator.
    private Map<String, List<UnaryOperation<T>>> unaryOperations = new HashMap<>();

    // the parser (generated by JavaCC)
    private FlParser parser;

    // true, if the expression's type should always be an object and not a primitive.
    private boolean ensureResultIsObject = true;

    private DatatypeHelperProvider<T> datatypeHelperProvider;

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
            ConversionCodeGenerator<T> conversionCg, DatatypeHelperProvider<T> datatypeHelperProvider) {
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
        List<BinaryOperation<T>> operatorOperations = binaryOperations.computeIfAbsent(op.getOperator(),
                $ -> new ArrayList<>(20));
        operatorOperations.add(op);
        op.setCompiler(this);
    }

    /**
     * Registers the unary operation.
     */
    public void register(UnaryOperation<T> op) {
        List<UnaryOperation<T>> operatorOperations = unaryOperations.computeIfAbsent(op.getOperator(),
                $ -> new ArrayList<>(20));
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
        binaryOperations = new HashMap<>();
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
        unaryOperations = new HashMap<>();
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
     * Return the functions supported by the compiler.
     */
    public FlFunction<T>[] getFunctions() {
        List<FlFunction<T>> functions = new ArrayList<>();
        for (FunctionResolver<T> resolver : functionResolvers) {
            FlFunction<T>[] resolverFunctions = resolver.getFunctions();
            List<FlFunction<T>> functionsOfResolver = Arrays.asList(resolverFunctions);
            Collections.sort(functionsOfResolver, new FunctionComparator());
            functions.addAll(functionsOfResolver);
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

        LinkedHashSet<FlFunction<T>> ambiguousFunctions = new LinkedHashSet<>();
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
            pe.printStackTrace();
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
            pe.printStackTrace();
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
            return newCompilationResultImpl(converted,
                    ((ValueDatatype)resultType).getWrapperType());
            // CSOFF: IllegalCatch
        } catch (RuntimeException pe) {
            // CSON: IllegalCatch
            pe.printStackTrace();
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
        Object[] replacements = { e.currentToken.next.toString(),
                Integer.valueOf(e.currentToken.next.beginLine), Integer.valueOf(e.currentToken.next.beginColumn),
                expected };
        return newCompilationResultImpl(Message.newError(SYNTAX_ERROR,
                LOCALIZED_STRINGS.getString(SYNTAX_ERROR, getLocale(), replacements)));
    }

    BinaryOperation<T>[] getBinaryOperations(String operator) {
        List<BinaryOperation<T>> operatorOperations = binaryOperations.get(operator);
        if (operatorOperations == null) {
            return newBinaryOperation();
        }
        @SuppressWarnings("unchecked")
        BinaryOperation<T>[] binaryOperationsArray = new BinaryOperation[operatorOperations.size()];
        return operatorOperations.toArray(binaryOperationsArray);
    }

    @SuppressWarnings("unchecked")
    private BinaryOperation<T>[] newBinaryOperation() {
        return new BinaryOperation[0];
    }

    UnaryOperation<T>[] getUnaryOperations(String operator) {
        List<UnaryOperation<T>> operatorOperations = unaryOperations.get(operator);
        if (operatorOperations == null) {
            return newUnaryOperation();
        }
        @SuppressWarnings("unchecked")
        UnaryOperation<T>[] unaryOperationsArray = new UnaryOperation[operatorOperations.size()];
        return operatorOperations.toArray(unaryOperationsArray);
    }

    @SuppressWarnings("unchecked")
    private UnaryOperation<T>[] newUnaryOperation() {
        return new UnaryOperation[0];
    }

    /**
     * @return Returns the {@link DatatypeHelperProvider}.
     */
    public DatatypeHelperProvider<T> getDatatypeHelperProvider() {
        return datatypeHelperProvider;
    }

    /**
     * @param provider The {@link DatatypeHelperProvider} to set.
     */
    public void setDatatypeHelperProvider(DatatypeHelperProvider<T> provider) {
        this.datatypeHelperProvider = provider;
    }

    /**
     * Returns the {@link DatatypeHelper code generation helper} for the given type or {@code null}
     * if no helper is available.
     */
    public BaseDatatypeHelper<T> getDatatypeHelper(Datatype type) {
        if (datatypeHelperProvider == null || type == null) {
            return null;
        }
        return datatypeHelperProvider.getDatatypeHelper(type);
    }

    public static LocalizedStringsSet getLocalizedStrings() {
        return LOCALIZED_STRINGS;
    }

    public CompilationResult<T> getMatchingFunctionUsingConversion(CompilationResult<T>[] argResults,
            Datatype[] argTypes,
            String fctName) {
        FlFunction<T> function = null;
        boolean functionFoundByName = false;
        FlFunction<T>[] functions = getFunctions();
        LinkedHashSet<FlFunction<T>> ambiguousFunctions = getAmbiguousFunctions(functions);

        for (FlFunction<T> function2 : functions) {
            if (function2.match(fctName, argTypes)) {
                if (ambiguousFunctions.contains(function2)) {
                    return createAmbiguousFunctionCompilationResultImpl(function2);
                }
                return function2.compile(argResults);
            } else if (function2.matchUsingConversion(fctName, argTypes, getConversionCodeGenerator())) {
                function = function2;
            } else if (!functionFoundByName && function2.getName().equals(fctName)) {
                functionFoundByName = true;
            }
        }

        if (function != null) {
            if (ambiguousFunctions.contains(function)) {
                return createAmbiguousFunctionCompilationResultImpl(function);
            }
            return function.compile(convert(function, argResults));
        }

        return createErrorCompilationResult(argResults, fctName, functionFoundByName);
    }

    public CompilationResult<T> getMatchingFunctionUsingConversionSingleArgument(AbstractCompilationResult<T> argResult,
            Datatype argTypes,
            String fctName) {
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T>[] argResults = new AbstractCompilationResult[] { argResult };
        return getMatchingFunctionUsingConversion(argResults, new Datatype[] { argTypes }, fctName);
    }

    private CompilationResult<T> createErrorCompilationResult(CompilationResult<T>[] argResults,
            String fctName,
            boolean functionFoundByName) {
        // if the function name is defined but the argument types are wrong
        // generate a ExprCompiler.WRONG_ARGUMENT_TYPES error message.
        if (functionFoundByName) {
            Object[] replacements = new String[] { fctName, argTypesToString(argResults) };
            String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.WRONG_ARGUMENT_TYPES, getLocale(),
                    replacements);
            return newCompilationResultImpl(Message.newError(ExprCompiler.WRONG_ARGUMENT_TYPES, text));
        }

        // The function is undefined. Generate a ExprCompiler.UNDEFINED_FUNCTION error message
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_FUNCTION, getLocale(),
                fctName);
        return newCompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_FUNCTION, text));
    }

    private AbstractCompilationResult<T> createAmbiguousFunctionCompilationResultImpl(FlFunction<T> flFunction) {
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.AMBIGUOUS_FUNCTION_CALL, getLocale(),
                flFunction.getName());

        return newCompilationResultImpl(Message.newError(ExprCompiler.AMBIGUOUS_FUNCTION_CALL, text));
    }

    private String argTypesToString(CompilationResult<T>[] results) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.length; i++) {
            if (i > 0) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append(results[i].getDatatype().getName());
        }
        return sb.toString();
    }

    private CompilationResult<T>[] convert(FlFunction<T> flFunction, CompilationResult<T>[] argResults) {
        ConversionCodeGenerator<T> conversionCodeGenerator = getConversionCodeGenerator();
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T>[] convertedArgs = new AbstractCompilationResult[argResults.length];
        for (int i = 0; i < argResults.length; i++) {

            Datatype functionDatatype = flFunction.hasVarArgs() ? flFunction.getArgTypes()[0]
                    : flFunction
                            .getArgTypes()[i];
            if (functionDatatype instanceof AnyDatatype) {
                convertedArgs[i] = (AbstractCompilationResult<T>)argResults[i];
            } else {
                T fragment = conversionCodeGenerator.getConversionCode(argResults[i].getDatatype(), functionDatatype,
                        argResults[i].getCodeFragment());
                convertedArgs[i] = newCompilationResultImpl(fragment, functionDatatype);
                convertedArgs[i].addMessages(argResults[i].getMessages());
            }
        }
        return convertedArgs;
    }

    public CompilationResult<T> getBinaryOperation(String operator,
            AbstractCompilationResult<T> lhsResult,
            AbstractCompilationResult<T> rhsResult) {
        BinaryOperation<T> operation = null;
        BinaryOperation<T>[] operations = getBinaryOperations(operator);
        for (BinaryOperation<T> operation2 : operations) {
            // exact match?
            if (operation2.getLhsDatatype().equals(lhsResult.getDatatype())
                    && operation2.getRhsDatatype().equals(rhsResult.getDatatype())) {
                return operation2.generate(lhsResult, rhsResult);
            }
            // match with implicit casting
            if (isConversionPossibleAndOperationIsNull(lhsResult, rhsResult, operation, operation2)) {
                // we use the
                // operation
                // that matches
                // with code
                // conversion
                operation = operation2;
            }
        }
        if (operation != null) {
            // use operation with implicit casting
            AbstractCompilationResult<T> convertedLhsResult = lhsResult;
            if (!lhsResult.getDatatype().equals(operation.getLhsDatatype())
                    && (!(operation.getLhsDatatype() instanceof AnyDatatype))) {
                T convertedLhs = getConversionCodeGenerator().getConversionCode(lhsResult.getDatatype(),
                        operation.getLhsDatatype(), lhsResult.getCodeFragment());
                convertedLhsResult = newCompilationResultImpl(convertedLhs, operation.getLhsDatatype());
                convertedLhsResult.addMessages(lhsResult.getMessages());
            }
            AbstractCompilationResult<T> convertedRhsResult = rhsResult;
            if (!rhsResult.getDatatype().equals(operation.getRhsDatatype())
                    && (!(operation.getRhsDatatype() instanceof AnyDatatype))) {
                T convertedRhs = getConversionCodeGenerator().getConversionCode(rhsResult.getDatatype(),
                        operation.getRhsDatatype(), rhsResult.getCodeFragment());
                convertedRhsResult = newCompilationResultImpl(convertedRhs, operation.getRhsDatatype());
                convertedRhsResult.addMessages(rhsResult.getMessages());
            }
            return operation.generate(convertedLhsResult, convertedRhsResult);
        }
        Object[] replacements = { operator,
                lhsResult.getDatatype().getName() + ", " + rhsResult.getDatatype().getName() }; //$NON-NLS-1$
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_OPERATOR, getLocale(),
                replacements);
        return newCompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_OPERATOR, text));
    }

    private boolean isConversionPossibleAndOperationIsNull(AbstractCompilationResult<T> lhsResult,
            AbstractCompilationResult<T> rhsResult,
            BinaryOperation<T> operation,
            BinaryOperation<T> operation2) {
        return operation == null
                && getConversionCodeGenerator().canConvert(lhsResult.getDatatype(), operation2.getLhsDatatype())
                && getConversionCodeGenerator().canConvert(rhsResult.getDatatype(), operation2.getRhsDatatype());
    }

    private static class FunctionComparator implements Comparator<FlFunction<?>>, Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -6448576956808509752L;

        @Override
        public int compare(FlFunction<?> o1, FlFunction<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
