public class PhDStudent extends MasterStudent implements ArticleScoreBehaviour {
    int numberOfArticles;
    PhDStudent(int id, float mid, float fin, int numberOfConf, int numberOfArticles) {
        super(id, mid, fin, numberOfConf);
        this.numberOfArticles = numberOfArticles;
    }
    float computeTotalScore() {
        return computeBaseScore()+articleScore();
    }
    @Override
    public float articleScore() {
        return numberOfArticles*8;
    }
    @Override
    public String toString() {
        return "This is a PhD Student with the following academic achievements" + "\n" +
                "ID: " + id + "\n" +
                "Midterm Score: " + mid + "\n" +
                "Final Score: " + fin + "\n" +
                "Number of Conferences: " + numberOfConf + "\n" +
                "Conference Score: " + conferenceScore() + "\n" +
                "Number of Articles: " + numberOfArticles + "\n" +
                "Article Score: " + articleScore() + "\n" +
                "Total Score: " + computeTotalScore();
    }
}