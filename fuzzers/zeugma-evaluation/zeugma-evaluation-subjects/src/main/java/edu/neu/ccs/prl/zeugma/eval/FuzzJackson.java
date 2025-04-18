package edu.neu.ccs.prl.zeugma.eval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.pholser.junit.quickcheck.From;
import de.hub.se.jqf.examples.json.SplitJsonGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.neu.ccs.prl.zeugma.generators.JsonGenerator;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class FuzzJackson {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Fuzz
    public void testWithGenerator(@From(JsonGenerator.class) String input) {
        try {
            objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
            Assume.assumeNoException(e);
        }
    }

    @Fuzz
    public void testWithSplitGenerator(@From(SplitJsonGenerator.class) String input) {
        try {
            objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
            Assume.assumeNoException(e);
        }
    }
}
