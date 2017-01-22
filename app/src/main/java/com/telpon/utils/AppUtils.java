package com.telpon.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by adikwidiasmono on 1/21/17.
 */

public class AppUtils {

    public static Bitmap generateBitmapFromLetter(String label, int key, Context context, int tileSize) {
        LetterTileProvider tileProvider = new LetterTileProvider(context);
        Bitmap letterTile = tileProvider.getLetterTile(label, String.valueOf(key), tileSize, tileSize);
//        letterTile = Bitmap.createScaledBitmap(letterTile, 128, 128, true);

        return letterTile;
    }

}
