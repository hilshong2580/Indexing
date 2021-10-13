import java.util.List;

public class Posting {
    private int docId;
    private List<Integer> positions;

    public int getDocId(){
        return docId;
    }

    public void setDocId(int number){
        docId = number;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void add(int number){
        positions.add(number);
    }

    public void remove(int index){
        positions.remove(index);
    }

    public int get(int index){
        return positions.get(index);
    }
}
