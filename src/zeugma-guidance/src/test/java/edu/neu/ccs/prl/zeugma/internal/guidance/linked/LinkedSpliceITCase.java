package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static edu.neu.ccs.prl.zeugma.internal.guidance.linked.CallTreeUtil.findMatches;

public class LinkedSpliceITCase {
    @Test
    void spliceXmlElement() {
        CallTreeUtil.GenerateFuzzTarget<String> target = CallTreeUtil.createXmlTarget();
        // Run once to get the extended input
        ByteList input = ByteArrayList.range((byte) 0, (byte) 20);
        RecordingDataProvider provider1 =
                BasicRecordingDataProvider.createFactory(null, Integer.MAX_VALUE).create(input);
        target.run(provider1);
        input = provider1.getRecording();
        // Create the call tree for the input
        CallTree tree = new CallTreeBuilder(target).build(input);
        // Splice generateElement0 into generateElement1
        MethodCallVertex e0 =
                (MethodCallVertex) findMatches(tree, n -> CallTreeUtil.isMethodCall(n, "generateElement")).get(0);
        MethodCallVertex e1 =
                (MethodCallVertex) findMatches(tree, n -> CallTreeUtil.isMethodCall(n, "generateElement")).get(1);
        LinkedSplice splice = new LinkedSplice(e1, e0, input);
        splice.apply(input);
        run(target, splice.apply(input));
        // Get the structure generated after the modification
        String actual = target.getStructure();
        // Original structure: <b j='d' x='r'><c ></c><a ></a><a ></a></b>
        String expected = "<b j='d' x='r'><c j='d' x='r'><c ></c><a ></a><a ></a></c><a ></a><a ></a></b>";
        Assertions.assertEquals(expected, actual);
    }

    static void run(FuzzTarget target, ByteList input) {
        RecordingDataProvider provider =
                BasicRecordingDataProvider.createFactory(null, Integer.MAX_VALUE).create(input);
        try {
            target.run(provider);
        } finally {
            provider.close();
        }
    }
}