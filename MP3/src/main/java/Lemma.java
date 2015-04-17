import java.io.IOException;
import java.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

public class Lemma {
    public static void main(String[] args) throws IOException,
            ClassNotFoundException {

        // Initialize the tagger
        final MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");

        // The sample string
        final String sample1 = "This is a sample text.";
        final String sample2 = "The sailor dogs the hatch.";

        // The tagged string
        final String tagged1 = tagger.tagString(sample1);
        final String tagged2 = tagger.tagString(sample2);

        // Output the result
        System.out.println(tagged1);
        System.out.println(tagged2);
    }
}