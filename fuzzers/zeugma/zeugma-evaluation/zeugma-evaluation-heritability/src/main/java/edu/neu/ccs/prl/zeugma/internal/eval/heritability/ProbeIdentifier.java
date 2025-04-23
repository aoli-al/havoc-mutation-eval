package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

public final class ProbeIdentifier {
    private final int classIndex;
    private final int probeIndex;

    public ProbeIdentifier(int classIndex, int probeIndex) {
        if (classIndex < 0 || probeIndex < 0) {
            throw new IllegalArgumentException();
        }
        this.classIndex = classIndex;
        this.probeIndex = probeIndex;
    }

    @Override
    public int hashCode() {
        int result = probeIndex;
        result = 31 * result + classIndex;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ProbeIdentifier)) {
            return false;
        }
        ProbeIdentifier that = (ProbeIdentifier) o;
        if (probeIndex != that.probeIndex) {
            return false;
        }
        return classIndex == that.classIndex;
    }

    @Override
    public String toString() {
        return "ProbeIdentifier{" + classIndex + ", " + probeIndex + '}';
    }
}
