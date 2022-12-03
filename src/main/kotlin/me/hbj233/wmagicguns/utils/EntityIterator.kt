package me.hbj233.wmagicguns.utils

import cn.nukkit.entity.Entity
import cn.nukkit.level.Level
import cn.nukkit.level.particle.FlameParticle
import cn.nukkit.level.particle.Particle
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.Vector3
import java.util.*
import java.util.concurrent.LinkedBlockingDeque


class EntityIterator constructor(private val level: Level,
                                 start: Vector3, direction: Vector3,
                                 private val width: Double = 0.1,
                                 private val maxDistance: Int = 120) :
        MutableIterator<Entity?> {

    private var end = false
    private var currentEntityObject: Entity? = null
    private var currentDistance = 0.0
    private val startPosition: Vector3
    var currentPosition: Vector3 = Vector3()
    private var direction: Vector3 = Vector3()
    private var particle: Particle = FlameParticle(Vector3())
    private val queueEntity: Queue<Entity> = LinkedBlockingDeque()

    init {
        currentPosition = start.clone()
        startPosition = start.clone()
        this.direction = direction.normalize()
    }

    fun setParticle(particle: Particle): EntityIterator {
        this.particle = particle
        return this
    }

    override fun next(): Entity? {
        return currentEntityObject
    }

    override fun hasNext(): Boolean {
        scan()
        //println("END:$end , $currentEntityObject")
        return !end && currentEntityObject != null
    }

    private fun scan() {
        if (maxDistance != 0 && currentDistance > maxDistance) {
            end = true
            return
        }
        if (end) return
        if (!queueEntity.isEmpty()) {
            currentEntityObject = queueEntity.poll()
            return
        }
        currentEntityObject = null
        var out = false
        do {
            if (currentDistance > 2 && Random().nextInt(100) < 35) {
                particle.setComponents(currentPosition.x, currentPosition.y, currentPosition.z)
                level.addParticle(particle)
            }
            val block = level.getBlock(currentPosition)
            if (block.boundingBox != null && block.boundingBox.isVectorInside(currentPosition)) {
                end = true
            } else {
                val box: AxisAlignedBB = object : AxisAlignedBB {
                    //this.currentPosition.x, this.currentPosition.y, this.currentPosition.z, this.currentPosition.x, this.currentPosition.y, this.currentPosition.z
                    override fun getMinX(): Double = currentPosition.x - 0.1
                    override fun getMinY(): Double = currentPosition.y - 0.1
                    override fun getMinZ(): Double = currentPosition.z - 0.1
                    override fun getMaxX(): Double = currentPosition.x + 0.1
                    override fun getMaxY(): Double = currentPosition.y + 0.1
                    override fun getMaxZ(): Double = currentPosition.z + 0.1
                    override fun clone(): AxisAlignedBB = null!!
                }
                //Entity[] list = this.getNearbyEntities(box, this.currentEntityObject);
                //val list = level.entities.filter { it.distance(currentPosition) < 1.8}
                //Entity[] list = this.getNearbyEntities(box, this.currentEntityObject);
                val list = level.entities.filter { it.boundingBox.intersectsWith(box) }
                for (entity in list) {
                    queueEntity.offer(entity)
                    out = true
                }
            }

            currentPosition = currentPosition.add(direction.multiply(0.1))
            currentDistance = currentPosition.distance(startPosition)

            //currentDistance = this.currentPosition?.distance(startPosition)

            if (maxDistance != 0 && currentDistance > maxDistance) end = true
        } while (!end && !out)
        currentEntityObject = queueEntity.poll()
    }

    override fun remove() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}