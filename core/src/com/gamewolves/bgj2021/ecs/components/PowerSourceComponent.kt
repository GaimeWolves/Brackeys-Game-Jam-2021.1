import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class PowerSourceComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)

    override fun reset() {
        position.set(0f, 0f)
    }

    companion object {
        val mapper = mapperFor<PowerSourceComponent>()
    }
}