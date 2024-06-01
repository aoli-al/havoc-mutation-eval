package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CallTreeBuilderITCase {
    @Test
    void builtTreeMatchesExpectedForMap() {
        CallTreeBuilder builder = new CallTreeBuilder(CallTreeUtil.createMapTarget());
        ByteList input = new ByteArrayList();
        CallTreeUtil.writeBasicMap(input);
        CallTree tree = builder.build(input);
        assertChildrenMatch(tree, "generateMap");
        CallTreeVertex generateMap = tree.getChild(0);
        assertChildrenMatch(generateMap, "generateSize", "generateEntry", "generateEntry");
        CallTreeVertex generateSize0 = generateMap.getChild(0);
        assertChildrenMatch(generateSize0, 4);
        CallTreeVertex generateEntry0 = generateMap.getChild(1);
        assertChildrenMatch(generateEntry0, "generateString", "generateLong");
        CallTreeVertex generateString0 = generateEntry0.getChild(0);
        assertChildrenMatch(generateString0,
                4,
                "generateCodePoint",
                "generateCodePoint",
                "generateCodePoint",
                "generateCodePoint");
        for (int i = 1; i < generateString0.getNumberOfChildren(); i++) {
            assertChildrenMatch(generateString0.getChild(i), 4);
        }
        CallTreeVertex generateLong0 = generateEntry0.getChild(1);
        assertChildrenMatch(generateLong0, 8);
        CallTreeVertex generateEntry1 = generateMap.getChild(2);
        assertChildrenMatch(generateEntry1, "generateString", "generateLong");
        CallTreeVertex generateString1 = generateEntry1.getChild(0);
        assertChildrenMatch(generateString1, 4, "generateCodePoint", "generateCodePoint", "generateCodePoint");
        for (int i = 1; i < generateString1.getNumberOfChildren(); i++) {
            assertChildrenMatch(generateString1.getChild(i), 4);
        }
        CallTreeVertex generateLong1 = generateEntry1.getChild(1);
        assertChildrenMatch(generateLong1, 8);
    }

    @Test
    void builtTreeMatchesExpectedForXml() {
        CallTreeBuilder builder = new CallTreeBuilder(CallTreeUtil.createXmlTarget());
        ByteList input = new ByteArrayList();
        CallTreeUtil.writeBasicXml(input);
        CallTree tree = builder.build(input);
        assertChildrenMatch(tree, "generateXml");
        CallTreeVertex generateXml = tree.getChild(0);
        assertChildrenMatch(generateXml, "generateString", "generateElement");
        CallTreeVertex generateString0 = generateXml.getChild(0);
        assertChildrenMatch(generateString0, 2);
        CallTreeVertex generateElement0 = generateXml.getChild(1);
        assertChildrenMatch(generateElement0,
                4,
                "generateString",
                "generateString",
                "nextBoolean",
                4,
                "generateString",
                "generateElement",
                "generateString",
                "generateElement");
        CallTreeVertex generateString1 = generateElement0.getChild(1);
        assertChildrenMatch(generateString1, 2);
        CallTreeVertex generateString2 = generateElement0.getChild(2);
        assertChildrenMatch(generateString2, 2);
        CallTreeVertex generateString3 = generateElement0.getChild(5);
        assertChildrenMatch(generateString3, 2);
        CallTreeVertex generateElement1 = generateElement0.getChild(6);
        assertChildrenMatch(generateElement1, 4, "nextBoolean", "nextBoolean");
        CallTreeVertex generateString4 = generateElement0.getChild(7);
        assertChildrenMatch(generateString4, 2);
        CallTreeVertex generateElement2 = generateElement0.getChild(8);
        assertChildrenMatch(generateElement2, 4, "nextBoolean", "nextBoolean", "generateString");
        CallTreeVertex generateString5 = generateElement2.getChild(3);
        assertChildrenMatch(generateString5, 2);
    }

    static void assertChildrenMatch(CallTreeVertex vertex, Object... expected) {
        Object[] actual = new Object[vertex.getNumberOfChildren()];
        for (int i = 0; i < actual.length; i++) {
            CallTreeVertex child = vertex.getChild(i);
            if (child instanceof MethodCallVertex) {
                actual[i] = ((MethodCallVertex) child).getMethod().getName();
            } else if (child instanceof ParameterRequestVertex) {
                actual[i] = child.getSourceInterval().size();
            }
        }
        Assertions.assertArrayEquals(expected, actual);
    }
}