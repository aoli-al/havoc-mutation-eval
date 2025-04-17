package de.hub.se.jqf.fuzz;

import de.hub.se.jqf.fuzz.div.BeDivFuzzGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.CoverageFactory;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Random;

/**
 * Version of {@link BeDivFuzzGuidance} that ensures that inputs that exercise new coverage are saved to the corpus.
 */
public class SavingBeDivFuzzGuidance extends BeDivFuzzGuidance {
    private final ICoverage<?> allCoverage = CoverageFactory.newInstance();

    public SavingBeDivFuzzGuidance(String testName, Duration duration, Long trials, File outputDirectory,
                                   Random sourceOfRandomness) throws IOException {
        super(testName, duration, trials, outputDirectory, sourceOfRandomness);
    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        super.handleResult(result, error);
        this.conditionallySynchronize(this.multiThreaded, () -> {
            int before = allCoverage.getNonZeroCount();
            allCoverage.updateBits(runCoverage);
            int after = allCoverage.getNonZeroCount();
            if (after > before) {
                // Input exercised new coverage
                saveCurrentInputToCorpus();
            }
        });
    }

    private void saveCurrentInputToCorpus() {
        try {
            currentInput.gc();
            assert (currentInput.primaryInput.size() > 0 || currentInput.secondaryInput.size() > 0);
            // First, save to disk (note: we issue IDs to everyone, but only write to disk if valid)
            int newInputIdx = numSavedInputs++;
            String primarySaveFileName = String.format("id_%06d", newInputIdx);
            File primarySaveFile = new File(savedCorpusDirectory, primarySaveFileName);
            String secondarySaveFileName = primarySaveFileName + "_secondary";
            File secondarySaveFile = new File(savedCorpusDirectory, secondarySaveFileName);
            writeInputToFile(currentInput.primaryInput, primarySaveFile);
            writeInputToFile(currentInput.secondaryInput, secondarySaveFile);
        } catch (IOException e) {
            throw new GuidanceException(e);
        }
    }
}
