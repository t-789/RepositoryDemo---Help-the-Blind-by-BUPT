package org.example.RepositoryDemo.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Statistic {
    // getter and setter methods
    public int point_count;
    public int user_count;
    public int post_count;

    public Statistic(int point_count, int user_count, int post_count) {
        this.point_count = point_count;
        this.user_count = user_count;
        this.post_count = post_count;
    }
    public Statistic() {
    }

}