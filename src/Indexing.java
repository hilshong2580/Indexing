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
    private Map<String, PostingList> postingMap = new HashMap<String, PostingList>();
    private String[] playId;
    private String[] sceneId;

    public static void main(String[] args) throws IOException {
        Indexing indexing = new Indexing();
        indexing.load("shakespeare-scenes.json");
        indexing.indexingProcess();
        indexing.termBasedQueries0();
        indexing.termBased123();
       // indexing.testProcess();
    }

    public void termBasedQueries0(){
        HashMap<Integer, Integer> thee = getDocidSet(postingMap.get("thee"));
        HashMap<Integer, Integer> thou = getDocidSet(postingMap.get("thou"));
        HashMap<Integer, Integer> you = getDocidSet(postingMap.get("you"));
        HashSet<Integer> set = new HashSet<>();

        thou.forEach((k,v)->{
            if(!thee.containsKey(k))
                thee.put(k, v);
            else if(thou.get(k) > thee.get(k))
                thee.put(k, v);
        });

        thee.forEach((k,v)->{
            if(you.containsKey(k))
                if(v > you.get(k))
                    set.add(k);
        });
        ArrayList<String> list = new ArrayList<>();
        for(int i: set)
            list.add(sceneId[i]);
        Collections.sort(list);

        writeterm(list, "term0.txt");
    }

    public void termBased123(){
        HashSet<Integer> set = new HashSet<>();
        set.addAll(findWord("venice"));
        set.addAll(findWord("rome"));
        set.addAll(findWord("denmark"));

        ArrayList<String> list = new ArrayList<String>();
        for(int i : set)
            list.add(sceneId[i]);
        Collections.sort(list);
        writeterm(list, "term1.txt");


        HashSet<Integer> set2 = new HashSet<>();
        set2.addAll(findWord("goneril"));
        ArrayList<String> list2 = new ArrayList<String>();
        for(int i : set2)
            list2.add(playId[i]);
        Collections.sort(list2);
        writeterm(list2, "term2.txt");

        HashSet<Integer> set3 = new HashSet<>();
        set3.addAll(findWord("soldier"));
        ArrayList<String> list3 = new ArrayList<String>();
        for(int i : set3)
            list3.add(playId[i]);
        Collections.sort(list3);
        writeterm(list3, "term3.txt");

    }

    public HashSet<Integer> findWord(String str){
        HashSet<Integer> set = new HashSet<>();
        if(postingMap.containsKey(str))
            getDocidSet(postingMap.get(str)).forEach((k,v)->{
                set.add(k);
            });
        return set;
    }

    public HashMap<Integer, Integer> getDocidSet(PostingList L){
        HashMap<Integer, Integer> map = new HashMap();
        for(Posting post : L.getPostings())
            map.put(post.getDocId(), post.getAllPositions().size());
        return map;
    }
    private void load(String inFile) {
    int c = 0;
        try {
            // parsing file "JSONExample.json"
            Object obj = new JSONParser().parse(new FileReader(inFile));

            // typecasting obj to JSONObject
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("corpus");

            // iterating address Map
            itr = ja.iterator();
            playId = new String[ja.size()+1];
            sceneId = new String[ja.size()+1];

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

    public void testProcess(){
        PostingList L = postingMap.get("can");
        //System.out.println(L.getCurListMaxDocId());
        Iterator postingItr = L.getPostings().iterator();
        while (postingItr.hasNext()) {
            Posting posting = (Posting) postingItr.next();
            System.out.println(posting.getDocId());
        }
    }

    public void indexingProcess(){
        int docid = 0;
        while(itr.hasNext()){
            docid +=1;
            JSONObject d = (JSONObject) itr.next();
            playId[docid] = d.get("playId").toString();
            sceneId[docid] = d.get("sceneId").toString();
            String[] tokens = d.get("text").toString().replaceAll("\\s+", " ").split(" ");
            int position = 0;
            Iterator tokenItr = Arrays.stream(tokens).iterator();
            while(tokenItr.hasNext()){
                position += 1;
                String token = tokenItr.next().toString();

                if(!postingMap.containsKey(token))
                    postingMap.put(token, new PostingList());


                AtomicBoolean check = new AtomicBoolean(false);
                int temp = docid;
                int finalPosition = position;

                PostingList postinglist = postingMap.get(token);
                postinglist.getPostings().forEach((e)->{
                    if(e.getDocId() == temp){
                        check.set(true);
                        e.addPositions(finalPosition);
                    }
                });

                if(!check.get()){
                    postinglist.addPosting(new Posting(docid, position));
                }

                postingMap.put(token, postinglist);
            }
        }
//        postingMap.forEach((k,v)->{
//            System.out.print(k+" ");
//            v.getPostings().forEach((e)->{
//                e.getAllPositions().forEach((post)->{
//                    System.out.print("[docid:"+e.getDocId()+", position:"+post+"] ");
//                });
//            });
//            System.out.println(" ");
//        });
//        System.out.println(postingMap.size());
    }

    public PostingList intersectingPostingLists(PostingList[] L){
        ArrayList<Posting> matchingPostings = new ArrayList<>();
        PostingList newList = new PostingList();

        //check all PostingList is L have more
        boolean check = true;
        for(PostingList l : L){
            if(l.haveMore() == false)
                check = false;
        }

        while (check){
            int candidate = maxDocid(L);
            for(PostingList l : L)
                l.skipTo(candidate);

            if(allMatch(L, candidate)){
                for(PostingList l : L){
                    matchingPostings.add(l.getCurPosting());
                }
                if(matchingWindows(matchingPostings) != null)
                    newList.addPosting(matchingWindows(matchingPostings));
            }
            for(PostingList l : L)
                l.skipTo(candidate+1);
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
                    int cur = k;
                    if(prev < cur && cur <= (prev+distance)){
                        found = true;
                        prev = cur;
                        break;
                    }
                }
                if(found == false)
                    break;
            }
            if(found)
                if(result == null)
                    result = new Posting(l.get(0).getDocId(), p0.get(i));
                else
                    result.addPositions(p0.get(i));
        }
        return result;
    }

    public int maxDocid(PostingList[] L){
        int maxId = -1;
        for(PostingList l : L){
            if(l.getCurPosting().getDocId() > maxId)
                maxId = l.getCurPosting().getDocId();
        }
        return maxId;
    }

    public boolean allMatch(PostingList[] L, int candidate){
        for(PostingList l : L)
            if(!(l.getCurPosting().getDocId() == candidate))
                return false;
        return true;
    }

    private void writeterm(ArrayList<String> list, String str){
        try {
            FileWriter myWriter = new FileWriter(str);
            for (String i: list) {
                //output the content align
                String temp = String.format("%-50s", i)+"\n";
                myWriter.write(temp);
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("error: term0");
            e.printStackTrace();
        }
    }
}


