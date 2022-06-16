package com.cankocak.rpi_tracker;

import java.time.ZonedDateTime;

public class RSS_Item implements Comparable<RSS_Item> {
    private final String title;
    private final String description;
    private final String link;
    private final ZonedDateTime date;

    // RPi Locator Specific Attributes
    private final String vendor;
    private final String country;
    private final String device;

    public RSS_Item(String title, String description, String link, ZonedDateTime date,
                    String vendor, String country, String device) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.date = date;
        this.vendor = vendor;
        this.country = country;
        this.device = device;
    }

    public RSS_Item(String title, String description, String link, ZonedDateTime date) {
        this(title, description, link, date, null, null, null);
    }

    @Override
    public int compareTo(RSS_Item rss_item) {
        if (this.date == null) {
            return -1;
        } else if (rss_item.date == null) {
            return 1;
        }
        return this.date.compareTo(rss_item.date);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public String getVendor() {
        return vendor;
    }

    public String getCountry() {
        return country;
    }

    public String getDevice() {
        return device;
    }
}
