package com.atl.backend.util;

import java.util.Comparator;

import com.atl.backend.model.FootballStanding;

public class StandingsComparators {
    public static final Comparator<FootballStanding> FOOTBALL_STANDING = Comparator.comparingInt(FootballStanding::getPts)
            .reversed()
            .thenComparing(Comparator.comparingInt(FootballStanding::getGoalDifference).reversed())
            .thenComparing(Comparator.comparingInt(FootballStanding::getGoalsScored).reversed())
            .thenComparingInt(FootballStanding::getGoalsConceded); // ascending

    private StandingsComparators() {
    }
}
