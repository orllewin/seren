package orllewin.seren


import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import orllewin.skiss.*
import orllewin.skiss.objects.Vector

class SplashSkiss(
    view: SkissView,
    @ColorInt var backgroundColour: Int,
    @ColorInt val orbitColour: Int): Skiss(view) {

    var planet: Int = Color.BLACK

    val planetDiam = screenWidth/4
    val originX = screenWidth/2
    val originY = screenHeight/2

    lateinit var blackHole: Vector

    private val shapes = mutableListOf<Shape>()

    override fun setup(width: Int, height: Int) {
        blackHole = Vector(screenWidth/2, screenHeight/2)

        planet = saturate(orbitColour, 0.25)

        shapes.clear()

        repeat(random(3, 6).toInt()){
            shapes.add(Shape(orbitColour, genRad()))
        }
    }

    private fun genRad(): Int{
        return (screenWidth/12 + random(screenWidth/12).toInt())/2
    }

    fun updateBackgroundColour(backgroundColour: Int){
        this.backgroundColour = backgroundColour
    }

    override fun update(canvas: Canvas) {
        background(backgroundColour)
        noStroke()

        fill(planet)
        circle(originX, originY, planetDiam)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                shapes.forEach { shape ->
                    shape.update()
                    fill(shape.color, BlendMode.MODULATE)
                    circle(shape.location.x, shape.location.y, shape.radius)
                }
            }
            else -> {
                shapes.forEach { shape ->
                    shape.update()
                    fill(shape.color)
                    circle(shape.location.x, shape.location.y, shape.radius)
                }
            }
        }
    }

    inner class Shape(val color: Int, val radius: Int){
        var location = Vector(originX + random(-screenWidth/4, screenWidth/4), originY + random(-screenHeight/4, screenHeight/4))
        private var velocity = Vector(0f, 0f)
        private var acceleration: Vector? = null
        private var maxSpeed = 10f

        var direction = Vector.randomDirection()

        fun update(): Shape {
            var agg = Vector(0, 0)
            location += direction
            var directionToBlackHole = blackHole - location
            directionToBlackHole.normalize()
            directionToBlackHole *= 1.4f
            agg += directionToBlackHole

            acceleration = agg/20

            velocity += acceleration!!
            velocity.limit(maxSpeed)
            location += velocity

            when {
                location.x > screenWidth || location.x < 0 -> {
                    velocity.x *= -1
                    velocity.x *= 0.5f
                }
            }
            when {
                location.y > screenHeight || location.y < 0 -> {
                    velocity.y *= -1
                    velocity.y *= 0.5f
                }
            }

            return this
        }
    }
}
