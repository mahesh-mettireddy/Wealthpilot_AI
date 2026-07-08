package com.leostar.wealthpilot.model;

import java.util.List;

public class ProjectionResponse {

    private double blendedAnnualReturnPct;
    private long projectedCorpus;
    private long totalInvested;
    private List<YearlyProjection> yearlyBreakdown;

    // Getters and Setters
    public double getBlendedAnnualReturnPct() { return blendedAnnualReturnPct; }
    public void setBlendedAnnualReturnPct(double blendedAnnualReturnPct) { this.blendedAnnualReturnPct = blendedAnnualReturnPct; }

    public long getProjectedCorpus() { return projectedCorpus; }
    public void setProjectedCorpus(long projectedCorpus) { this.projectedCorpus = projectedCorpus; }

    public long getTotalInvested() { return totalInvested; }
    public void setTotalInvested(long totalInvested) { this.totalInvested = totalInvested; }

    public List<YearlyProjection> getYearlyBreakdown() { return yearlyBreakdown; }
    public void setYearlyBreakdown(List<YearlyProjection> yearlyBreakdown) { this.yearlyBreakdown = yearlyBreakdown; }

    public static class YearlyProjection {
        private int year;
        private long corpus;

        public YearlyProjection(int year, long corpus) {
            this.year = year;
            this.corpus = corpus;
        }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public long getCorpus() { return corpus; }
        public void setCorpus(long corpus) { this.corpus = corpus; }
    }
}
