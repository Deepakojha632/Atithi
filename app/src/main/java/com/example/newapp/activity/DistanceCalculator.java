package com.example.newapp.activity;

class DistanceCalculator {
    public static void main(String[] args) throws java.lang.Exception {
        System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
        System.out.println(distance(12.023670, 79.860680, 12.513090, 78.214680, "K") + " Kilometers\n");
        System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
    }

    private static double distance(double currentLat, double currentlon, double destinationLat, double destinationLon, String unit) {
        if ((currentLat == destinationLat) && (currentlon == destinationLon)) {
            return 0;
        } else {
            double theta = currentlon - destinationLon;
            double dist = Math.sin(Math.toRadians(currentLat)) * Math.sin(Math.toRadians(destinationLat)) + Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(destinationLat)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }
}