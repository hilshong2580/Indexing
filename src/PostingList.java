import java.util.ArrayList;
import java.util.List;

public class PostingList {
    private List<Posting> postings = new ArrayList<>();
    private int index;

    public PostingList(){
        this.postings = new ArrayList<>();
        this.index = 0;
    }
    public boolean haveMore(){
        return postings.size() > index;
    }

    public Posting getCurPosting(){
        if(postings.size()==0)
            return null;
        else if(this.index >= this.postings.size())
            this.index = this.postings.size()-1;

        return this.postings.get(this.index);
    }

    public Posting getIndexPosting(int index){
        return this.postings.get(index);
    }

    public void skipTo(int docid){
        this.index = 0;
        for(Posting p: postings){
            if(p.getDocId() < docid){
                this.index++;
            }
        }
    }


    public List<Posting> getPostings(){
        return this.postings;
    }

    public void addPosting(Posting p){
        this.postings.add(p);
    }

    public int getCurListMaxDocId(){
        Posting posting = postings.get(postings.size() - 1 );
       return posting.getDocId();
    }

    public int getIndex(){
        return this.index;
    }
    public void resetIndex(){
        this.index = 0;
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "postings=" + postings +
                ", index=" + index +
                '}';
    }
}
