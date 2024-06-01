package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnePointSplicerTest extends SplicerTest {
    private final OnePointSplicer splicer = new OnePointSplicer(new Random(4380));

    @Override
    Splicer getSplicer() {
        return splicer;
    }

    @Test
    void spliceMiddle() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList child = OnePointSplicer.splice(p1, 5, p2, 6);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 16, 17, 18, 19});
        assertEquals(expected, child);
    }

    @Test
    void spliceSingletons() {
        ByteList p1 = new ByteArrayList(new byte[]{0});
        ByteList p2 = new ByteArrayList(new byte[]{1});
        ByteList child = OnePointSplicer.splice(p1, 0, p2, 0);
        ByteList expected = new ByteArrayList(new byte[]{1});
        assertEquals(expected, child);
    }

    @Test
    void spliceFirstParentEmpty() {
        ByteList p1 = new ByteArrayList();
        ByteList p2 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList child = OnePointSplicer.splice(p1, 0, p2, 2);
        ByteList expected = new ByteArrayList(new byte[]{2, 3, 4, 5, 6, 7, 8, 9});
        assertEquals(expected, child);
    }

    @Test
    void spliceSecondParentEmpty() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList p2 = new ByteArrayList();
        ByteList child = OnePointSplicer.splice(p1, 2, p2, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 1});
        assertEquals(expected, child);
    }

    @Test
    void spliceNoneOfFirstParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList child = OnePointSplicer.splice(p1, 0, p2, 6);
        ByteList expected = new ByteArrayList(new byte[]{16, 17, 18, 19});
        assertEquals(expected, child);
    }


    @Test
    void spliceAllOfFirstParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList child = OnePointSplicer.splice(p1, p1.size(), p2, 6);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 16, 17, 18, 19});
        assertEquals(expected, child);
    }

    @Test
    void spliceNoneOfSecondParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList child = OnePointSplicer.splice(p1, 7, p2, p2.size());
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5, 6});
        assertEquals(expected, child);
    }


    @Test
    void spliceAllOfSecondParent() {
        ByteList p1 = ByteArrayList.range((byte) 0, (byte) (10));
        ByteList p2 = ByteArrayList.range((byte) 10, (byte) 20);
        ByteList child = OnePointSplicer.splice(p1, 7, p2, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        assertEquals(expected, child);
    }
}