package de.hub.se.jqf.examples.json;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.se.jqf.fuzz.junit.quickcheck.SplitSourceOfRandomness;

public class SplitJsonGenerator extends Generator<String> {
    int depth = 0;
    public SplitJsonGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        depth = 0;
        return generateJsonObject((SplitSourceOfRandomness) random, status).toString();
    }

    private StringBuilder generateJsonObject(SplitSourceOfRandomness random, GenerationStatus status) {
        StringBuilder json = new StringBuilder("{");
        int numberOfKeys = random.nextInt(0, 10, true);

        for (int i = 0; i < numberOfKeys; i++) {
            if (i > 0) {
                json.append(",");
            }
            String key = generateJsonString(random, status).toString();
            StringBuilder value = generateJsonValue(random, status);
            json.append(key).append(":").append(value);
        }

        json.append("}");
        return json;
    }

    private StringBuilder generateJsonArray(SplitSourceOfRandomness random, GenerationStatus status) {
        StringBuilder array = new StringBuilder("[");
        int numberOfElements = random.nextInt(0, 10, true);

        for (int i = 0; i < numberOfElements; i++) {
            if (i > 0) {
                array.append(",");
            }
            StringBuilder value = generateJsonValue(random, status);
            array.append(value);
        }

        array.append("]");
        return array;
    }

    private StringBuilder generateJsonString(SplitSourceOfRandomness random, GenerationStatus status) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = random.nextInt(0, 10, true);
        StringBuilder str = new StringBuilder("\"");

        for (int i = 0; i < length; i++) {
            char c = chars.charAt(random.nextInt(chars.length(), false));
            str.append(c);
        }

        str.append("\"");
        return str;
    }

    private StringBuilder generateJsonValue(SplitSourceOfRandomness random, GenerationStatus status) {
        int control;
        depth += 1;
        if (depth > 5) {
            control = 5;
        } else {
            control = 7;
        }
        StringBuilder res;
        switch (random.nextInt(control, true)) {
            case 0:
                res = new StringBuilder("null");
                break;
            case 1:
                res = new StringBuilder(random.nextBoolean(false) ? "true" : "false");
                break;
            case 2:
                res = new StringBuilder(String.valueOf(random.nextInt(false)));
                break;
            case 3:
                res = new StringBuilder(String.valueOf(random.nextDouble()));
                break;
            case 4:
                res = generateJsonString(random, status);
                break;
            case 5:
                res = generateJsonArray(random, status);
                break;
            case 6:
                res = generateJsonObject(random ,status);
                break;
            default: throw new RuntimeException("Unreachable");
        }
        depth -= 1;
        return res;
    }
}
