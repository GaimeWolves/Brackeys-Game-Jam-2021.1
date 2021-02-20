import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class PowerSourceComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var texture = TextureRegion()

    override fun reset() {
        position.set(0f, 0f)
        texture = TextureRegion()
    }

    companion object {
        val mapper = mapperFor<PowerSourceComponent>()
    }
}