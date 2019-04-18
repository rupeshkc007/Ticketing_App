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
    public static final String DEVICE_NAME = "device_name";
    public static final String NAME_HELPER = "helper_name";
    public static final String ID_HELPER = "helper_id";
    public static final String RESET = "reset";
    public static final String SENT_TICKET = "sent_ticket";
    public static final String FORWARD = "rev_for"; /// save if bus is going along with order or returning
    public static final String ROUTE_TYPE = "route_type";
    public static final String MODE = "mode";


    //    public static final String MAIN_URL = "http://172.16.1.131:85/route_api/public/api/";//server
//    public static final String MAIN_URL = "http://202.52.240.149:85/route_api/public/api/";//test---8170613861
    public static final String MAIN_URL = "http://202.52.240.149/route_api_v2/public/api/";//production
    public static final String REGISTER_URL = MAIN_URL + "routeDevice";
    public static final String ROUTE_STATION = MAIN_URL + "getRouteStation";


    //    public static final String TICKET_URL = "http://172.16.1.131:85/routemanagement/api/";/// server
    public static final String TICKET_URL = "http://117.121.237.226:83/routemanagement/api/";/// production
    //    public static final String TICKET_URL = "http://202.52.240.149:85/routemanagement/api/";////testServer
    public static final String TICKET_PRICE_LIST = "rate_list";
    public static final String UPDATE_TICKET = TICKET_URL + "update_device_info";
    public static final String RESET_DEVICE = TICKET_URL + "reset_device";
    public static final String TICKET_POST = TICKET_URL + "store_ticket";
    public static final String TICKET_REGISTER_DEVICE = TICKET_URL + "register";


    public static final int RING_ROAD = 0;/// for ring road
    public static final int NON_RING_ROAD = 1;/// for non ring road

    public static final int MODE_1 = 1;//// normal mode---> starting
    public static final int MODE_2 = 2;//// location suggestion wrt prices
    public static final int MODE_3 = 3;//// price calculations with route

}
