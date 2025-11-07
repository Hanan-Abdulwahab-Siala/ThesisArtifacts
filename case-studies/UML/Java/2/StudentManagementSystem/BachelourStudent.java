public class BachelourStudent extends Student implements ProjectScoreBehaviour {
    BachelourStudent(int id, float mid, float fin) {
        super(id, mid, fin);
    }
    float computeTotalScore() {
        return computeBaseScore()+projectScore();
    }
    @Override
    public float projectScore() {
        return 20f;
    }
    @Override
    public String toString() {
        return "\n" + "This is a Bachelour Student with the following academic achievements" + "\n" +
                "ID: " + id + "\n" +
                "Midterm Score: " + mid + "\n" +
                "Final Score: " + fin + "\n" +
                "Project Score: " + projectScore() + "\n" +
                "Total Score: " + computeTotalScore() + "\n";
    }
}