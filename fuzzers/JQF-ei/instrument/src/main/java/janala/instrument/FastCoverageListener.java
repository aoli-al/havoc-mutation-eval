package janala.instrument;

import java.util.Date;

public interface FastCoverageListener {
    public class Default implements FastCoverageListener {
        public void logMethodBegin(int iid) {
        }

        public void logMethodEnd(int iid) {
        }

        public void logJump(int iid, int branch) {
        }

        public void logLookUpSwitch(int value, int iid, int dflt, int[] cases) {
        }

        public void logTableSwitch(int value, int iid, int min, int max, int dflt) {
        }

        public void done() {}

        public void start() {}
    }

    void logMethodBegin(int iid);

    void logMethodEnd(int iid);

    void logJump(int iid, int branch);

    void logLookUpSwitch(int value, int iid, int dflt, int[] cases);

    void logTableSwitch(int value, int iid, int min, int max, int dflt);

    void done();
    void start();
}
