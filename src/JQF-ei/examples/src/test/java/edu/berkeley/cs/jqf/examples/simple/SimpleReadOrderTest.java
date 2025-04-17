package edu.berkeley.cs.jqf.examples.simple;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AlphaStringGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class SimpleReadOrderTest {

    @Fuzz
    public void testWithGenerator(@From(AlphaStringGenerator.class) String a1,
                                  @From(AlphaStringGenerator.class) String b1,
                                  @From(AlphaStringGenerator.class) String a2,
                                  @From(AlphaStringGenerator.class) String b2) {
        SimpleReadOrderClass.leftToRight(a1, b1);
        SimpleReadOrderClass.rightToLeft(a2, b2);
    }

    @Fuzz
    public void testWithGeneratorEI(@From(AlphaStringGenerator.class) String a1,
                                  @From(AlphaStringGenerator.class) String b1,
                                  @From(AlphaStringGenerator.class) String a2,
                                  @From(AlphaStringGenerator.class) String b2) {
        SimpleReadOrderClass.leftToRight(a1, b1);
        SimpleReadOrderClass.rightToLeft(a2, b2);
    }
}
