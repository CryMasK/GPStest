package com.fatguy.fju.gpstest;

import android.content.Context;
import android.content.res.TypedArray;

import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.Switch;

import java.lang.reflect.Field;

/**
 * Created by User on 2016/10/9.
 * https://gist.github.com/T-Spoon/fe9f0ee8cbb79049b1ec
 */

public class SwitchCompatFix extends Switch {

    public SwitchCompatFix(Context context) {
        super(context);
    }

    public SwitchCompatFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchCompatFix(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSwitchTextAppearance(Context context, int resid) {
        TypedArray appearance = context.obtainStyledAttributes(resid, new int[]{android.R.attr.textSize});
        int ts = appearance.getDimensionPixelSize(0, 0);

        try {
            Field field = Switch.class.getDeclaredField("mTextPaint");
            field.setAccessible(true);

            TextPaint textPaint = (TextPaint) field.get(this);
            textPaint.setTextSize(ts);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        appearance.recycle();

        super.setSwitchTextAppearance(context, resid);
    }
}
