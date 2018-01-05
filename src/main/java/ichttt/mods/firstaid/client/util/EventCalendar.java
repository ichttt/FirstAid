package ichttt.mods.firstaid.client.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Calendar;

import static java.util.Calendar.*;

/**
 * Stolen from iChunUtil (https://github.com/iChun/iChunUtil)
 * Modified to fit code style and removed PG birthday (I don't need that :D)
 */
@SideOnly(Side.CLIENT)
public class EventCalendar {

    private static boolean isNewYear; //1/1
    private static boolean isValentinesDay; //14/2
    private static boolean isAFDay; //1/4
    private static boolean isHalloween; //31/10
    private static boolean isChristmas; //25/12

    public static int day;

    public static void checkDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        day = calendar.get(Calendar.DAY_OF_MONTH);

        switch (calendar.get(Calendar.MONTH)) //month
        {
            case JANUARY:
                if (day == 1) isNewYear = true;
                break;
            case FEBRUARY:
                if (day == 14) isValentinesDay = true;
                break;
            case APRIL:
                if (day == 1) isAFDay = true;
                break;
            case OCTOBER:
                if (day == 31) isHalloween = true;
                break;
            case DECEMBER:
                if (day == 25) isChristmas = true;
                break;
        }
    }

    public static boolean isGuiFun() {
        return isNewYear || isHalloween || isAFDay;
    }

    public static boolean isNewYear() {
        return isNewYear;
    }

    public static boolean isValentinesDay() {
        return isValentinesDay;
    }

    public static boolean isAFDay() {
        return isAFDay;
    }

    public static boolean isHalloween() {
        return isHalloween;
    }

    public static boolean isChristmas() {
        return isChristmas;
    }
}
