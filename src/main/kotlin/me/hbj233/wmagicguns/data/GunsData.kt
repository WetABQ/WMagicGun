package me.hbj233.wmagicguns.data

/**
 * QuickShop
 *
 * @author WetABQ Copyright (c) 2018.09
 * @version 1.0
 */
class GunsData(var name: String,
               var itemId : Int,
               var maxBullets: Int,
               var reloadSpeed: Float,
               var infiniteBullet: Boolean,
               var price: Int,
               var fireSpeed: Float,
               var criticalDamage: Float,
               var generalDamage: Float,
               var bulletType: Int,
               var bindingBulletId: Int,
               var standRecoil: Float,
               var sneakRecoil: Float,
               var walkRecoil: Float,
               var sprintRecoil: Float,
               var jumpRecoil: Float,
               var recoilMaxOffset: Float,
               var muzzleParticle: String,
               var hitParticle: String,
               var particleR: Int,
               var particleG: Int,
               var particleB: Int,
               var moveBallisticOffset: Float,
               var jumpBallisticOffset: Float,
               var intermittent: Boolean,
               var queueFireCount: Int,
               var fireEachTicks: Int) {

    fun toMap() : Map<String,Any>{
        return hashMapOf(
                "name" to name,
                "itemId" to itemId,
                "maxBullets" to maxBullets,
                "reloadSpeed" to reloadSpeed,
                "infiniteBullet" to infiniteBullet,
                "price" to price,
                "fireSpeed" to fireSpeed,
                "criticalDamage" to criticalDamage,
                "generalDamage" to generalDamage,
                "bulletType" to bulletType,
                "bindingBulletId" to bindingBulletId,
                "standRecoil" to standRecoil,
                "sneakRecoil" to sneakRecoil,
                "walkRecoil" to walkRecoil,
                "sprintRecoil" to sprintRecoil,
                "jumpRecoil" to jumpRecoil,
                "recoilMaxOffset" to recoilMaxOffset,
                "muzzleParticle" to muzzleParticle,
                "hitParticle" to hitParticle,
                "particleR" to particleR,
                "particleG" to particleG,
                "particleB" to particleB,
                "moveBallisticOffset" to moveBallisticOffset,
                "jumpBallisticOffset" to jumpBallisticOffset,
                "intermittent" to intermittent,
                "queueFireCount" to queueFireCount,
                "fireEachTicks" to fireEachTicks
        )
    }

    fun toTranslateMap() : Map<String,Any>{
        return linkedMapOf(
                "枪械名称" to name,
                "枪械物品Id" to itemId,
                "最大子弹数量" to maxBullets,
                "重装速度" to reloadSpeed,
                "是否无限子弹" to infiniteBullet,
                "价格" to price,
                "开火速度,单位ms" to fireSpeed,
                "暴头伤害" to criticalDamage,
                "一般伤害" to generalDamage,
                "子弹类型" to bulletType,
                "枪械使用的弹药物品Id" to bindingBulletId,
                "站立时后坐力" to standRecoil,
                "潜行时后坐力" to sneakRecoil,
                "行走时后坐力" to walkRecoil,
                "奔跑时后坐力" to sprintRecoil,
                "跳跃时后坐力" to jumpRecoil,
                "最大的后坐力" to recoilMaxOffset,
                "枪口粒子" to muzzleParticle,
                "击中粒子" to hitParticle,
                "枪械轨迹颜色R" to particleR,
                "枪械轨迹颜色G" to particleG,
                "枪械轨迹颜色B" to particleB,
                "移动时弹道偏移" to moveBallisticOffset,
                "跳跃时弹道偏移" to jumpBallisticOffset,
                "换弹是否打断" to intermittent,
                "单次点击触发的开火次数" to queueFireCount,
                "每Tick开火次数" to fireEachTicks
        )
    }
}