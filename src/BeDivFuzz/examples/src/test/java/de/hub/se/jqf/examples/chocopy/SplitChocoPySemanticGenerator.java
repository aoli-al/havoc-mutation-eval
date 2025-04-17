package de.hub.se.jqf.examples.chocopy;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.se.jqf.fuzz.junit.quickcheck.SplitSourceOfRandomness;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assume.assumeTrue;

/* Generates random strings that are syntactically valid ChocoPy */
public class SplitChocoPySemanticGenerator extends Generator<String> {
    public SplitChocoPySemanticGenerator() {
        super(String.class); // Register type of generated object

        // Read parameters from system properties
        maxIdentifiers = Integer.getInteger(identifierProp, 3);
        maxItems = Integer.getInteger(itemsProp, 3);
        maxDepth = Integer.getInteger(depthProp, 3);
        maxBound = 4;
        assert(maxIdentifiers > 0);
        assert(maxItems > 0);
        assert(maxDepth > 0);

        // Create set of fixed identifiers
        generateIdentifiers(maxBound);
    }

    private final String identifierProp = "maxIdentifiers";
    private final String itemsProp = "maxItems";
    private final String depthProp = "maxDepth";
    private GenerationStatus status; // saved state object when generating
    private static List<String> identifiers; // Stores generated IDs, to promote re-use
    private static List<String> funcIdentifiers; // Stores generated IDs, to promote re-use
    private static List<String> allTypes; // Keeps track of all types
    private static List<String> classTypes; // Keeps track of all types
    private static int maxIdentifiers;
    private static int maxItems;
    private static int maxDepth;
    private static int maxBound;
    private int statementDepth; // Keeps track of how deep the AST is at any point
    private int declarationDepth; // Keeps track of how deep the AST is at any point
    private int expressionDepth; // Keeps track of how nested an expression is at any point
    private int indentLevel; // Keeps track of indentation level

    private static final String[] BINARY_TOKENS = {
            "+", "-", "*", "//", "%", "and", "or",
            "<", "<=", ">", ">=", "==", "!=", "is"
    };

    private static final String[] BINARY_BOOL_TOKENS = {
            "and", "or"
    };

    private static final String[] BASE_TYPES = {
            "int", "str", "bool", "object"
    };

    private static final String[] INT_LITERALS = {
            "0", "1"
    };

    private static final String[] STRING_LITERALS = {
            "\"a\"", "\"\""
    };

    private static final String[] BOOL_LITERALS = {
            "True", "False"
    };

    private static final String INDENT_TOKEN = "    "; // 4 spaces


    /** Main entry point. Called once per test case. Returns a random ChocoPy program. */
    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        this.status = status; // we save this so that we can pass it on to other generators
        this.declarationDepth = 0;
        this.statementDepth = 0;
        this.expressionDepth = 0;
        this.indentLevel = 0;
        this.classTypes = new ArrayList<>();
        this.classTypes.add("object");
        this.allTypes = Arrays.asList(BASE_TYPES);
        this.allTypes = new ArrayList<>(this.allTypes);
        for (String identifier : identifiers) {
            this.allTypes.add(identifier);
            this.classTypes.add(identifier);
        }
        return generateProgram((SplitSourceOfRandomness) random);
    }

    /** Utility method for generating a random list of items (e.g. statements, arguments, attributes) */
    private static List<String> generateItems(Function<SplitSourceOfRandomness, String> genMethod, SplitSourceOfRandomness random, int minimum) {
        int len = nextStructureIntBound(random, minimum, maxBound, maxItems);
        List<String> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            items.add(genMethod.apply(random));
        }
        return items;
    }

    /** Utility method for generating a random list of items from a list of functions to choose from */
    private static List<String> generateItemsMultipleMethods(List<Function<SplitSourceOfRandomness, String>> genMethods, SplitSourceOfRandomness random, int minimum) {
        int len = nextStructureIntBound(random, minimum, maxBound, maxItems);
        List<String> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            items.add(random.choose(genMethods, true).apply(random));
        }
        return items;
    }

    private static int nextStructureIntBound(SplitSourceOfRandomness random, int minimum, int maximum, int maxParam) {
        int randInt = random.nextInt(minimum, maximum, true);
        assumeTrue(randInt <= maxParam);
        return randInt;
    }

    private static int nextValueIntBound(SplitSourceOfRandomness random, int minimum, int maximum, int maxParam) {
        int randInt = random.nextInt(minimum, maximum, true);
        assumeTrue(randInt <= maxParam);
        return randInt;
    }

    /** Generates a random ChocoPy program of classes, declarations, and statements */
    private String generateProgram(SplitSourceOfRandomness random) {
        String declarations = String.join("", generateItemsMultipleMethods(Arrays.asList(
                this::generateClassDef,
                this::generateFuncDef,
                this::generateVarDef
        ), random, 0));
        String statements = generateBlock(random, 0);
        return declarations + statements;
    }

    /** Generates a random ChocoPy declaration */
    private String generateDeclaration(SplitSourceOfRandomness random) {
        String result = StringUtils.repeat(INDENT_TOKEN, indentLevel);
        int randDepth = nextStructureIntBound(random, 0, maxBound, maxDepth);
        if (declarationDepth >= randDepth) {
            // Choose a random private method from this class, and then call it with `random`
            result += generateVarDef(random);
        } else {
            // If depth is low and we won the flip, then generate compound declarations
            // (that is, declarations that contain other declarations)
            result += random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateFuncDef
            ), true).apply(random);
        }
        return result;
    }

    /** Generates a random ChocoPy function declaration */
    private String generateFuncDeclaration(SplitSourceOfRandomness random) {
        String result = StringUtils.repeat(INDENT_TOKEN, indentLevel);
        int randDepth = nextStructureIntBound(random, 0, maxBound, maxDepth);
        if (declarationDepth >= randDepth) {
            // Choose a random private method from this class, and then call it with `random`
            result += random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateVarDef,
                    this::generateNonlocalDecl,
                    this::generateGlobalDecl
            ), true).apply(random);
        } else {
            // If depth is low and we won the flip, then generate compound declarations
            // (that is, declarations that contain other declarations)
            result += random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateFuncDef
            ), true).apply(random);
        }
        return result + "\n";
    }

    /** Generates a random ChocoPy statement */
    private String generateStatement(SplitSourceOfRandomness random) {
        String result = StringUtils.repeat(INDENT_TOKEN, indentLevel);
        // If depth is too high, then generate only simple statements to prevent infinite recursion
        // If not, generate simple statements after the flip of a coin
        int randDepth = nextStructureIntBound(random, 0, maxBound, maxDepth);
        if (statementDepth >= randDepth) {
            // Choose a random private method from this class, and then call it with `random`
            result += random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateAssignStmt,
                    this::generatePassStmt,
                    this::generateReturnStmt,
                    this::generateExpressionStmt
            ), true).apply(random);
        } else {
            // If depth is low and we won the flip, then generate compound statements
            // (that is, statements that contain other statements)
            result += random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateIfStmt,
                    this::generateForStmt,
                    this::generateWhileStmt
            ), true).apply(random);
        }
        return result + "\n";
    }

    /** Generates a random ChocoPy expression using recursive calls */
    private String generateExpression(SplitSourceOfRandomness random) {
        String result;
        // Choose terminal if nesting depth is too high or based on a random flip of a coin
        int randDepth = nextStructureIntBound(random, 0, maxBound, maxDepth);
        if (expressionDepth >= randDepth) {
            result = generateCExpression(random);
        } else {
            expressionDepth++;
            // Otherwise, choose a non-terminal generating function
            result = random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateUnaryBoolExpr,
                    this::generateBinaryBoolExpr,
                    this::generateIfExpr
            ), true).apply(random);
            expressionDepth--;
        }
        return result;
    }

    private String generateCExpression(SplitSourceOfRandomness random) {
        expressionDepth++;
        String result;
        // Choose terminal if nesting depth is too high or based on a random flip of a coin
        int randDepth = nextStructureIntBound(random, 0, maxBound, maxDepth);
        if (expressionDepth >= randDepth) {
            result = random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateLiteral,
                    this::generateIdentifier
            ), true).apply(random);
        } else {
            // Otherwise, choose a non-terminal generating function
            result = random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateListExpr,
                    this::generateParenExpr,
                    this::generateMemberExpr,
                    this::generateMethodCallExpr,
                    this::generateIndexExpr,
                    this::generateCallExpr,
                    this::generateBinaryExpr,
                    this::generateUnaryExpr
            ), true).apply(random);
        }
        expressionDepth--;
        return result;
    }

    private String generateAssignStmt(SplitSourceOfRandomness random) {
        String result = "";
        int len = nextStructureIntBound(random, 1, maxBound, maxDepth);
        List<String> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            String target = random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                    this::generateIdentifier,
                    this::generateIndexExpr,
                    this::generateMemberExpr
            ), true).apply(random);
            result += target + " = ";
        }
        return result + generateExpression(random);
    }

    private String generateBinaryBoolExpr(SplitSourceOfRandomness random) {
        String token = random.choose(BINARY_BOOL_TOKENS, false);
        String lhs = generateCExpression(random);
        String rhs = generateCExpression(random);

        return lhs + " " + token + " " + rhs;
    }

    /** Generates a random binary expression (e.g. A op B) */
    private String generateBinaryExpr(SplitSourceOfRandomness random) {
        String token = random.choose(BINARY_TOKENS, false);
        String lhs = generateCExpression(random);
        String rhs = generateCExpression(random);

        return lhs + " " + token + " " + rhs;
    }

    /** Generates a block of statements */
    private String generateBlock(SplitSourceOfRandomness random, int minimum) {
        return String.join("", generateItems(this::generateStatement, random, minimum));
    }

    private String generateCallExpr(SplitSourceOfRandomness random) {
        String func = generateFuncIdentifier(random);
        String args = String.join(",", generateItems(this::generateExpression, random, 0));

        String call = func + "(" + args + ")";
        return call;
    }

    private String generateClassDef(SplitSourceOfRandomness random) {
        String result = "";
        String className = generateIdentifier(random);
        // Superclass could be one of the identifiers or object. Index should be from 0 to maxIdentifiers inclusive.
        int superClassIndex = nextValueIntBound(random, 0, classTypes.size(), maxIdentifiers);
        String superClassName = classTypes.get(superClassIndex);
        result += "class " + className + "(" + superClassName + "):\n";
        indentLevel++;
        result += generateDeclarationBlock(random, 1);
        indentLevel--;
        return result + "\n";
    }

    /** Generates a block of VarDefs and FuncDefs*/
    private String generateDeclarationBlock(SplitSourceOfRandomness random, int minimum) {
        return String.join("", generateItems(this::generateDeclaration, random, minimum));
    }

    private String generateExpressionStmt(SplitSourceOfRandomness random) {
        return generateExpression(random);
    }

    private String generateForStmt(SplitSourceOfRandomness random) {
        statementDepth++;
        String s = "for " + generateIdentifier(random) + " in " + generateExpression(random) + ":\n";
        indentLevel++;
        s += generateBlock(random, 1);
        indentLevel--;
        statementDepth--;
        return s;
    }

    private String generateFuncDef(SplitSourceOfRandomness random) {
        declarationDepth++;
        String result = "def " + generateFuncIdentifier(random) + "("
                + String.join(", ", generateItems(this::generateTypedVar, random, 0)) + ")";
        if (random.nextBoolean(true)) {
            result += "->" + generateType(random);
        }
        result += ":\n";
        indentLevel++;
        result += String.join("", generateItems(this::generateFuncDeclaration, random, 0));
        result += generateBlock(random, 1);
        indentLevel--;
        declarationDepth--;
        return result + "\n";
    }

    private String generateGlobalDecl(SplitSourceOfRandomness random) {
        return "global " + generateIdentifier(random) + "\n";
    }

    private String generateIdentifier(SplitSourceOfRandomness random) {
        int index = nextValueIntBound(random, 0, maxBound, maxIdentifiers - 1);
        return identifiers.get(index);
    }

    private String generateFuncIdentifier(SplitSourceOfRandomness random) {
        int index = nextValueIntBound(random, 0, funcIdentifiers.size(),
                maxIdentifiers + BASE_TYPES.length);
        return funcIdentifiers.get(index);
    }

    /** Creates initial set of identifiers depending on parameter */
    private void generateIdentifiers(int numIdentifiers) {
        this.identifiers = new ArrayList<>();
        this.funcIdentifiers = new ArrayList<>(Arrays.asList(BASE_TYPES));
        funcIdentifiers.add("len");
        String ident;
        for (int i = 0; i < numIdentifiers; i++) {
            ident = "a" + i;
            identifiers.add(ident);
            funcIdentifiers.add(ident);
        }
    }

    private String generateIfExpr(SplitSourceOfRandomness random) {
        return generateExpression(random) + " if " + generateExpression(random) + " else " + generateExpression(random);
    }

    private String generateIfStmt(SplitSourceOfRandomness random) {
        statementDepth++;
        String result = "if " + generateExpression(random) + ":\n";
        indentLevel++;
        result += generateBlock(random, 1);
        indentLevel--;
        if (random.nextBoolean(true)) {
            result += StringUtils.repeat(INDENT_TOKEN, indentLevel);
            result += "elif " + generateExpression(random) + ":\n";
            indentLevel++;
            result += generateBlock(random, 1);
            indentLevel--;
        }
        if (random.nextBoolean(true)) {
            result += StringUtils.repeat(INDENT_TOKEN, indentLevel);
            result += "else:\n";
            indentLevel++;
            result += generateBlock(random, 1);
            indentLevel--;
        }
        statementDepth--;
        return result;
    }

    private String generateIndexExpr(SplitSourceOfRandomness random) {
        return "(" + generateCExpression(random) + ")[" + generateExpression(random) + "]";
    }

    private String generateListExpr(SplitSourceOfRandomness random) {
        return "[" + String.join(", ", generateItems(this::generateExpression, random, 0)) + "]";
    }

    // Generate fixed primitive literals
    private String generateLiteral(SplitSourceOfRandomness random) {

        return random.choose(Arrays.<Function<SplitSourceOfRandomness, String>>asList(
                this::generateIntLiteral,
                this::generateStringLiteral,
                this::generateBoolLiteral,
                this::generateNoneLiteral), true).apply(random);
    }

    private String generateIntLiteral(SplitSourceOfRandomness random) {
        return random.choose(INT_LITERALS, false);
    }

    private String generateStringLiteral(SplitSourceOfRandomness random) {
        return random.choose(STRING_LITERALS, false);
    }

    private String generateBoolLiteral(SplitSourceOfRandomness random) {
        return random.choose(BOOL_LITERALS, false);
    }

    private String generateNoneLiteral(SplitSourceOfRandomness random) {
        return "None";
    }

    private String generateMemberExpr(SplitSourceOfRandomness random) {
        return "(" + generateCExpression(random) + ")." + generateIdentifier(random);
    }

    private String generateMethodCallExpr(SplitSourceOfRandomness random) {
        return generateCExpression(random) + "." + generateCallExpr(random);
    }

    private String generateNonlocalDecl(SplitSourceOfRandomness random) {
        return "nonlocal " + generateIdentifier(random) + "\n";
    }

    private String generateParenExpr(SplitSourceOfRandomness random) {
        return "(" + generateExpression(random) + ")";
    }

    private String generatePassStmt(SplitSourceOfRandomness random) {
        return "pass";
    }

    private String generateReturnStmt(SplitSourceOfRandomness random) {
        return random.nextBoolean(true) ? "return" : "return " + generateExpression(random);
    }

    /** Randomly choose from types and random list depth using maxDepth parameter */
    private String generateType(SplitSourceOfRandomness random) {
        int typeIndex = nextValueIntBound(random, 0, allTypes.size(), BASE_TYPES.length + maxIdentifiers - 1);
        String type = allTypes.get(typeIndex);
        int listDepth = nextValueIntBound(random, 0, maxBound, maxDepth);
        for (int i = 0; i < listDepth; i++) {
            type = "[" + type + "]";
        }
        return type;
    }

    private String generateTypedVar(SplitSourceOfRandomness random) {
        String result = "";
        String type = generateType(random);
        result += generateIdentifier(random) + ":" + type;
        return result;
    }

    private String generateUnaryBoolExpr(SplitSourceOfRandomness random) {
        return "not " + generateExpression(random);
    }

    private String generateUnaryExpr(SplitSourceOfRandomness random) {
        return "-" + generateCExpression(random);
    }

    private String generateVarDef(SplitSourceOfRandomness random) {
        return generateTypedVar(random) + " = " + generateLiteral(random) + "\n";
    }

    private String generateWhileStmt(SplitSourceOfRandomness random) {
        statementDepth++;
        indentLevel++;
        String result = "while " + generateExpression(random) + ":\n" + generateBlock(random, 1);
        indentLevel--;
        statementDepth--;
        return result;
    }
}
