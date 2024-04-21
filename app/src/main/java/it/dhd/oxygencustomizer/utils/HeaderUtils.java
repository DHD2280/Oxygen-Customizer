package it.dhd.oxygencustomizer.utils;

/*
 *  Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HeaderUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "BrowseHeaderActivity";

    public static class DaylightHeaderInfo {
        public int mType = 0;
        public int mHour = -1;
        public int mDay = -1;
        public int mMonth = -1;
        public String mImage;
        public String mName;
    }

    public static String loadHeaders(Resources res, String headerName, List<DaylightHeaderInfo> headersList) throws XmlPullParserException, IOException {
        headersList.clear();
        String creatorName = null;
        InputStream in = null;
        XmlPullParser parser = null;

        try {
            if (headerName == null) {
                if (DEBUG) Log.i(TAG, "Load header pack config daylight_header.xml");
                in = res.getAssets().open("daylight_header.xml");
            } else {
                int idx = headerName.lastIndexOf(".");
                String headerConfigFile = headerName.substring(idx + 1) + ".xml";
                if (DEBUG) Log.i(TAG, "Load header pack config " + headerConfigFile);
                in = res.getAssets().open(headerConfigFile);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(in, "UTF-8");
            creatorName = loadResourcesFromXmlParser(parser, headersList);
        } finally {
            // Cleanup resources
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
        return creatorName;
    }

    private static String loadResourcesFromXmlParser(XmlPullParser parser, List<DaylightHeaderInfo> headersList) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String creatorName = null;
        do {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase("day_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 0;
                String day = parser.getAttributeValue(null, "day");
                if (day != null) {
                    headerInfo.mDay = Integer.parseInt(day);
                }
                String month = parser.getAttributeValue(null, "month");
                if (month != null) {
                    headerInfo.mMonth = Integer.parseInt(month);
                }
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                String imageName = parser.getAttributeValue(null, "name");
                if (imageName != null) {
                    headerInfo.mName = imageName;
                }
                if (headerInfo.mImage != null && headerInfo.mDay != -1 && headerInfo.mMonth != -1) {
                    headersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("hour_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 1;
                String hour = parser.getAttributeValue(null, "hour");
                if (hour != null) {
                    headerInfo.mHour = Integer.parseInt(hour);
                }
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                String imageName = parser.getAttributeValue(null, "name");
                if (imageName != null) {
                    headerInfo.mName = imageName;
                }
                if (headerInfo.mImage != null && headerInfo.mHour != -1) {
                    headersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("random_header") ||
                    name.equalsIgnoreCase("list_header")) {
                DaylightHeaderInfo headerInfo = new DaylightHeaderInfo();
                headerInfo.mType = 2;
                String image = parser.getAttributeValue(null, "image");
                if (image != null) {
                    headerInfo.mImage = image;
                }
                String imageName = parser.getAttributeValue(null, "name");
                if (imageName != null) {
                    headerInfo.mName = imageName;
                }
                if (headerInfo.mImage != null) {
                    headersList.add(headerInfo);
                }
            } else if (name.equalsIgnoreCase("meta_data")) {
                creatorName = parser.getAttributeValue(null, "creator");
                if (DEBUG) Log.i(TAG, "creator = " + creatorName);
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
        if (DEBUG) Log.i(TAG, "loaded size = " + headersList.size());
        return creatorName;
    }
}
