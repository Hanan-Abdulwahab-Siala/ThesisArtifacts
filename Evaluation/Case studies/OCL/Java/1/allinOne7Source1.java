// Source: https://en.wikipedia.org/wiki/Java_syntax
// Source: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/index.html

/* This is a multi-line comment.
It may occupy more than one line. */

// This is an end-of-line comment

/**
 * This is a documentation comment.
 * 
 * @author John Doe
 */
 
package myapplication.mylibrary;

import java.util.Random; // Single type declaration
import java.util.*;
import java.*;
import static java.lang.System.out; //'out' is a static field in java.lang.System
import static screen.ColorName.*;

public enum ColorName {
    RED, BLUE, GREEN
};

// See http://docs.oracle.com/javase/7/docs/technotes/guides/language/underscores-literals.html
public class LexerTest {
    static void main(String[] args) {
        long creditCardNumber = 1234_5678_9012_3456L;
        long socialSecurityNumber = 999_99_9999L;
        float pi = 3.14_15F;
        long hexBytes = 0xFF_EC_DE_5E;
        long hexWords = 0xCAFE_BABE;
        long maxLong = 0x7fff_ffff_ffff_ffffL;
        byte nybbles = 0b0010_0101;
        long bytes = 0b11010010_01101001_10010100_10010010;
        long lastReceivedMessageId = 0L;
        double hexDouble1 = 0x1.0p0;
        double hexDouble2 = 0x1.956ad0aae33a4p117;
        int octal = 01234567;
        long hexUpper = 0x1234567890ABCDEFL;
        long hexLower = 0x1234567890abcedfl;

        int x1 = _52;              // This is an identifier, not a numeric literal
        int x2 = 5_2;              // OK (decimal literal)
        int x4 = 5_______2;        // OK (decimal literal)

        int x7 = 0x5_2;            // OK (hexadecimal literal)

        int x9 = 0_52;             // OK (octal literal)
        int x10 = 05_2;            // OK (octal literal)
   } 
} 