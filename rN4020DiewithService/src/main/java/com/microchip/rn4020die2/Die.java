/*
 * MICROCHIP SOFTWARE NOTICE AND DISCLAIMER:  You may use this software, and any derivatives created by any person or
 * entity by or on your behalf, exclusively with Microchip?s products.  Microchip and its licensors retain all ownership
 * and intellectual property rights in the accompanying software and in all derivatives hereto.
 *
 * This software and any accompanying information is for suggestion only.  It does not modify Microchip?s standard
 * warranty for its products.  You agree that you are solely responsible for testing the software and determining its
 * suitability.  Microchip has no obligation to modify, test, certify, or support the software.
 *
 * THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED OR STATUTORY, INCLUDING,
 * BUT NOT LIMITED TO, IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE
 * APPLY TO THIS SOFTWARE, ITS INTERACTION WITH MICROCHIP?S PRODUCTS, COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN
 * ANY APPLICATION.
 *
 * IN NO EVENT, WILL MICROCHIP BE LIABLE, WHETHER IN CONTRACT, WARRANTY, TORT (INCLUDING NEGLIGENCE OR BREACH OF
 * STATUTORY DUTY), STRICT LIABILITY, INDEMNITY, CONTRIBUTION, OR OTHERWISE, FOR ANY INDIRECT, SPECIAL, PUNITIVE,
 * EXEMPLARY, INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE, FOR COST OR EXPENSE OF ANY KIND WHATSOEVER RELATED TO THE
 * SOFTWARE, HOWSOEVER CAUSED, EVEN IF MICROCHIP HAS BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE.
 * TO THE FULLEST EXTENT ALLOWABLE BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL CLAIMS IN ANY WAY RELATED TO THIS
 * SOFTWARE WILL NOT EXCEED THE AMOUNT OF FEES, IF ANY, THAT YOU HAVE PAID DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.
 *
 * MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE TERMS.
 *
 *
 * File:        Die.java
 * Date:        July 24, 2013
 * Compiler:    JDK and Android SDK
 *
 * Class to represent a die that can roll a number from 1 to 6
 *
 */

package com.microchip.rn4020die2;

public class Die {

    private static byte dieCount = 0;            //Variable to track the number of dice created
    private byte spots;                            //Variable to represent the spots showing on the die

    //--------------------------------------------------------------------------
//Constructor creates a Die and initializes the value of spots
    public Die() {
        spots = (byte) (1 + (Math.random() * 6));
        dieCount++;
    }

    //--------------------------------------------------------------------------
//Method rolls the die and returns the new value of spots
    public byte Roll() {
        byte oldSpots = spots;
        while (spots == oldSpots)                //Ensure each roll is different for demo - remove for a real die!
            spots = (byte) (1 + (Math.random() * 6));
        return spots;
    }

    //--------------------------------------------------------------------------
//Method returns the value of spots without rolling the die
    public byte View() {
        return spots;
    }

    //--------------------------------------------------------------------------
//Method returns the total number of die objects created
    public byte NumDie() {
        return dieCount;
    }
}
