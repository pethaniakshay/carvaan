package com.codepuran.carvaan.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {
        /*Pattern filmPattern = Pattern.compile("^Film:");
        Pattern artistesPattern = Pattern.compile("^Artistes:");

        var s = "Film: Shreeman Funtoosh\n".split("^Film:");*/

        //Matcher filmMatcher = filmPattern.matcher("Film: Shreeman Funtoosh\n");
        /*Pattern digitPattern = Pattern.compile("^\\d+\\.");
        var digitMatcher = digitPattern.matcher("0323 Dhalti Jaaye Raat\n");
        var name = "02. Yeh Dard Bhara Afsana\n".split(". ");

        if(digitMatcher.find()){
            System.out.println("Match");
        } else {
            System.out.println("Not Matched");
        }*/

        String ar = "AA";
        var ra = ar.split(",");
        System.out.println("Done!");

    }
}
