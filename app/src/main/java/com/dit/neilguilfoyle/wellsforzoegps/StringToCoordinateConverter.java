package com.dit.neilguilfoyle.wellsforzoegps;

/**
 * Created by neil on 23/03/2015.
 */
public class StringToCoordinateConverter {
    private static String[] StringsToReplace = new String[] { " ", ":", ";", "*" };

    public static double CoordToDD(String coord)
    {
        if (coord.length() < 2) throw new IllegalArgumentException("The coordinate must be in format equivalent to L DD MM.MMM");
        for(String badString : StringsToReplace)
            coord = coord.replace(badString, "");

        float polarity;
        switch (coord.charAt(0))
        {
            case 'N':
            case 'E':
                polarity = 1;
                break;
            case 'S':
            case 'W':
                polarity = -1;
                break;
            default:
                throw new IllegalArgumentException("The coordinate String must start with N,E,S or W");
        }

        int dotIndex = coord.indexOf('.');

        //if no minutes make it work on degrees
        if (dotIndex == -1) dotIndex = coord.length() + 2;

        String minStr = coord.substring(dotIndex - 2, coord.length());//59.138
        String degStr = coord.substring(1, dotIndex - 2);//033
        if (minStr == null || minStr.equals("")) minStr = "0";
        if (degStr == null || degStr.equals("")) degStr = "0";
        double minutes = toDouble(minStr);
        double degrees = toDouble(degStr);

        return polarity * (degrees + (minutes / 60.0));
    }

    public static String DDToCoord(Double dd, String type)
    {
        Boolean isPositive = dd > 0;
        String polarity;
        if("lat".equals(type))
        {
            polarity = isPositive ? "N" : "S";
        }
        else if ("lng".equals(type))
        {
            polarity = isPositive ? "E" : "W";
        }
        else
        {
            return null;
        }
        dd = new Double(Math.abs(dd));
        int wholenumber = dd.intValue();
        double remainder = (dd - wholenumber) * 60;
        String remainderStr = String.format("%f", remainder);
        if(remainder < 10){
            remainderStr = "0" + remainderStr;
        }
        return polarity+wholenumber+remainderStr;
    }

    public static Double toDouble(String input){
        if (!("".equals(input) || input == null))
        {
            try{
                return Double.parseDouble(input);
            }
            catch (NumberFormatException e){
                return null;
            }
        }
        return null;
    }
}
