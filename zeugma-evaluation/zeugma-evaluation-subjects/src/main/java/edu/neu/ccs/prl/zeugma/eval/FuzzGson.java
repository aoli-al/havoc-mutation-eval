package edu.neu.ccs.prl.zeugma.eval;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.pholser.junit.quickcheck.From;
import de.hub.se.jqf.examples.json.SplitJsonGenerator;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.neu.ccs.prl.zeugma.generators.JsonGenerator;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class FuzzGson {

    private Gson gson = new Gson();

    @Fuzz
    public void testWithGenerator(@From(JsonGenerator.class) String input) {
        try {
            gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
    }

    @Fuzz
    public void testWithSplitGenerator(@From(SplitJsonGenerator.class) String input) {
        try {
            gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
    }
}