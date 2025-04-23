package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Math;

public abstract class Corpus {
    public static final String CORPUS_PREFIX = String.join(File.separator, "campaign", "corpus");
    public static final String SUMMARY_FILE_NAME = "summary.json";
    private final String fuzzer;
    private final String subject;
    private final String testClassName;
    private final String testMethodName;

    Corpus(String summary) {
        if (summary == null) {
            throw new NullPointerException();
        }
        this.testClassName = findValue("testClassName", summary);
        this.testMethodName = findValue("testMethodName", summary);
        String frameworkClassName = findValue("frameworkClassName", summary);
        String[] split = testClassName.split("\\.");
        this.subject = split[split.length - 1].replace("Fuzz", "");
        split = frameworkClassName.split("\\.");
        String tempFuzzer = split[split.length - 1].replace("Framework", "");
        if (summary.contains("-Dzeugma.crossover=none")) {
            tempFuzzer += "-None";
        }
        this.fuzzer = tempFuzzer;
    }

    public String getFuzzer() {
        return fuzzer;
    }

    public String getSubject() {
        return subject;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public List<ByteList> readInputs(Duration duration) throws IOException {
        if (duration.isNegative()) {
            throw new IllegalArgumentException();
        }
        List<TimestampedInput> elements = readInputs();
        long firstTime = findFirstTime(elements);
        List<ByteList> inputs = new ArrayList<>();
        for (TimestampedInput element : elements) {
            if (element.getTime() - firstTime < duration.toMillis()) {
                inputs.add(element.getInput());
            }
        }
        return inputs;
    }

    protected abstract List<TimestampedInput> readInputs() throws IOException;

    private long findFirstTime(List<TimestampedInput> elements) {
        long first = Long.MAX_VALUE;
        for (TimestampedInput element : elements) {
            first = Math.min(element.getTime(), first);
        }
        return first;
    }

    private static String findValue(String key, String content) {
        String target = String.format("\"%s\": \"", key);
        int i = content.indexOf(target);
        if (i == -1) {
            throw new IllegalStateException("Invalid meringue summary");
        }
        int start = i + target.length();
        int end = content.indexOf("\"", start);
        if (end == -1) {
            throw new IllegalStateException("Invalid meringue summary");
        }
        return content.substring(start, end);
    }
}
