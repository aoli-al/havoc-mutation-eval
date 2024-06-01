package edu.neu.ccs.prl.zeugma.internal.parametric;

import edu.neu.ccs.prl.meringue.Replayer;
import edu.neu.ccs.prl.meringue.ReplayerManager;
import edu.neu.ccs.prl.zeugma.internal.fuzz.CampaignOutput;
import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Guidance;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;

import java.io.IOException;

public class ZeugmaReplayer implements Replayer {
    private String testClassName;
    private String testMethodName;
    private ClassLoader classLoader;

    @Override
    public void configure(String testClassName, String testMethodName, ClassLoader classLoader) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.classLoader = classLoader;
    }

    @Override
    public void accept(ReplayerManager manager) throws Throwable {
        JqfRunner.createInstance(testClassName,
                testMethodName,
                classLoader,
                target -> new ReplayGuidance(manager, target)).run();
    }

    private static final class ReplayGuidance implements Guidance {
        private final ReplayerManager manager;
        private final FuzzTarget target;
        private final DataProviderFactory factory = BasicRecordingDataProvider.createFactory(null, Integer.MAX_VALUE);

        private ReplayGuidance(ReplayerManager manager, FuzzTarget target) {
            this.manager = manager;
            this.target = target;
        }

        @Override
        public void fuzz() throws IOException {
            while (manager.hasNextInput()) {
                RecordingDataProvider provider = factory.create(CampaignOutput.readInput(manager.nextInput()));
                Throwable failure = target.run(provider).getFailure();
                manager.handleResult(failure);
            }
        }
    }
}