public class MasterStudent extends Student implements ConferenceScoreBehaviour {
    int numberOfConf;
    MasterStudent(int id, float mid, float fin, int numberOfConf) {
        super(id, mid, fin);
        this.numberOfConf = numberOfConf;
    }
    float computeTotalScore() {
        return computeBaseScore()+conferenceScore();
    }
    @Override
    public float conferenceScore() {
        return numberOfConf*5;
    }
    @Override
    public String toString() {
        return "This is a Master Student with the following academic achievements" + "\n" +
                "ID: " + id + "\n" +
                "Midterm Score: " + mid + "\n" +
                "Final Score: " + fin + "\n" +
                "Number of Conferences: " + numberOfConf + "\n" +
                "Conference Score: " + conferenceScore() + "\n" +
                "Total Score: " + computeTotalScore() + "\n";
    }
}