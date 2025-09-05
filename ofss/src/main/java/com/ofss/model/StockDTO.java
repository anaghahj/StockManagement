package com.ofss.model;

public class StockDTO {

    private String symbol;
    private String name;
    private String industry;
    private Double latestPrice;
    private String colorTag;

    public String getColorTag() {
        return colorTag;
    }

    public void setColorTag(String colorTag) {
        this.colorTag = colorTag;
    }

    public StockDTO(String symbol, String name, String industry, Double latestPrice, String colorTag) {
        this.symbol = symbol;
        this.name = name;
        this.industry = industry;
        this.latestPrice = latestPrice;
        this.colorTag = colorTag;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(Double latestPrice) {
        this.latestPrice = latestPrice;
    }

}
