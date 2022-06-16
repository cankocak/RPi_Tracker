package com.cankocak.rpi_tracker;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class RSS_Parser {
    public List<RSS_Item> parse(String rssFeed) throws IOException, XmlPullParserException {
        List<RSS_Item> retval = new ArrayList<RSS_Item>();

        String itemTitle = null;
        String itemDescription = null;
        String itemLink = null;
        ZonedDateTime itemDate = null;
        String itemCategoryVendor = null;
        String itemCategoryCountry = null;
        String itemCategoryDevice = null;

        boolean insideTitle = false;
        boolean insideDescription = false;
        boolean insideLink = false;
        boolean insideDate = false;
        boolean insideCategory = false;
        int categoryCount = 0;

        XmlPullParser parser = Xml.newPullParser();
        StringReader reader = new StringReader(rssFeed);
        parser.setInput(reader);
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("item")) {
                    // Start of a new item
                    itemTitle = null;
                    itemDescription = null;
                    itemLink = null;
                    itemDate = null;
                    categoryCount = 0;
                } else if (parser.getName().equals("title")) {
                    insideTitle = true;
                } else if (parser.getName().equals("description")) {
                    insideDescription = true;
                } else if (parser.getName().equals("link")) {
                    insideLink = true;
                } else if (parser.getName().equals("pubDate")) {
                    insideDate = true;
                } else if (parser.getName().equals("category")) {
                    insideCategory = true;
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("item")) {
                    // End of item
                    retval.add(new RSS_Item(itemTitle, itemDescription, itemLink, itemDate,
                            itemCategoryVendor, itemCategoryCountry, itemCategoryDevice));
                } else if (parser.getName().equals("title")) {
                    insideTitle = false;
                } else if (parser.getName().equals("description")) {
                    insideDescription = false;
                } else if (parser.getName().equals("link")) {
                    insideLink = false;
                } else if (parser.getName().equals("pubDate")) {
                    insideDate = false;
                } else if (parser.getName().equals("category")) {
                    insideCategory = false;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (insideTitle) {
                    itemTitle = parser.getText();
                } else if (insideDescription) {
                    itemDescription = parser.getText();
                } else if (insideLink) {
                    itemLink = parser.getText();
                } else if (insideDate) {
                    itemDate = ZonedDateTime.parse(parser.getText(), FeedViewModel.dateFormatter);
                } else if (insideCategory) {
                    switch (categoryCount) {
                        case 0:
                            itemCategoryVendor = parser.getText();
                            break;
                        case 1:
                            itemCategoryCountry = parser.getText();
                            break;
                        case 2:
                            itemCategoryDevice = parser.getText();
                            break;
                        default:
                            break;
                    }
                    ++categoryCount;
                }
            }
            eventType = parser.next();
        }
        reader.close();

        return retval;
    }
}
