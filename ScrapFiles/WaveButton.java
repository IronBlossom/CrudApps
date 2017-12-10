package developer.shivam.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Ishtiaq Mahmood Amin on 12/11/2017.
 */

public class WaveButton extends android.support.v7.widget.AppCompatButton {
    /**
     * @frequency - Then less the frequency more will be the number of
     *  waves
     */
    int frequency = 360;

    /**
     * @amplitude - Amplitude gives the height of wave
     */

    int amplitude = 80;
    private float shift = 0;
    /**
     * x, y1, y2 are used to plot the path for wave
     */
    float x;
    float y;

    private Paint firstWaveColor;
    Path firstWavePath = new Path();
    private Context mContext;
    private int quadrant;
    private int width=0;

    public WaveButton(Context context) {
        super(context);
        init(context,null,-1);
    }


    public WaveButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,-1);
    }

    public WaveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        firstWaveColor = new Paint();
        firstWaveColor.setAntiAlias(true);
        firstWaveColor.setStrokeWidth(2);
        firstWaveColor.setColor(Color.parseColor("#D0D0D0"));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.parseColor("#F8F8F8"));
        amplitude=getHeight()/2;
        quadrant = getHeight()/3;
        width = canvas.getWidth();

        firstWavePath.moveTo(0, getHeight());
        firstWavePath.lineTo(0, quadrant);


        for (int i = 0; i < width + 10; i = i + 10) {
            x = (float) i;

            y = quadrant + amplitude * (float) Math.sin(((i + 10) * Math.PI / frequency) + shift);

            firstWavePath.lineTo(x, y);
        }
        firstWavePath.lineTo(getWidth(), getHeight());
        canvas.drawPath(firstWavePath, firstWaveColor);

    }
}
