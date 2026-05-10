package com.spk.bodygoals;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BmiGaugeView extends View {

    private float bmi = 0f;
    private final float minBmi = 10f;
    private final float maxBmi = 45f;

    private Paint arcPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint indicatorPaint;
    private String kategori = "Kategori";

    public BmiGaugeView(Context context) {
        super(context);
        init();
    }

    public BmiGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BmiGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.rgb(22, 34, 55));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.rgb(120, 130, 145));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        indicatorPaint.setColor(Color.WHITE);
    }

    public void setBmi(float bmi) {
        if (bmi < minBmi) bmi = minBmi;
        if (bmi > maxBmi) bmi = maxBmi;

        this.bmi = bmi;
        this.kategori = getKategoriBmi(bmi);

        invalidate();
    }

    private String getKategoriBmi(float bmi) {
        if (bmi < 18.5f) {
            return "Kurus";
        } else if (bmi >= 18.5f && bmi <= 24.99f) {
            return "Ideal";
        } else if (bmi >= 25.0f && bmi <= 29.99f) {
            return "BB Lebih";
        } else if (bmi >= 30.0f && bmi <= 34.99f) {
            return "Obesitas I";
        } else if (bmi >= 35.0f && bmi <= 39.99f) {
            return "Obesitas II";
        } else {
            return "Obesitas III";
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float size = Math.min(width, height);
        float strokeWidth = size * 0.11f;

        arcPaint.setStrokeWidth(strokeWidth);
        indicatorPaint.setStrokeWidth(strokeWidth + 2f);

        float padding = strokeWidth + 8f;

        RectF rect = new RectF(
                padding,
                padding,
                width - padding,
                width - padding
        );

        /*
         * Gauge mulai dari kiri bawah ke kanan bawah.
         * Total sudut 240 derajat.
         */
        float startAngle = 150f;
        float totalAngle = 240f;

        float currentAngle = startAngle;

        // 1. Kurus / Underweight
        drawSegment(canvas, rect, currentAngle, 58f, Color.rgb(255, 203, 27));
        currentAngle += 58f;

        // 2. Normal / Ideal
        drawSegment(canvas, rect, currentAngle, 45f, Color.rgb(85, 204, 151));
        currentAngle += 45f;

        // 3. Berat Badan Lebih
        drawSegment(canvas, rect, currentAngle, 34f, Color.rgb(255, 220, 105));
        currentAngle += 34f;

        // 4. Obesitas Kelas I
        drawSegment(canvas, rect, currentAngle, 34f, Color.rgb(255, 169, 38));
        currentAngle += 34f;

        // 5. Obesitas Kelas II
        drawSegment(canvas, rect, currentAngle, 34f, Color.rgb(255, 105, 42));
        currentAngle += 34f;

        // 6. Obesitas Kelas III
        drawSegment(canvas, rect, currentAngle, 35f, Color.rgb(255, 73, 32));

        drawIndicator(canvas, rect, startAngle, totalAngle);

        textPaint.setTextSize(size * 0.18f);
        canvas.drawText(
                String.format("%.2f", bmi),
                width / 2f,
                height * 0.50f,
                textPaint
        );

        labelPaint.setTextSize(size * 0.08f);
        labelPaint.setFakeBoldText(true);
        canvas.drawText(
                kategori,
                width / 2f,
                height * 0.74f,
                labelPaint
        );
    }

    private void drawSegment(Canvas canvas, RectF rect, float startAngle, float sweepAngle, int color) {
        arcPaint.setColor(color);
        canvas.drawArc(rect, startAngle, sweepAngle, false, arcPaint);
    }

    private void drawIndicator(Canvas canvas, RectF rect, float startAngle, float totalAngle) {
        float safeBmi = bmi;

        if (safeBmi < minBmi) safeBmi = minBmi;
        if (safeBmi > maxBmi) safeBmi = maxBmi;

        float percent = (safeBmi - minBmi) / (maxBmi - minBmi);
        float angle = startAngle + (percent * totalAngle);

        float centerX = rect.centerX();
        float centerY = rect.centerY();

        float radius = rect.width() / 2f;

        double rad = Math.toRadians(angle);

        float x1 = centerX + (float) Math.cos(rad) * (radius - 8f);
        float y1 = centerY + (float) Math.sin(rad) * (radius - 8f);

        float x2 = centerX + (float) Math.cos(rad) * (radius + 8f);
        float y2 = centerY + (float) Math.sin(rad) * (radius + 8f);

        canvas.drawLine(x1, y1, x2, y2, indicatorPaint);
    }
}
