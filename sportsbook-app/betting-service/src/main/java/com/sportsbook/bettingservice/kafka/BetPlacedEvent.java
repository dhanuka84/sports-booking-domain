package com.sportsbook.bettingservice.kafka;

import java.util.Objects;

public class BetPlacedEvent {
    private String betId;
    private String userId;
    private String matchId;
    private String selection;
    private double stake;
    private double odds;

    public BetPlacedEvent() {
    }

    public BetPlacedEvent(String betId, String userId, String matchId, String selection, double stake, double odds) {
        this.betId = betId;
        this.userId = userId;
        this.matchId = matchId;
        this.selection = selection;
        this.stake = stake;
        this.odds = odds;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public double getStake() {
        return stake;
    }

    public void setStake(double stake) {
        this.stake = stake;
    }

    public double getOdds() {
        return odds;
    }

    public void setOdds(double odds) {
        this.odds = odds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BetPlacedEvent that = (BetPlacedEvent) o;
        return Double.compare(that.stake, stake) == 0 &&
                Double.compare(that.odds, odds) == 0 &&
                Objects.equals(betId, that.betId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(matchId, that.matchId) &&
                Objects.equals(selection, that.selection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(betId, userId, matchId, selection, stake, odds);
    }

    @Override
    public String toString() {
        return "BetPlacedEvent{" +
                "betId='" + betId + '\'' +
                ", userId='" + userId + '\'' +
                ", matchId='" + matchId + '\'' +
                ", selection='" + selection + '\'' +
                ", stake=" + stake +
                ", odds=" + odds +
                '}';
    }
}