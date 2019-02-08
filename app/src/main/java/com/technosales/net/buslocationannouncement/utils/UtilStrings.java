package com.technosales.net.buslocationannouncement.utils;

public class UtilStrings {
    public static final String SHARED_PREFERENCES = "shared_prefs";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String DEVICE_ID = "device_id";
    public static final String ROUTE_ID = "route_id";
    public static final String TOTAL_TICKETS = "total_tickets";
    public static final String TOTAL_COLLECTIONS = "total_collections";
    public static final String DATE_TIME = "date_time";
    public static final String DATA_SENDING = "data_sent";
    public static final String LAST_DATA_ID = "last_data_id";
    public static final String ROUTE_NAME = "route_name";


    //    public static final String MAIN_URL = "http://172.16.1.131:85/route_api/public/api/";
    public static final String MAIN_URL = "http://202.52.240.149:85/route_api/public/api/";
    public static final String REGISTER_URL = MAIN_URL + "routeDevice";
    public static final String ROUTE_STATION = MAIN_URL + "getRouteStation";


    //    public static final String TICKET_URL = "http://172.16.1.131:85/routemanagement/api/";
    public static final String TICKET_URL = "http://202.52.240.149:85/routemanagement/api/";
    public static final String TICKET_PRICE_LIST = "rate_list";
    public static final String TICKET_POST = TICKET_URL + "store_ticket";

}
