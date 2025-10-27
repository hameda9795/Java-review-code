package com.devmentor.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long specialUsers;
    private Long totalReviews;
    private Long reviewsToday;
    private Long reviewsThisWeek;
    private Long reviewsThisMonth;
}
