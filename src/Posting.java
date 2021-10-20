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

    public void setDocId(int number){
        this.docId = number;
    }

    public List<Integer> getAllPositions() {
        return this.positions;
    }

    public void addPositions(int number){
        this.positions.add(number);
    }

    public void removePositions(int index){
        this.positions.remove(index);
    }

    public int getPositions(int index){
        return this.positions.get(index);
    }



}
