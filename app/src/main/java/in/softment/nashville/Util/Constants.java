package in.softment.nashville.Util;

import java.util.Calendar;
import java.util.Date;

public class Constants {

    public static Date currentDate = new Date();

    public static Date getExpireDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -1);
        return  calendar.getTime();
    }
    public static Date expireDate = getExpireDate();
}
