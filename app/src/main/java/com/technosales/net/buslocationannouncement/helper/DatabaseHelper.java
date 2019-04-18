/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.technosales.net.buslocationannouncement.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.technosales.net.buslocationannouncement.network.RouteStation;
import com.technosales.net.buslocationannouncement.network.TicketInfoDataPush;
import com.technosales.net.buslocationannouncement.pojo.HelperList;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.trackcar.Position;
import com.technosales.net.buslocationannouncement.trackcar.TrackingService;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "traccar.db";

    public static final String ROUTE_STATION_TABLE = "route_station";
    public static final String STATION_ID = "station_id";
    public static final String STATION_ORDER = "station_order";
    public static final String STATION_NAME = "name_nepali";
    private static final String STATION_NAME_ENG = "name_english";
    public static final String STATION_LAT = "latitude";
    public static final String STATION_LNG = "longitude";
    public static final String STATION_DISTANCE = "station_distance";


    public static final String PRICE_TABLE = "price_table";
    public static final String PRICE_VALUE = "price_value";
    public static final String PRICE_DISTANCE = "price_distance";
    public static final String PRICE_MIN_DISTANCE = "price_min_distance";


    public static final String TICKET_TABLE = "ticket_table";
    public static final String TICKET_TABLE_TXT = "ticket_table_txt";
    public static final String TICKET_NUMBER = "ticket_number";
    public static final String TICKET_PRICE = "ticket_price";
    public static final String TICKET_TYPE = "ticket_type";
    public static final String TICKET_DATE = "ticket_date";
    public static final String TICKET_TIME = "ticket_time";
    public static final String TICKET_LAT = "ticket_lat";
    public static final String TICKET_LNG = "ticket_lng";

    public static final String HELPER_TABLE = "helper_table";
    public static final String HELPER_ID = "helper_id";
    public static final String HELPER_NAME = "helper_name";
    private final Context context;


    public interface DatabaseHandler<T> {
        void onComplete(boolean success, T result);
    }

    private static abstract class DatabaseAsyncTask<T> extends AsyncTask<Void, Void, T> {

        private DatabaseHandler<T> handler;
        private RuntimeException error;


        public DatabaseAsyncTask(DatabaseHandler<T> handler) {
            this.handler = handler;
        }

        @Override
        protected T doInBackground(Void... params) {
            try {
                return executeMethod();
            } catch (RuntimeException error) {
                this.error = error;
                return null;
            }
        }

        protected abstract T executeMethod();

        @Override
        protected void onPostExecute(T result) {
            handler.onComplete(error == null, result);
        }
    }

    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE position (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "deviceId TEXT," +
                "time INTEGER," +
                "latitude REAL," +
                "longitude REAL," +
                "altitude REAL," +
                "speed REAL," +
                "course REAL," +
                "accuracy REAL," +
                "battery REAL," +
                "mock INTEGER)");

        db.execSQL("CREATE TABLE ticket_table (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                TICKET_NUMBER + " TEXT," +
                TICKET_PRICE + " TEXT," +
                TICKET_TYPE + " TEXT," +
                TICKET_DATE + " TEXT," +
                TICKET_TIME + " TEXT," +
                TICKET_LAT + " TEXT," +
                HELPER_ID + " TEXT," +
                TICKET_LNG + " TEXT)");
        db.execSQL("CREATE TABLE ticket_table_txt (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                TICKET_NUMBER + " TEXT," +
                TICKET_PRICE + " TEXT," +
                TICKET_TYPE + " TEXT," +
                TICKET_DATE + " TEXT," +
                TICKET_TIME + " TEXT," +
                TICKET_LAT + " TEXT," +
                HELPER_ID + " TEXT," +
                TICKET_LNG + " TEXT)");

        db.execSQL("CREATE TABLE price_table (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                PRICE_VALUE + " TEXT," +
                PRICE_MIN_DISTANCE + " INTEGER," +
                PRICE_DISTANCE + " INTEGER)");

        db.execSQL("CREATE TABLE route_station (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                STATION_ID + " TEXT," +
                STATION_ORDER + " INTEGER," +
                STATION_NAME + " TEXT," +
                STATION_NAME_ENG + " TEXT," +
                STATION_LAT + " TEXT," +
                STATION_LNG + " TEXT," +
                STATION_DISTANCE + " FLOAT)");
        db.execSQL("CREATE TABLE helper_table (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                HELPER_ID + " TEXT," +
                HELPER_NAME + " TEXT)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS position;");
        db.execSQL("DROP TABLE IF EXISTS ticket_table;");
        db.execSQL("DROP TABLE IF EXISTS price_table;");
        db.execSQL("DROP TABLE IF EXISTS route_station;");
        db.execSQL("DROP TABLE IF EXISTS ticket_table_txt;");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS position;");
        db.execSQL("DROP TABLE IF EXISTS ticket_table;");
        db.execSQL("DROP TABLE IF EXISTS price_table;");
        db.execSQL("DROP TABLE IF EXISTS route_station;");
        db.execSQL("DROP TABLE IF EXISTS ticket_table_txt;");
        onCreate(db);
    }

    public void insertPosition(Position position) {
        ContentValues values = new ContentValues();
        values.put("deviceId", position.getDeviceId());
        values.put("time", position.getTime().getTime());
        values.put("latitude", position.getLatitude());
        values.put("longitude", position.getLongitude());
        values.put("altitude", position.getAltitude());
        values.put("speed", position.getSpeed());
        values.put("course", position.getCourse());
        values.put("accuracy", position.getAccuracy());
        values.put("battery", position.getBattery());
        values.put("mock", position.getMock() ? 1 : 0);

        Log.i("dbValues", values + "");

        db.insertOrThrow("position", null, values);
    }

    public void insertStations(RouteStationList routeStationList) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        contentValues.put(STATION_ID, routeStationList.station_id);
        contentValues.put(STATION_ORDER, routeStationList.station_order);
        contentValues.put(STATION_NAME, routeStationList.station_name);
        contentValues.put(STATION_NAME_ENG, routeStationList.station_name_eng);
        contentValues.put(STATION_LAT, routeStationList.station_lat);
        contentValues.put(STATION_LNG, routeStationList.station_lng);
        contentValues.put(STATION_DISTANCE, routeStationList.station_distance);
        sqLiteDatabase.insert(ROUTE_STATION_TABLE, null, contentValues);

        Log.i("routeStation", "" + routeStationList.station_name + ":" + routeStationList.station_distance);
    }

    public void insertHelpers(ContentValues contentValues) {
        Log.i("helperValue", "" + contentValues.toString());
        getWritableDatabase().insert(HELPER_TABLE, null, contentValues);
    }

    public void insertPrice(ContentValues contentValues) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Log.i("getValue", "" + contentValues.toString());
        sqLiteDatabase.insert(PRICE_TABLE, null, contentValues);
    }

    public void insertTicketInfo(TicketInfoList ticketInfoList) {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        contentValues.put(TICKET_NUMBER, ticketInfoList.ticketNumber);
        contentValues.put(TICKET_PRICE, ticketInfoList.ticketPrice);
        contentValues.put(TICKET_TYPE, ticketInfoList.ticketType);
        contentValues.put(TICKET_DATE, ticketInfoList.ticketDate);
        contentValues.put(TICKET_TIME, ticketInfoList.ticketTime);
        contentValues.put(TICKET_LAT, ticketInfoList.ticketLat);
        contentValues.put(TICKET_LNG, ticketInfoList.ticketLng);
        contentValues.put(HELPER_ID, ticketInfoList.helper_id);
        sqLiteDatabase.insert(TICKET_TABLE, null, contentValues);


        insertTicketTxt(contentValues);

       /* boolean datasending = context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getBoolean(UtilStrings.DATA_SENDING, false);
        if (!datasending) {
            ticketInfoLists();
        }*/
    }

    public float distancesFromStart() {
        float total = 0;
        Cursor c = getReadableDatabase().rawQuery("SELECT " + STATION_DISTANCE + " FROM " + ROUTE_STATION_TABLE, null);
        while (c.moveToNext()) {
            total = c.getFloat(c.getColumnIndex(STATION_DISTANCE));

        }
        c.close();
/*
        Log.i("routeStation", "total-" +total);
*/
        return total;
    }

    public double recentStationLat(int order) {
        double lat = 0.0;
        Cursor c = getReadableDatabase().rawQuery("SELECT " + STATION_LAT + " FROM " + ROUTE_STATION_TABLE + " WHERE " + STATION_ORDER + " =" + order, null);
        while (c.moveToNext()) {
            lat = Double.parseDouble(c.getString(c.getColumnIndex(STATION_LAT)));
        }
        c.close();
        return lat;
    }

    public double recentStationLng(int order) {
        double lat = 0.0;
        Cursor c = getReadableDatabase().rawQuery("SELECT " + STATION_LNG + " FROM " + ROUTE_STATION_TABLE + " WHERE " + STATION_ORDER + " =" + order, null);
        while (c.moveToNext()) {
            lat = Double.parseDouble(c.getString(c.getColumnIndex(STATION_LNG)));
        }
        c.close();
        return lat;
    }

    public void insertTicketTxt(ContentValues contentValues) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.insert(TICKET_TABLE_TXT, null, contentValues);
        /*writeToFile();*/ // need at the end of the day
    }

    public List<TicketInfoList> listTickets() {
        List<TicketInfoList> ticketInfoLists = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TICKET_TABLE, null);
        while (cursor.moveToNext()) {
            TicketInfoList ticketInfoList = new TicketInfoList();
            ticketInfoList.ticketNumber = cursor.getString(cursor.getColumnIndex(TICKET_NUMBER));
            ticketInfoList.ticketPrice = cursor.getString(cursor.getColumnIndex(TICKET_PRICE));
            ticketInfoList.ticketType = cursor.getString(cursor.getColumnIndex(TICKET_TYPE));
            ticketInfoList.ticketDate = cursor.getString(cursor.getColumnIndex(TICKET_DATE));
            ticketInfoList.ticketTime = cursor.getString(cursor.getColumnIndex(TICKET_TIME));
            ticketInfoList.ticketLat = cursor.getString(cursor.getColumnIndex(TICKET_LAT));
            ticketInfoList.ticketLng = cursor.getString(cursor.getColumnIndex(TICKET_LNG));
            ticketInfoList.helper_id = cursor.getString(cursor.getColumnIndex(HELPER_ID));
            ticketInfoLists.add(ticketInfoList);
        }
        return ticketInfoLists;

    }

    public int remainingAmount() {
        int amount = 0;
        int ticketAmount;
        List<TicketInfoList> ticketInfoLists = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TICKET_TABLE, null);
        while (cursor.moveToNext()) {
            TicketInfoList ticketInfoList = new TicketInfoList();
            ticketInfoList.ticketNumber = cursor.getString(cursor.getColumnIndex(TICKET_NUMBER));
            ticketInfoList.ticketPrice = cursor.getString(cursor.getColumnIndex(TICKET_PRICE));
            ticketInfoList.ticketType = cursor.getString(cursor.getColumnIndex(TICKET_TYPE));
            ticketInfoList.ticketDate = cursor.getString(cursor.getColumnIndex(TICKET_DATE));
            ticketInfoList.ticketTime = cursor.getString(cursor.getColumnIndex(TICKET_TIME));
            ticketInfoList.ticketLat = cursor.getString(cursor.getColumnIndex(TICKET_LAT));
            ticketInfoList.ticketLng = cursor.getString(cursor.getColumnIndex(TICKET_LNG));
            ticketInfoList.helper_id = cursor.getString(cursor.getColumnIndex(HELPER_ID));
            ticketInfoLists.add(ticketInfoList);
            ticketAmount = Integer.parseInt(ticketInfoList.ticketPrice);
            amount = amount + ticketAmount;

        }

        return amount;
    }

    public void ticketInfoLists() {
        int id = 0;
        List<TicketInfoList> ticketInfoLists = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TICKET_TABLE + " LIMIT 1", null);
        while (cursor.moveToNext()) {
            TicketInfoList ticketInfoList = new TicketInfoList();
            ticketInfoList.ticketNumber = cursor.getString(cursor.getColumnIndex(TICKET_NUMBER));
            ticketInfoList.ticketPrice = cursor.getString(cursor.getColumnIndex(TICKET_PRICE));
            ticketInfoList.ticketType = cursor.getString(cursor.getColumnIndex(TICKET_TYPE));
            ticketInfoList.ticketDate = cursor.getString(cursor.getColumnIndex(TICKET_DATE));
            ticketInfoList.ticketTime = cursor.getString(cursor.getColumnIndex(TICKET_TIME));
            ticketInfoList.ticketLat = cursor.getString(cursor.getColumnIndex(TICKET_LAT));
            ticketInfoList.ticketLng = cursor.getString(cursor.getColumnIndex(TICKET_LNG));
            ticketInfoList.helper_id = cursor.getString(cursor.getColumnIndex(HELPER_ID));
            ticketInfoLists.add(ticketInfoList);
            id = cursor.getInt(cursor.getColumnIndex("id"));

        }
        cursor.close();
        context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putInt(UtilStrings.LAST_DATA_ID, id).apply();
        TicketInfoDataPush.pushTicketData(context, ticketInfoLists);
        /*TicketInfoDataPush.pushBusData(context, getJsonData(ticketInfoLists));*/
    }


    public JSONObject getJsonData(List<TicketInfoList> ticketInfoLists) {
        JSONObject object = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < ticketInfoLists.size(); i++) {
                TicketInfoList ticketInfoList = ticketInfoLists.get(i);
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("device_id", context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, ""));
                jsonObject.put("helper_id", ticketInfoList.helper_id);
                jsonObject.put("ticket_number", ticketInfoList.ticketNumber);
                jsonObject.put("price", ticketInfoList.ticketPrice);
                jsonObject.put("device_time", ticketInfoList.ticketDate + " " + ticketInfoList.ticketTime);
                jsonObject.put("ticket_type", ticketInfoList.ticketType);
                jsonObject.put("latitude", ticketInfoList.ticketLat);
                jsonObject.put("longitude", ticketInfoList.ticketLng);
                jsonObject.put("trip", "1");
                array.put(jsonObject);

            }
            object.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    public List<HelperList> helperLists() {
        List<HelperList> helperLists = new ArrayList<>();
        String sql = "SELECT * FROM " + HELPER_TABLE;
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
            HelperList helperList = new HelperList();
            helperList.helper_id = c.getString(c.getColumnIndex(HELPER_ID));
            helperList.helper_name = c.getString(c.getColumnIndex(HELPER_NAME));
            helperLists.add(helperList);
        }
        c.close();
        return helperLists;
    }

    public void clearHelpers() {
        getWritableDatabase().execSQL("DELETE FROM " + HELPER_TABLE);
    }

    public void deleteFromLocal() {
        int id = context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.LAST_DATA_ID, 1);
        String sql = "DELETE FROM " + TICKET_TABLE + " WHERE id<=" + id;
        Log.i("deleteFromLocal", "" + sql);
        getWritableDatabase().execSQL(sql);
        context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.DATA_SENDING, false).apply();

    }

    public void deleteFromLocalId(String ticketNo) {
        String sql = "DELETE FROM " + TICKET_TABLE + " WHERE " + TICKET_NUMBER + " ='" + ticketNo + "'";
        Log.i("deleteFromLocal", "" + sql);
        getWritableDatabase().execSQL(sql);
        /*context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.DATA_SENDING, false).apply();*/

    }

    public void clearAllFromData() {
        String sql = "DELETE FROM " + TICKET_TABLE;
        getWritableDatabase().execSQL(sql);
    }

    public List<PriceList> priceLists(int id) {
        List<PriceList> priceLists = new ArrayList<>();

        String sql = "SELECT * FROM " + PRICE_TABLE + " WHERE id >" + id;
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
            PriceList priceList = new PriceList();
            priceList.price_value = c.getString(c.getColumnIndex(PRICE_VALUE));
            priceList.price_min_distance = c.getInt(c.getColumnIndex(PRICE_MIN_DISTANCE));
            priceList.price_distance = c.getInt(c.getColumnIndex(PRICE_DISTANCE));
            priceLists.add(priceList);
        }
        c.close();
        return priceLists;

    }

    public String priveWrtDistance(float distance) {
        String price = "";
        Cursor c = getWritableDatabase().rawQuery("SELECT " + PRICE_VALUE + " FROM " + PRICE_TABLE + " WHERE " + PRICE_MIN_DISTANCE + " < " + distance + " AND " + PRICE_DISTANCE + " > " + distance, null);
        while (c.moveToNext()) {
            price = c.getString(c.getColumnIndex(PRICE_VALUE));
        }
        c.close();
        return price;
    }

    public List<RouteStationList> routeStationLists() {
        List<RouteStationList> routeStationLists = new ArrayList<>();

        String sql = "SELECT * FROM " + ROUTE_STATION_TABLE;
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
            RouteStationList routeStationList = new RouteStationList();
            routeStationList.station_id = c.getString(c.getColumnIndex(STATION_ID));
            routeStationList.station_order = c.getInt(c.getColumnIndex(STATION_ORDER));
            routeStationList.station_name = c.getString(c.getColumnIndex(STATION_NAME));
            routeStationList.station_name_eng = c.getString(c.getColumnIndex(STATION_NAME_ENG));
            routeStationList.station_lat = c.getString(c.getColumnIndex(STATION_LAT));
            routeStationList.station_lng = c.getString(c.getColumnIndex(STATION_LNG));
            routeStationList.station_distance = c.getFloat(c.getColumnIndex(STATION_DISTANCE));

            routeStationLists.add(routeStationList);
            Log.i("getParams", routeStationList.station_name);
        }
        c.close();


        return routeStationLists;

    }

    public String nextStation(int stationOrder) {
        String station = "";
        String sql = "SELECT * FROM " + ROUTE_STATION_TABLE + " WHERE " + STATION_ORDER + " =" + stationOrder;
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {

            station = c.getString(c.getColumnIndex(STATION_NAME));

        }
        c.close();
        return station;

    }

    public int nextStationId(String stationId) {
        int station = 0;
        String sql = "SELECT " + STATION_ORDER + " FROM " + ROUTE_STATION_TABLE + " WHERE " + STATION_ID + " ='" + stationId + "'";
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {

            station = c.getInt(c.getColumnIndex(STATION_ORDER));

        }
        c.close();
        return station;

    }

    public int lastStation(String stationId) {
        int station = 0;
        String sql = "SELECT " + STATION_ORDER + " FROM " + ROUTE_STATION_TABLE + " WHERE " + STATION_ID + " ='" + stationId + "'";
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {

            station = c.getInt(c.getColumnIndex(STATION_ORDER));

        }
        c.close();
        return station;

    }

    public int getDouble(String stationId) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCount = db.rawQuery("SELECT COUNT(*) FROM " + ROUTE_STATION_TABLE + " WHERE " + STATION_ID + " ='" + stationId + "'", null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        Log.i("getDouble", "" + count);
        return count;
    }

    public void clearStations() {
        String sql = "DELETE FROM " + ROUTE_STATION_TABLE;
        getWritableDatabase().execSQL(sql);

    }

    public int RouteStationRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, ROUTE_STATION_TABLE);
        return numRows;
    }

    public void insertPositionAsync(final Position position, DatabaseHandler<Void> handler) {
        new DatabaseAsyncTask<Void>(handler) {
            @Override
            protected Void executeMethod() {
                insertPosition(position);
                return null;
            }
        }.execute();
    }

    public Position selectPosition() {
        Position position = new Position();

        Cursor cursor = db.rawQuery("SELECT * FROM position ORDER BY id LIMIT 1", null);
        try {
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();

                position.setId(cursor.getLong(cursor.getColumnIndex("id")));
                position.setDeviceId(cursor.getString(cursor.getColumnIndex("deviceId")));
                position.setTime(new Date(cursor.getLong(cursor.getColumnIndex("time"))));
                position.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                position.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                position.setAltitude(cursor.getDouble(cursor.getColumnIndex("altitude")));
                position.setSpeed(cursor.getDouble(cursor.getColumnIndex("speed")));
                position.setCourse(cursor.getDouble(cursor.getColumnIndex("course")));
                position.setAccuracy(cursor.getDouble(cursor.getColumnIndex("accuracy")));
                position.setBattery(cursor.getDouble(cursor.getColumnIndex("battery")));
                position.setMock(cursor.getInt(cursor.getColumnIndex("mock")) > 0);

            } else {
                return null;
            }
        } finally {
            cursor.close();
        }

        return position;
    }

    public void selectPositionAsync(DatabaseHandler<Position> handler) {
        new DatabaseAsyncTask<Position>(handler) {
            @Override
            protected Position executeMethod() {
                return selectPosition();
            }
        }.execute();
    }

    public void deletePosition(long id) {
        if (db.delete("position", "id = ?", new String[]{String.valueOf(id)}) != 1) {
            throw new SQLException();
        }
    }

    public void deletePositionAsync(final long id, DatabaseHandler<Void> handler) {
        new DatabaseAsyncTask<Void>(handler) {
            @Override
            protected Void executeMethod() {
                deletePosition(id);
                return null;
            }
        }.execute();
    }


    public String getWriteData() {
        String sql = "SELECT * FROM " + TICKET_TABLE_TXT;
        String data = "";
        Cursor c = getWritableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
            data = data + c.getString(c.getColumnIndex(TICKET_NUMBER)) + "\t" +
                    c.getString(c.getColumnIndex(TICKET_PRICE)) + "\t" +
                    c.getString(c.getColumnIndex(TICKET_TYPE)) + "\t" +
                    c.getString(c.getColumnIndex(TICKET_DATE)) + "\t" +
                    c.getString(c.getColumnIndex(TICKET_TIME)) + "\t" +
                    c.getString(c.getColumnIndex(TICKET_LAT)) + "\t" +
                    c.getString(c.getColumnIndex(TICKET_LNG)) + "\t" +
                    c.getString(c.getColumnIndex(HELPER_ID)) + "\t" + "\n";
        }
        c.close();
        return data;

    }

    public void writeToFile() {
        String data = getWriteData();
        Log.i("Data", "Data:" + data);

        File txtFile = new File(Environment.getExternalStorageDirectory() + "/TicketData/" + GeneralUtils.getFullDate() + ".txt");

        if (!txtFile.exists()) {
            try {
                txtFile.createNewFile();
                GeneralUtils.writeInTxt(txtFile, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            GeneralUtils.writeInTxt(txtFile, data);
        }

    }

    public void clearTxtTable() {
        String sql = "DELETE FROM " + TICKET_TABLE_TXT;
        getWritableDatabase().execSQL(sql);
    }

}
