package ironblossom.csemock.experimental.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;

import ironblossom.csemock.R;

public class TitleStyler {
    private SpannableStringBuilder mSpanStrBuild;
    private SpannableString mSpanStr;
    private ImageSpan imageSpan;
    Context context;

  public  TitleStyler(Context context) {
        this.context = context;
        imageSpan = new ImageSpan(context, R.drawable.title_divider_img);
        mSpanStrBuild = new SpannableStringBuilder(" MarketTrak*");
        mSpanStrBuild.setSpan(new StyleSpan(Typeface.BOLD), 6, mSpanStrBuild.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpanStrBuild.setSpan(imageSpan, mSpanStrBuild.length()-1, mSpanStrBuild.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public SpannableString getStyled(String targetString) {
        mSpanStr = new SpannableString(mSpanStrBuild.append(targetString));
        return mSpanStr;
    }
}
