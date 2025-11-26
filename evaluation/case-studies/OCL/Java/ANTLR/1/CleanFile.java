package myapplication.mylibrary;
import java.util.Random; 
import java.util.*;
import java.*;
import static java.lang.System.out; 
import static screen.ColorName.*;
public enum ColorName {
    RED, BLUE, GREEN
};
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
        int x1 = _52;              
        int x2 = 5_2;              
        int x4 = 5_______2;        
        int x7 = 0x5_2;            
        int x9 = 0_52;             
        int x10 = 05_2;            
   } 
} 