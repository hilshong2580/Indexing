import java.util.ArrayList;
import java.util.List;

public class PostingList {
    private List<Posting> postings = new ArrayList<>();
    private int index;

    public PostingList(){
        this.postings = new ArrayList<>();
        index = 0;
    }
    public boolean haveMore(){
        return postings.size() > index;
    }

    public Posting getCurPosting(){
        if(postings.size()==0)
            return null;
        return this.postings.get(this.index);
    }

    public Posting getIndexPosting(int index){
        return this.postings.get(index);
    }

    public void skipTo(int docid){
        for(int i = 0; i< postings.size(); i++)
            if(postings.get(i).getDocId() >= docid){
                this.index = i;
                break;
            }
    }

    public List<Posting> getPostings(){
        return this.postings;
    }

    public void addPosting(Posting p){
        this.postings.add(p);
        this.index++;
    }

    public int getCurListMaxDocId(){
        Posting posting = postings.get(postings.size() - 1 );
       return posting.getDocId();
    }

    public void setIndex(){
        this.index = 0;
    }
}
