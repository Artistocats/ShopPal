package com.shoppalteam.shoppal.misc;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product implements Serializable
{
    private final int id;
    private String name;
    private double price;
    private int quantity;

    private String lastBuy;
    private double buyPeriod;
    private int timesBought;

    private boolean selected;
    private boolean toBeDeleted=false;

    private int color;

    public Product(int id, String name, double price, int quantity,String lb, double bp, int tb) {
        this.id=id;
        this.name=name;
        this.price=price;
        this.quantity=quantity;

        lastBuy = lb;
        buyPeriod =bp;
        timesBought =tb;

        generateColor();
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        BigDecimal pP = BigDecimal.valueOf(price);
        BigDecimal pQ = BigDecimal.valueOf(quantity);
        pP = pP.multiply(pQ);
        return pP.setScale(2, RoundingMode.CEILING);
    }

    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    public String getLastBuy() {
        return lastBuy;
    }

    public double getBuyPeriod() {
        return buyPeriod;
    }

    public int getTimesBought() {
        return timesBought;
    }

    public int getColor() {
        return color;
    }

    //Generates the product's color, returning true if color changed
    public boolean generateColor() {
        int tempColor;

        if((timesBought>1)&&(buyPeriod>0)) {
            String now = Utils.getNow();
            Duration duration;

            duration = Utils.getDuration(lastBuy,now);
            BigDecimal durationBD = BigDecimal.valueOf(duration.getStandardSeconds()).divide(Utils.SECONDS_PER_HOUR, Utils.DIGITS_PRECISION, RoundingMode.HALF_EVEN);
            BigDecimal bP = new BigDecimal(buyPeriod);

            durationBD = durationBD.divide(bP,3, RoundingMode.HALF_EVEN);

            double percentage=durationBD.doubleValue()*100;

            if(percentage<50)
                tempColor=1;
            else if (percentage<80)
                tempColor=2;
            else if (percentage<110)
                tempColor=3;
            else
                tempColor=4;
        }
        else
            tempColor=0;

        if(color!=tempColor) {
            color=tempColor;
            return true;
        }
        else
            return false;
    }

    //Returns DateTime of lastbuy + buyPeriod
    public DateTime getDepletionDate() {
        DateTime depletionDate=null;
        if((timesBought>1)&&(buyPeriod>0))
        {
            depletionDate= Utils.stringToDateTime(lastBuy);
            depletionDate=depletionDate.plus((Double.valueOf(buyPeriod*60*60*1000)).longValue());
        }
        return depletionDate;
    }
}
