package com.example.imamin.keypadproject;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @author imamin
 * <p>
 *         This class is a outcome of the issue 9904 that was raised in
 *         code.google
 *         mentioning that android.graphics.Typeface doesn't garbage collected
 *         by
 *         System.GC. Everytime Typeface.createFromAsset() is called there's a
 *         new
 *         object created on the
 *         stack, which is never been garbage collected on pre ICS devices.
 * 
 * </p>
 * 
 * 
 * @see <a
 *      href="https://code.google.com/p/android/issues/detail?id=9904"><b>Typeface.createFromAsset
 *      leaks asset stream</b> </a>
 * */
public class OptimizedTypeface {
	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static Typeface get(Context c, String fontName) {
		synchronized (cache) {
			if (!cache.containsKey(fontName)) {
				Typeface t = Typeface.createFromAsset(c.getAssets(), fontName);
				cache.put(fontName, t);
			}
			return cache.get(fontName);
		}
	}
}
