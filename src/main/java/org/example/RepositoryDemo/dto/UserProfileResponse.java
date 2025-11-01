package org.example.RepositoryDemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {
    private int pointCount;
    private int postCount;
    private int credit;
    
    public UserProfileResponse() {
    }
    
    public UserProfileResponse(int pointCount, int postCount, int credit) {
        this.pointCount = pointCount;
        this.postCount = postCount;
        this.credit = credit;
    }
}