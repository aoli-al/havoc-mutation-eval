package edu.neu.ccs.prl.zeugma.internal.runtime.model;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

/**
 * Thread-safe.
 */
public final class ModelRegistry {
    private static final SimpleList<ClassModel> registered = new SimpleList<>();
    private static long numberOfRegisteredProbes = 0;

    private ModelRegistry() {
        throw new AssertionError();
    }

    public static synchronized int register(ClassModel model) {
        numberOfRegisteredProbes += model.getNumberOfProbes();
        registered.add(model);
        return registered.size() - 1;
    }

    public static synchronized ClassModel get(int index) {
        return registered.get(index);
    }

    public static synchronized long getNumberOfRegisteredProbes() {
        return numberOfRegisteredProbes;
    }

    public static synchronized void resetCoverageMap() {
        for (int i = 0; i < registered.size(); i++) {
            registered.get(i).reset();
        }
    }

    public static synchronized boolean[][] getCoverageMap() {
        boolean[][] result = new boolean[registered.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = registered.get(i).getCoverage();
        }
        return result;
    }
}