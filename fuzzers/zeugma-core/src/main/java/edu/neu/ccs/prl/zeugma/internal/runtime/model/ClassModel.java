package edu.neu.ccs.prl.zeugma.internal.runtime.model;

public final class ClassModel {
    /**
     * Information used to unique identify the class.
     * <p>
     * Non-null.
     */
    private final ClassIdentifier identifier;
    /**
     * Methods declared within the class.
     * <p>
     * Non-null.
     */
    private final MethodIdentifier[] methods;
    /**
     * Number of coverage probes placed in the class.
     * <p>
     * Non-negative.
     */
    private final int numberOfProbes;
    /**
     * Index that this model is registered.
     * <p>
     * Non-negative.
     */
    private final int index;
    /**
     * For each probe index i {@code covered[i] == true} if that probe was covered since this model was last reset.
     * <p>
     * Non-null.
     */
    private final boolean[] coverage;

    ClassModel(ClassIdentifier identifier, MethodIdentifier[] methods, int numberOfProbes) {
        if (identifier == null) {
            throw new NullPointerException();
        }
        this.identifier = identifier;
        this.methods = methods.clone();
        this.numberOfProbes = numberOfProbes;
        this.index = ModelRegistry.register(this);
        this.coverage = new boolean[numberOfProbes];
    }

    public MethodIdentifier getMethod(int index) {
        return methods[index];
    }

    @Override
    public String toString() {
        return identifier.getName();
    }

    public int getNumberOfProbes() {
        return numberOfProbes;
    }

    public int getIndex() {
        return index;
    }

    public synchronized void cover(int probeIndex) {
        coverage[probeIndex] = true;
    }

    public synchronized void reset() {
        for (int i = 0; i < numberOfProbes; i++) {
            coverage[i] = false;
        }
    }

    public synchronized boolean[] getCoverage() {
        return coverage.clone();
    }

    public String getName() {
        return identifier.getName();
    }
}
