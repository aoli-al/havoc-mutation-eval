package edu.neu.ccs.prl.zeugma.examples;

import edu.neu.ccs.prl.zeugma.internal.guidance.DataProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleMapGenerator {
    DataProvider provider;

    public Map<String, Long> generateMap(DataProvider provider) {
        this.provider = provider;
        int size = generateSize();
        Map<String, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            generateEntry(result);
        }
        return result;
    }

    private void generateEntry(Map<String, Long> map) {
        map.put(generateString(), generateLong());
    }

    private int generateSize() {
        return provider.nextInt(100);
    }

    private Long generateLong() {
        return provider.nextLong();
    }

    private int generateCodePoint() {
        return provider.nextInt();
    }

    private String generateString() {
        int[] codePoints = new int[provider.nextInt(100)];
        for (int i = 0; i < codePoints.length; ++i) {
            codePoints[i] = generateCodePoint();
        }
        return new String(codePoints, 0, codePoints.length);
    }
}
