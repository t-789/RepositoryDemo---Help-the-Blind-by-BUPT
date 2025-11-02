package org.example.RepositoryDemo.entity;

public class QuestionSet {
    public String question1;
    public String question2;
    public String ans1;
    public String ans2;

    public QuestionSet() {
    }
    public QuestionSet(String question1, String question2, String ans1, String ans2) {
        this.question1 = question1;
        this.question2 = question2;
        this.ans1 = ans1;
        this.ans2 = ans2;
    }
    public String toString() {
        return "QuestionSet{" +
                "question1=" + question1 +
                ", question2=" + question2 +
                ", ans1='" + ans1 + '\'' +
                ", ans2='" + ans2 + '\'' +
                '}';
    }
}
