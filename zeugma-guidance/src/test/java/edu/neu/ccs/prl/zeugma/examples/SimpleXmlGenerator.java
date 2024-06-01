package edu.neu.ccs.prl.zeugma.examples;

import edu.neu.ccs.prl.zeugma.internal.guidance.DataProvider;

public class SimpleXmlGenerator {
    private DataProvider provider;

    public String generateXml(DataProvider provider) {
        this.provider = provider;
        String name = generateString();
        return generateElement(name);
    }

    public boolean nextBoolean() {
        return provider.nextBoolean();
    }

    public String generateElement(String name) {
        String[] attributes = new String[provider.nextInt(3)];
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = String.format("%s='%s'", generateString(), generateString());
        }
        String content = "";
        if (nextBoolean()) {
            String[] children = new String[provider.nextInt(1, 4)];
            for (int i = 0; i < children.length; i++) {
                children[i] = generateElement(generateString());
            }
            content = String.join("", children);
        } else if (nextBoolean()) {
            content = generateString();
        }
        return String.format("<%s %s>%s</%s>", name, String.join(" ", attributes), content, name);
    }

    private String generateString() {
        return String.valueOf(provider.nextChar('a', 'z'));
    }
}
