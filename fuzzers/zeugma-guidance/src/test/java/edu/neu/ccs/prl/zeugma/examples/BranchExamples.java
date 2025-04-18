package edu.neu.ccs.prl.zeugma.examples;

public class BranchExamples {
    public static boolean result = false;

    public static void isNull(Object x) {
        result = x == null;
    }

    public static void equality(Object x, Object y) {
        result = x == y;
    }

    public static void equality(int x, int y) {
        result = x == y;
    }

    public static void equality(int x) {
        result = x == 0;
    }
}