package org.sana.text.json;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * ISO 8601 Date serializer.
 *
 * Original Source: <a href="https://gist.github.com/bbirec/5748489">https://gist.github.com/bbirec/5748489</a>
 */
public class ISODateAdapter implements JsonSerializer<Date>,
        JsonDeserializer<Date>{
    /** The iso8601 date format used by the dispatch server */
    public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final DateFormat iso8601Format;

    ISODateAdapter() {
        this.iso8601Format = new SimpleDateFormat(ISO_FORMAT, Locale.US);
        this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        String dateFormatAsString = iso8601Format.format(src);
        return new JsonPrimitive(dateFormatAsString);
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context)
            throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        }
        Date date = deserializeToDate(json);
        if (typeOfT == Date.class) {
            return date;
        } else if (typeOfT == Timestamp.class) {
            return new Timestamp(date.getTime());
        } else if (typeOfT == java.sql.Date.class) {
            return new java.sql.Date(date.getTime());
        } else {
            throw new IllegalArgumentException(getClass() +
                    " cannot deserialize to " + typeOfT);
        }
    }

    /**
     * Deserializer for date objects.
     * @param json The JSON string to deserialize.
     * @return a new Date instance.
     */
    private Date deserializeToDate(JsonElement json) {
        try {
            return iso8601Format.parse(json.getAsString());
        } catch (ParseException e) {
            throw new JsonSyntaxException(json.getAsString(), e);
        }
    }

    private static final ISODateAdapter adapter = new ISODateAdapter();

    /**
     * Returns the singleton instance of the adapter.
     *
     * @return
     */
    public static final ISODateAdapter get(){
        return adapter;
    }

    public static void main(String...args){
        class DateOBJ{
            public Date dob;
        }
        String json = "{ 'dob':'2014-04-08T19:25:28'}";
        Type type = new TypeToken<DateOBJ>(){}.getType();
        Type dateType = new TypeToken<Date>(){}.getType();
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(dateType, new ISODateAdapter())
                .create();
        System.out.println(json);
        DateOBJ dobj = gson.fromJson(json, type);
        System.out.println(dobj.dob.toString());
        json = "{ \"dob\":\"2014-04-08T19:25:28.00\"}";
        System.out.println(json);
        dobj = gson.fromJson(json, type);
        System.out.println(dobj.dob.toString());
        json = "{ 'dob':'2014-04-08T19:25:28'}";
        System.out.println(json);
        dobj = gson.fromJson(json, type);
        System.out.println(dobj.dob.toString());
        json = "{ 'dob':'2014-04-08T19:25:28'}";
    }
}
