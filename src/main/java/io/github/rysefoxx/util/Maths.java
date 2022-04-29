package io.github.rysefoxx.util;


import java.util.Random;

/**
 * @author Rysefoxx | Rysefoxx#6772
 * @since 4/29/2022
 */
public class Maths {


    private static final Random random = new Random();

    public static int randomInteger(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
