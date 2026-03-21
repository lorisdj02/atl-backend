package com.atl.backend.util;

import java.util.Comparator;

import com.atl.backend.model.FootballStanding;

public class StandingsComparators {
    public static final Comparator<FootballStanding> FOOTBALL_STANDING = Comparator.comparingInt(FootballStanding::getPts)
            .reversed()
            .thenComparingInt(FootballStanding::getGoalDifference).reversed()
            .thenComparingInt(FootballStanding::getGoalsScored).reversed()
            .thenComparingInt(FootballStanding::getGoalsConceded); // ascending

    private StandingsComparators() {
    }
}
