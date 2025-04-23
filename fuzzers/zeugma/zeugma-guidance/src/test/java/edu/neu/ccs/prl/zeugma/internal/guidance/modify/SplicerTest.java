package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class SplicerTest {
    abstract Splicer getSplicer();

    @Test
    void spliceDoesNotModifyFirstParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList p1Copy = new ByteArrayList(p1);
        getSplicer().splice(p1, p2);
        Assertions.assertEquals(p1Copy, p1);
    }

    @Test
    void spliceDoesNotModifySecondParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList p2Copy = new ByteArrayList(p2);
        getSplicer().splice(p1, p2);
        Assertions.assertEquals(p2Copy, p2);
    }

    @Test
    void spliceFirstParentNull() {
        assertThrows(NullPointerException.class, () -> getSplicer().splice(null, new ByteArrayList()));
    }

    @Test
    void spliceSecondParentNull() {
        assertThrows(NullPointerException.class, () -> getSplicer().splice(new ByteArrayList(), null));
    }

    @Test
    void spliceBothParentsEmpty() {
        ByteList p1 = new ByteArrayList();
        ByteList p2 = new ByteArrayList();
        ByteList child = getSplicer().splice(p1, p2);
        Assertions.assertTrue(child.isEmpty());
    }
}
