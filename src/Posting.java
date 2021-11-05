import java.util.ArrayList;
import java.util.List;

public class Posting {
    private int docId;
    private List<Integer> positions;

    public Posting(int id, int post){
        this.docId = id;
        this.positions = new ArrayList<>();
        this.positions.add(post);
    }

    public int getDocId(){
        return this.docId;
    }

    public List<Integer> getAllPositions() {
        return this.positions;
    }

    public void addPositions(int number){
        this.positions.add(number);
    }



}
