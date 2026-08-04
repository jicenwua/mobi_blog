<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useSeasonTheme } from '@/composables/useSeasonTheme'

const { activeSeason } = useSeasonTheme()

const canvasRef = ref(null)
let ctx = null
let particles = []
let animationId = 0
let width = 0
let height = 0

const PARTICLE_COUNT = {
  spring: 36,
  summer: 220,
  autumn: 32,
  winter: 34,
}

function rand(min, max) {
  return min + Math.random() * (max - min)
}

function createParticle(season) {
  const base = {
    x: rand(0, width),
    y: rand(-height, 0),
    opacity: rand(0.5, 1),
  }

  switch (season) {
    case 'spring':
      return {
        ...base,
        size: rand(8, 14),
        speedY: rand(0.6, 1.2),
        speedX: rand(-0.4, 0.4),
        rotation: rand(0, Math.PI * 2),
        rotationSpeed: rand(-0.02, 0.02),
        sway: rand(0, Math.PI * 2),
        swaySpeed: rand(0.01, 0.025),
        hue: rand(330, 350),
      }
    case 'summer':
      return {
        ...base,
        x: rand(-60, width + 80),
        y: rand(-height * 0.3, height),
        length: rand(14, 26),
        speedX: rand(1.2, 2.5),
        speedY: rand(4, 7),
        width: rand(1.2, 2),
        opacity: rand(0.35, 0.7),
      }
    case 'autumn':
      return {
        ...base,
        size: rand(10, 18),
        speedY: rand(0.8, 1.6),
        speedX: rand(-0.6, 0.6),
        rotation: rand(0, Math.PI * 2),
        rotationSpeed: rand(-0.03, 0.03),
        sway: rand(0, Math.PI * 2),
        swaySpeed: rand(0.008, 0.02),
        hue: rand(25, 45),
        lightness: rand(45, 60),
      }
    case 'winter':
      return {
        ...base,
        size: rand(5, 12),
        speedY: rand(0.7, 1.8),
        speedX: rand(-0.5, 0.5),
        rotation: rand(0, Math.PI * 2),
        rotationSpeed: rand(-0.015, 0.015),
        sway: rand(0, Math.PI * 2),
        swaySpeed: rand(0.01, 0.025),
        opacity: rand(0.75, 1),
        variant: Math.random() > 0.35 ? 'star' : 'dot',
      }
    default:
      return base
  }
}

function initParticles(season) {
  const count = PARTICLE_COUNT[season] || 0
  particles = Array.from({ length: count }, () => {
    const p = createParticle(season)
    if (season === 'summer') {
      p.x = rand(-60, width + 80)
      p.y = rand(-height * 0.1, height)
    }
    return p
  })
}

function drawPeachBlossom(p) {
  ctx.save()
  ctx.translate(p.x, p.y)
  ctx.rotate(p.rotation)
  ctx.globalAlpha = p.opacity

  const petalCount = 5
  for (let i = 0; i < petalCount; i++) {
    ctx.save()
    ctx.rotate((Math.PI * 2 * i) / petalCount)
    ctx.beginPath()
    ctx.ellipse(0, -p.size * 0.45, p.size * 0.35, p.size * 0.55, 0, 0, Math.PI * 2)
    ctx.fillStyle = `hsl(${p.hue}, 85%, 82%)`
    ctx.fill()
    ctx.restore()
  }

  ctx.beginPath()
  ctx.arc(0, 0, p.size * 0.15, 0, Math.PI * 2)
  ctx.fillStyle = `hsl(${p.hue - 10}, 70%, 65%)`
  ctx.fill()
  ctx.restore()
}

function drawRainDrop(p) {
  const tailX = p.x + p.length * 0.42
  const tailY = p.y - p.length

  ctx.save()
  ctx.globalAlpha = p.opacity
  ctx.lineCap = 'round'
  ctx.strokeStyle = 'rgba(80, 150, 220, 0.75)'
  ctx.lineWidth = p.width
  ctx.beginPath()
  ctx.moveTo(p.x, p.y)
  ctx.lineTo(tailX, tailY)
  ctx.stroke()
  ctx.restore()
}

function drawMapleLeaf(p) {
  ctx.save()
  ctx.translate(p.x, p.y)
  ctx.rotate(p.rotation)
  ctx.globalAlpha = p.opacity
  ctx.fillStyle = `hsl(${p.hue}, 90%, ${p.lightness}%)`

  const s = p.size
  ctx.beginPath()
  ctx.moveTo(0, -s * 0.8)
  ctx.bezierCurveTo(s * 0.5, -s * 0.5, s * 0.7, -s * 0.1, s * 0.5, s * 0.2)
  ctx.bezierCurveTo(s * 0.3, s * 0.5, s * 0.1, s * 0.7, 0, s * 0.5)
  ctx.bezierCurveTo(-s * 0.1, s * 0.7, -s * 0.3, s * 0.5, -s * 0.5, s * 0.2)
  ctx.bezierCurveTo(-s * 0.7, -s * 0.1, -s * 0.5, -s * 0.5, 0, -s * 0.8)
  ctx.closePath()
  ctx.fill()

  ctx.strokeStyle = `hsl(${p.hue}, 80%, ${p.lightness - 15}%)`
  ctx.lineWidth = 0.8
  ctx.beginPath()
  ctx.moveTo(0, -s * 0.7)
  ctx.lineTo(0, s * 0.4)
  ctx.stroke()
  ctx.restore()
}

function drawSnowflake(p) {
  ctx.save()
  ctx.translate(p.x, p.y)
  ctx.rotate(p.rotation)
  ctx.globalAlpha = p.opacity
  ctx.shadowColor = 'rgba(80, 140, 220, 0.55)'
  ctx.shadowBlur = 6

  if (p.variant === 'star') {
    const armLen = p.size * 2.8
    ctx.strokeStyle = 'rgba(90, 150, 230, 0.7)'
    ctx.lineWidth = 1.4
    ctx.lineCap = 'round'

    for (let i = 0; i < 6; i++) {
      ctx.save()
      ctx.rotate((Math.PI * i) / 3)
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.lineTo(0, -armLen)
      ctx.stroke()

      ctx.beginPath()
      ctx.moveTo(0, -armLen * 0.55)
      ctx.lineTo(armLen * 0.22, -armLen * 0.72)
      ctx.moveTo(0, -armLen * 0.55)
      ctx.lineTo(-armLen * 0.22, -armLen * 0.72)
      ctx.stroke()
      ctx.restore()
    }

    ctx.fillStyle = 'rgba(255, 255, 255, 0.95)'
    ctx.beginPath()
    ctx.arc(0, 0, p.size * 0.35, 0, Math.PI * 2)
    ctx.fill()
  } else {
    ctx.fillStyle = 'rgba(255, 255, 255, 0.95)'
    ctx.strokeStyle = 'rgba(90, 150, 230, 0.55)'
    ctx.lineWidth = 1.2
    ctx.beginPath()
    ctx.arc(0, 0, p.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.stroke()
  }

  ctx.restore()
}

function updateParticle(p, season) {
  switch (season) {
    case 'spring':
      p.sway += p.swaySpeed
      p.x += p.speedX + Math.sin(p.sway) * 0.6
      p.y += p.speedY
      p.rotation += p.rotationSpeed
      if (p.y > height + 20) {
        Object.assign(p, createParticle(season), { y: -20 })
      }
      break
    case 'summer':
      p.x -= p.speedX
      p.y += p.speedY
      if (p.y > height + 30 || p.x < -60) {
        Object.assign(p, createParticle(season), {
          y: rand(-80, -10),
          x: rand(-60, width + 80),
        })
      }
      break
    case 'autumn':
      p.sway += p.swaySpeed
      p.x += p.speedX + Math.sin(p.sway) * 0.8
      p.y += p.speedY
      p.rotation += p.rotationSpeed
      if (p.y > height + 20) {
        Object.assign(p, createParticle(season), { y: -20 })
      }
      break
    case 'winter':
      p.sway += p.swaySpeed
      p.x += p.speedX + Math.sin(p.sway) * 0.8
      p.y += p.speedY
      p.rotation += p.rotationSpeed
      if (p.y > height + 20) {
        Object.assign(p, createParticle(season), { y: rand(-30, -5) })
      }
      break
  }
}

function drawParticle(p, season) {
  switch (season) {
    case 'spring':
      drawPeachBlossom(p)
      break
    case 'summer':
      drawRainDrop(p)
      break
    case 'autumn':
      drawMapleLeaf(p)
      break
    case 'winter':
      drawSnowflake(p)
      break
  }
}

function animate() {
  const season = activeSeason.value
  if (!ctx || !season) return

  ctx.clearRect(0, 0, width, height)

  for (const p of particles) {
    updateParticle(p, season)
    drawParticle(p, season)
  }

  animationId = requestAnimationFrame(animate)
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas) return

  width = window.innerWidth
  height = window.innerHeight
  canvas.width = width
  canvas.height = height
}

function start() {
  stop()
  const season = activeSeason.value
  if (!season || !canvasRef.value) return

  resize()
  ctx = canvasRef.value.getContext('2d')
  initParticles(season)
  animate()
}

function stop() {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = 0
  }
  particles = []
  if (ctx) {
    ctx.clearRect(0, 0, width, height)
  }
}

function handleResize() {
  resize()
  if (activeSeason.value) {
    initParticles(activeSeason.value)
  }
}

watch(activeSeason, (season) => {
  if (season) {
    start()
  } else {
    stop()
  }
})

onMounted(() => {
  window.addEventListener('resize', handleResize)
  if (activeSeason.value) {
    start()
  }
})

onUnmounted(() => {
  stop()
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <canvas
    v-show="activeSeason"
    ref="canvasRef"
    class="season-particles"
  />
</template>

<style scoped>
.season-particles {
  position: fixed;
  inset: 0;
  z-index: 50;
  pointer-events: none;
}
</style>
