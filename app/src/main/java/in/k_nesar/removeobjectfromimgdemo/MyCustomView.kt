package `in`.k_nesar.removeobjectfromimgdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View

class MyCustomView(context: Context) : View(context) {

    private var destBitmap: Bitmap? = null
    private var destCanvas: Canvas? = null
    private var destPaint: Paint? = null
    private var destPath: Path = Path()
    val mutableListOfXPos = mutableListOf<Float>()
    val mutableListOfYPos = mutableListOf<Float>()

    fun createView(selectedImagePath: String?) {
        val rawBitmap = BitmapFactory.decodeFile(selectedImagePath)
        Log.d("sizeOfImage", "createView: ${rawBitmap.width}, ${rawBitmap.height}")

        destBitmap = Bitmap.createBitmap(rawBitmap.width, rawBitmap.height, Bitmap.Config.ARGB_8888)
        destCanvas = Canvas()
        destCanvas?.setBitmap(destBitmap)
        destCanvas?.drawBitmap(rawBitmap, 0f, 0f, null)

        destPaint = Paint()
        destPaint?.alpha = 0
        destPaint?.isAntiAlias = true
        destPaint?.style = Paint.Style.STROKE
        destPaint?.strokeJoin = Paint.Join.ROUND
        destPaint?.strokeCap = Paint.Cap.ROUND
        destPaint?.strokeWidth = 20f
        destPaint?.color = resources.getColor(R.color.purple_200)
        //   destPaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    }

    override fun onDraw(canvas: Canvas?) {
        destPaint?.let { destCanvas?.drawPath(destPath, it) }
        destBitmap?.let { canvas?.drawBitmap(it, 0f, 0f, null) }
        super.onDraw(canvas)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val xPos = event?.x
        val yPos = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (xPos != null) {
                    if (yPos != null) {
                        destPath.moveTo(xPos, yPos)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (xPos != null) {
                    if (yPos != null) {
                        destPath.lineTo(xPos, yPos)
                        mutableListOfXPos.add(xPos)
                        mutableListOfYPos.add(yPos)
                        Log.d(
                            "xAndyValues",
                            "onTouchEvent:${mutableListOfXPos.get(0)} , ${mutableListOfYPos.get(0)} "
                        )
                        Log.d("destPath", "onTouchEvent: $xPos , $yPos")
                    }
                }
            }

            else -> {
                return false
            }
        }

        invalidate()
        return true
    }

}