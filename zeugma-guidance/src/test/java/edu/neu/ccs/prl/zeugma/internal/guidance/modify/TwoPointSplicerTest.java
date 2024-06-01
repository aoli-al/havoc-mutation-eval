package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

class TwoPointSplicerTest extends SplicerTest {
    private static final TwoPointSplicer splicer = new TwoPointSplicer(new Random(4380));

    @Override
    Splicer getSplicer() {
        return splicer;
    }

    @Test
    void spliceMiddle() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 17);
        // 0 1 2 3 4 | 5 6 | 7 8 9
        // 10 11 | 12 13 14 15 | 16
        ByteList child = TwoPointSplicer.splice(p1, 5, 7, p2, 2, 6);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 12, 13, 14, 15, 7, 8, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void spliceSingletons() {
        ByteList p1 = new ByteArrayList(new byte[]{0});
        ByteList p2 = new ByteArrayList(new byte[]{1});
        // | 0 |
        // | 1 |
        ByteList child = TwoPointSplicer.splice(p1, 0, 1, p2, 0, 1);
        ByteList expected = new ByteArrayList(new byte[]{1});
        Assertions.assertEquals(expected, child);
    }


    @Test
    void spliceFirstParentEmpty() {
        ByteList p1 = new ByteArrayList();
        ByteList p2 = ByteArrayList.range((byte) 0, (byte) 10);
        // 0 1 | 2 3 | 4 5 6 7 8 9
        ByteList child = TwoPointSplicer.splice(p1, 0, 0, p2, 2, 4);
        ByteList expected = new ByteArrayList(new byte[]{2, 3});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void spliceSecondParentEmpty() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = new ByteArrayList();
        // 0 1 | 2 3 | 4 5 6 7 8 9
        ByteList child = TwoPointSplicer.splice(p1, 2, 4, p2, 0, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 4, 5, 6, 7, 8, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void spliceAllOfFirstParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        // 0 1 2 3 4 5 || 6 7 8 9
        // 10 11 12 13 14 15 | 16 17 | 18 19
        ByteList child = TwoPointSplicer.splice(p1, 6, 6, p2, 6, 8);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5, 16, 17, 6, 7, 8, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void spliceNoneOfFirstParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        // | 0 1 2 3 4 5 6 7 8 9 |
        // 10 11 12 13 14 15 | 16 17 | 18 19
        ByteList child = TwoPointSplicer.splice(p1, 0, p1.size(), p2, 6, 8);
        ByteList expected = new ByteArrayList(new byte[]{16, 17});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void spliceAllOfSecondParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        // 0 | 1 2 3 4 5  6 7 8 | 9
        // | 10 11 12 13 14 15 16 17 18 19 |
        ByteList child = TwoPointSplicer.splice(p1, 1, 9, p2, 0, p2.size());
        ByteList expected = new ByteArrayList(new byte[]{0, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void spliceNoneOfSecondParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) 10);
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        // 0 | 1 2 3 4 5  6 | 7 8 9
        // 10 11 12 || 13 14 15 16 17 18 19
        ByteList child = TwoPointSplicer.splice(p1, 1, 7, p2, 3, 3);
        ByteList expected = new ByteArrayList(new byte[]{0, 7, 8, 9});
        Assertions.assertEquals(expected, child);
    }
}