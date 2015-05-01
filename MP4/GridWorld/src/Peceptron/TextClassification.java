package Peceptron;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by manshu on 5/1/15.
 */
public class TextClassification {
    Set<String> dictionary;

    public void readConfig(String file) throws IOException {
        dictionary = new HashSet<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            int index = 0;
            while (line.charAt(index++) != ' ');
            int tes

        }
    }

    public static void main(String[] args) {

    }
}
