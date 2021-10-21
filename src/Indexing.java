import java.io.IOException;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Indexing{

    private Iterator itr;
    private Map<String, PostingList> invertedList = new HashMap<>();
    private String[] playId;
    private String[] sceneId;

    public static void main(String[] args) {
        Indexing indexing = new Indexing();
        indexing.load();
        indexing.indexingProcess();
        indexing.TermBased("thee thou", "you", "sceneId", "term0.txt");
        indexing.TermBased("venice rome denmark", null, "sceneId", "term1.txt");
        indexing.TermBased("goneril", null, "playId", "term2.txt");
        indexing.TermBased("soldier", null, "playId", "term3.txt");

        indexing.phraseBasedQueries("poor yorick", "phrase0.txt");
        indexing.phraseBasedQueries("wherefore art thou romeo", "phrase1.txt");
        indexing.phraseBasedQueries("let slip", "phrase2.txt");

    }
    public void test(){
        invertedList.forEach((k,v)->{
            v.getPostings().forEach((e)-> e.getAllPositions().forEach((post)-> System.out.print("[docid:"+e.getDocId()+", position:"+post+"] ")));
            System.out.println(" ");
        });
        System.out.println(invertedList.size());
    }

    public void indexingProcess(){
        //initial the docid is 0
        int docid = 0;

        //while the JSON array has next
        while(itr.hasNext()){
            docid +=1;
            //get the JSON object from itr
            JSONObject d = (JSONObject) itr.next();

            //save the playId and sceneId
            playId[docid] = d.get("playId").toString();
            sceneId[docid] = d.get("sceneId").toString();

            //remove the extra space from text
            String[] tokens = d.get("text").toString().replaceAll("\\s+", " ").split(" ");

            //initial the position to zero
            int position = 0;

            //create a Iterator to do the while loop
            Iterator tokenItr = Arrays.stream(tokens).iterator();
            while(tokenItr.hasNext()){
                position += 1;

                //get the token
                String token = tokenItr.next().toString();

                //put a posting list if invertedList doesn't contain term
                if(!invertedList.containsKey(token))
                    invertedList.put(token, new PostingList());

                //check the action in below
                AtomicBoolean check = new AtomicBoolean(false);
                int temp = docid;
                int finalPosition = position;

                PostingList postinglist = invertedList.get(token);

                //add the position to posting if contain docid in postinglist
                postinglist.getPostings().forEach((e)->{
                    if(e.getDocId() == temp){
                        check.set(true);
                        e.addPositions(finalPosition);
                    }
                });

                //add new posting with docid and position to posting if not contain docid in postingList
                if(!check.get()){
                    postinglist.addPosting(new Posting(docid, position));
                }

                //update the invertedList
                invertedList.put(token, postinglist);
            }
        }
    }


    public PostingList intersectingPostingLists(List<PostingList> L){

        //create a empty list of Posting and a PostingList
        ArrayList<Posting> matchingPostings = new ArrayList<>();
        PostingList newList = new PostingList();

        //checkHaveMore will return reture if size of Posting list larger than the index for L
        while (checkHaveMore(L)){
            //max over current docid in list in L
            int candidate = maxDocid(L);

            //put the index pointer to the candidate's docid id
            for(PostingList l : L)
                l.skipTo(candidate);

            if(allMatch(L, candidate)){
                for(PostingList l : L){
                    matchingPostings.add(l.getCurPosting());
                }

                if(matchingWindows(matchingPostings) != null)
                    newList.addPosting(matchingWindows(matchingPostings));

            }

            for(PostingList l : L){
                l.skipTo(candidate+1);
            }
            matchingPostings.clear();
        }

        newList.setIndex();
        return newList;
    }

    public Posting matchingWindows(ArrayList<Posting> l){

        int distance = 1;
        boolean found = false;
        Posting result = null;
        List<Integer> p0 = l.get(0).getAllPositions();
        for(int i : p0){
            int prev = i;
            for(int j = 1; j < l.size(); j++){
                List<Integer> p = l.get(j).getAllPositions();
                found = false;
                for(int k : p){
                    if(prev < k && k <= (prev+distance)){
                        found = true;
                        prev = k;
                        break;
                    }
                }
                if(!found)
                    break;
            }
            if(found)
                if(result == null){
                    result = new Posting(l.get(0).getDocId(), i);
                }else
                    result.addPositions(p0.get(i));
        }

        return result;
    }

    //Doing the term based queries
    public void TermBased(String contain, String compare, String typeName, String output){
        //the hash set to save the play id or scene id
        HashSet<Integer> set = new HashSet<>();

        //save the playid or scene id
        HashSet<String> able = new HashSet<>();

        //save the docid if the first frequency larger second
        //getDocidMap = HashMap<docid, frequency>
        if(compare != null){
            HashMap<Integer, Integer> compareMap = getDocidMap(compare);
            for (String s : contain.split(" ")) {
                //s->(thee,thou); compareMap->you; k = docid, v = value
                //if "you" contain key and its frequency lower than s frequency
                getDocidMap(s).forEach((k,v)->{
                    if(compareMap.containsKey(k))
                        if(v > compareMap.get(k))
                            set.add(k);
                });
            }
        //save the all docid based on the term from invertedList
        } else {
            for (String s : contain.split(" ")) {
                getDocidMap(s).forEach((k,v)-> set.add(k));
            }
        }

        //identify to type to content
        if(typeName.equals("playId")){
            for(int i: set)
                able.add(playId[i]);
        } else if(typeName.equals("sceneId")){
            for(int i: set)
                able.add(sceneId[i]);
        }

        //save the hashSet to string array list to sort in writeTerm
        ArrayList<String> list = new ArrayList<>(able);

        //call output file function
        writeTerm(list, output);
    }

    //Doing the phrase based queries
    public void phraseBasedQueries(String InputStr, String fileName){
        //create a list of PostingList for the intersectingPostingLists() function
        List<PostingList> L = new ArrayList<>();

        //create a string list of save the scene id
        ArrayList<String> sceneIdList = new ArrayList<>();

        //split the input string to string array
        String[] str = InputStr.split(" ");

        //add the PostingList into L
        for(String s : str)
            if(invertedList.containsKey(s))
                L.add(invertedList.get(s));

        //save docid from return PostingList based on Intersecting PostingLists function
        intersectingPostingLists(L).getPostings().forEach(e-> sceneIdList.add(sceneId[e.getDocId()]));

        //call output file function
        writeTerm(sceneIdList, fileName);
    }

    //To return a HashMap<docid, frequency> from invertedList with term
    //This make the Term-based Queries and Phrase-based Queries more performance
    public HashMap<Integer, Integer> getDocidMap(String str){
        HashMap<Integer, Integer> map = new HashMap<>();
        for(Posting post : invertedList.get(str).getPostings())
            map.put(post.getDocId(), post.getAllPositions().size());
        return map;
    }

    //check all PostingList that the current posting have next
    public boolean checkHaveMore(List<PostingList> L){
        boolean check = true;
        for(PostingList l : L){
            //return false if exist no more posting in postingList
            if(!l.haveMore())
                check = false;
        }
        return check;
    }

    //find the largest docid from current list of PostingList with same index
    public int maxDocid(List<PostingList> L){
        int maxId = -1;     //initial the max over docid to -1
        for(PostingList l : L){
            //update the value is current docid larger than maxId
            if(l.getCurPosting().getDocId() > maxId)
                maxId = l.getCurPosting().getDocId();
        }
        return maxId;
    }

    //function to check docid from the list of PostingList
    public boolean allMatch(List<PostingList> L, int candidate){
        //return true if all docid equal to candidate
        for(PostingList l : L) {
            if (!(l.getCurPosting().getDocId() == candidate))
                return false;
        }
        return true;
    }

    //output file to txt
    private void writeTerm(ArrayList<String> list, String str){
        //sort the output list
        Collections.sort(list);

        try {
            FileWriter myWriter = new FileWriter(str);
            for (String i: list) {
                //output the content align
                String temp = i+"\n";
                myWriter.write(temp);
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("error: term0");
            e.printStackTrace();
        }
    }

    //load the JSON file, then store it into to private Iterator itr
    private void load() {
        try {
            // parsing file "JSONExample.json"
            Object obj = new JSONParser().parse(new FileReader("shakespeare-scenes.json"));

            // typecasting obj to JSONObject
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("corpus");

            // iterating address Map
            itr = ja.iterator();
            playId = new String[ja.size()+1];
            sceneId = new String[ja.size()+1];

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void printInvertedList(){
        invertedList.forEach((k,v)->{
            System.out.print(k+" ");
            v.getPostings().forEach((e)-> e.getAllPositions().forEach((post)-> System.out.print("[docid:"+e.getDocId()+", position:"+post+"] ")));
            System.out.println(" ");
        });
        System.out.println(invertedList.size());
    }

}