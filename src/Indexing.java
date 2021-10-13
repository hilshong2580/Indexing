import java.io.IOException;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Indexing {


    public static void main(String[] args) throws IOException {
        System.out.println("first");
        Indexing indexing = new Indexing();
        indexing.load("shakespeare-scenes.json");
    }

    private void load(String inFile) {

        try {
            // parsing file "JSONExample.json"
            Object obj = new JSONParser().parse(new FileReader(inFile));

            // typecasting obj to JSONObject
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("corpus");
            // iterating address Map
            Iterator itr2 = ja.iterator();

            while (itr2.hasNext())
            {
                Iterator<Map.Entry> itr1 = ((Map) itr2.next()).entrySet().iterator();
                while (itr1.hasNext()) {
                    Map.Entry pair = itr1.next();
                    System.out.println(pair.getKey() + " : " + pair.getValue());
                }
            }




        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private static void parseEmployeeObject(JSONObject employee)
    {
        //Get employee object within list
        JSONObject employeeObject = (JSONObject) employee.get("corpus");

        //Get employee first name
        String sceneId = (String) employeeObject.get("sceneId");
        System.out.println(sceneId);

        //Get employee last name
        String sceneNum = (String) employeeObject.get("sceneNum");
        System.out.println(sceneNum);

        //Get employee website name
        String text = (String) employeeObject.get("text");
        System.out.println(text);
    }
}
/**
 * Given a collection of documents, C
 * docid = 0;
 * while C has more documents
 * docid = docid +1;
 * d = next Document in C
 * documentNames[docid] - d.getName();
 * tokens = parse(d)
 * position = 0;
 * while tokens has lmore tokens
 *  position = position +1;
 *  token = next token in tokens
 *  postings = invertedLists[token]
 *  postings.add(docid, position)
 *  write inverted lists and other data to disk
 *
 *  Given a list of positions, L, all for the same document and a distance between the terms in the phrase
 *  p0 = the positions array of the first Posting in L
 *      for each position prev in p0
 *          p=the positions array of post;
 *          found = false
 *          for each position cur in p
 *              if( prev < cur && cur <= prev + distance)
 *                  found - true
 *                  prev = cur
 *                  break;
 *
 */
