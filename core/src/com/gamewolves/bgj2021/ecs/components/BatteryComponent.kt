import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class BatteryComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var emptyTexture = TextureRegion()
    var chargedTexture = TextureRegion()
    var charge = 0
    var maxCharge = 0
    var id = 0

    override fun reset() {
        position.set(0f, 0f)
        emptyTexture = TextureRegion()
        chargedTexture = TextureRegion()
        charge = 0
        maxCharge = 0
        id = 0
    }

    companion object {
        val mapper = mapperFor<BatteryComponent>()
    }
}