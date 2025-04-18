package edu.berkeley.cs.jqf.examples.simple;

public class SimpleReadOrderClass {

    static void leftToRight(String a, String b) {
        if (a.equals("abc")) {
            if (b.equals("def")) {
                throw new RuntimeException("bug 1 triggered!");
            }
        }
    }

    static void rightToLeft(String b, String a) {
        if (a.equals("abc")) {
            if (b.equals("def")) {
                throw new RuntimeException("bug 2 triggered!");
            }
        }
    }

}
