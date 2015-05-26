package com.ironblossom.ndksample;

/*
*
* This is the 'Java Native Library' node
*  on Marakana's video
*
*
* */
public class FibLib {

    public static long fibJR(long n) {

        /*
        * Below is the equivalence of
        *   if(n<=0){
        *  return 0;
        *  }else if(n==1){
        *  return 1;
        *  }else{
        *  return fibJR(n-1)+fibJR(n-2);
        *  }
        *
        * */
        return n <= 0 ? 0 : n == 1 ? 1 : fibJR(n - 1) + fibJR(n - 2);
    }

    public static native long fibNR(long n);

    public static long fibJI(long n) {
        long previous = -1;
        long result = 1;
        for (int i = 0; i < n; i++) {
            long sum = result + previous;
            previous = result;
            result = sum;
        }

        return result;
    }

    public static native long fibNI(long n);
}
