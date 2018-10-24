package com.shoppalteam.shoppal.misc;

import android.support.annotation.NonNull;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CurrencyFilter extends DigitsKeyListener
{
    private final Pattern mPattern;

    public CurrencyFilter(int maxDigitsBeforeDecimalPoint, int maxDigitsAfterDecimalPoint) {
        mPattern=Pattern.compile("[0-9]{0," + maxDigitsBeforeDecimalPoint + "}+((\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, @NonNull Spanned dest, int dStart, int dEnd) {
        String input = source.subSequence(start,end).toString();
        StringBuilder sBuilder= new StringBuilder(dest);
        sBuilder.replace(dStart, dEnd, input);
        Matcher matcher=mPattern.matcher(sBuilder.toString());

        if(!matcher.matches()) {
            if (source.length() !=1)
                return dest.subSequence(dStart, dEnd);
            return "";
        }
        return null;
    }
}
