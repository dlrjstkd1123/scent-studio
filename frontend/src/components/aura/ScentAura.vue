<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

/**
 * 향 아우라: 향마다 연기 구름이 하나씩, 화면 가운데에서 천천히 일렁이며 서로 섞이는 모습 (WebGL).
 * - 색: 계열 색(DB scent_family.hex_color_code). 한 연기가 짙은 곳은 그 색, 겹친 곳은 섞인 색
 * - 크기 · 짙기: 비중이 클수록 연기 구름이 크고 짙다
 * - 움직임: 구름들이 가운데 주변을 아주 천천히 돌며 겹치고, 계열마다 일렁이는 속도 · 소용돌이가 다르다
 * - 빛: 가운데에서 은은한 빛이 연기를 비추고, 작은 빛 알갱이가 떠다니며 반짝인다
 * WebGL 이 안 되는 기기는 같은 색의 그라데이션으로 대신 보여준다.
 */
const props = defineProps({
  /** [{ code, color: '#RRGGBB', pct }] — 앞에서부터 4개까지 쓴다. 비어 있으면 연기 없음 */
  families: { type: Array, default: () => [] },
  /** 화면 낭독기용 설명 */
  label: { type: String, default: '향 아우라' }
})

const MAX_FAMILIES = 4

/** 계열별 연기 성격. speed: 피어오르는 속도, turb: 흔들림 · 소용돌이 */
const CHARACTER = {
  CITRUS: { speed: 1.35, turb: 1.15 },
  FRUITY: { speed: 1.2, turb: 1.1 },
  FLORAL: { speed: 1.0, turb: 1.0 },
  GREEN: { speed: 1.05, turb: 1.0 },
  AROMATIC: { speed: 1.1, turb: 1.05 },
  AQUATIC: { speed: 1.15, turb: 0.9 },
  SPICY: { speed: 1.05, turb: 1.4 },
  WOODY: { speed: 0.7, turb: 0.85 },
  AMBER: { speed: 0.75, turb: 0.9 },
  GOURMAND: { speed: 0.8, turb: 0.8 },
  MUSK: { speed: 0.85, turb: 0.6 },
  POWDERY: { speed: 0.8, turb: 0.65 },
  LEATHER: { speed: 0.65, turb: 0.9 }
}
const DEFAULT_CHARACTER = { speed: 1, turb: 1 }
const DEFAULT_COLOR = '#C9A46A'

/** 바뀐 값으로 부드럽게 넘어가는 정도 (프레임마다) */
const EASE = 0.04
/** 빠진 향의 연기가 옅어지는 속도 (60fps 기준 프레임마다, 약 0.8초) */
const FADE = 0.07
/** 새 연기가 피어나는 속도 (60fps 기준 프레임마다, 약 1.3초) */
const GROW = 0.035
/** 일렁임 진행 속도 (계열 속도에 곱한다) */
const SWIRL = 0.6
/** 화면 밀도 상한 (높으면 선명하지만 느려진다) */
const MAX_PIXEL_RATIO = 1.5

const VERTEX_SHADER = `
attribute vec2 aPos;
void main() {
  gl_Position = vec4(aPos, 0.0, 1.0);
}
`

const FRAGMENT_SHADER = `
precision highp float;
uniform vec2 uRes;
uniform float uTime;
uniform vec3 uColors[4];
uniform float uWeights[4];
uniform float uPhases[4];
uniform float uAlphas[4];
uniform float uTurbs[4];
uniform float uAngles[4];

vec2 hash(vec2 p) {
  p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
  return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

// 부드럽게 이어지는 무작위 값 (연기 결의 재료)
float noise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  vec2 u = f * f * (3.0 - 2.0 * f);
  float a = dot(hash(i), f);
  float b = dot(hash(i + vec2(1.0, 0.0)), f - vec2(1.0, 0.0));
  float c = dot(hash(i + vec2(0.0, 1.0)), f - vec2(0.0, 1.0));
  float d = dot(hash(i + vec2(1.0, 1.0)), f - vec2(1.0, 1.0));
  return mix(mix(a, b, u.x), mix(c, d, u.x), u.y) * 0.5 + 0.5;
}

// 크고 작은 무늬를 겹친 노이즈
float fbm(vec2 p) {
  float v = 0.0;
  float amp = 0.5;
  mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
  for (int i = 0; i < 5; i++) {
    v += amp * noise(p);
    p = m * p;
    amp *= 0.5;
  }
  return v;
}

// 색 성격(밝고 어두움)은 그대로 두고 채도만 올린다 (우디는 갈색, 머스크는 밝은 회백색으로 보이게)
vec3 vivid(vec3 c) {
  float l = dot(c, vec3(0.299, 0.587, 0.114));
  return clamp(mix(vec3(l), c, 1.7) * 1.15, 0.0, 1.0);
}

// 향 하나의 연기 구름: 제자리에서 천천히 일렁이고 말린다 (선 없이 덩어리만)
float cloud(vec2 uv, vec2 center, float radius, float t, float seed, float turb) {
  vec2 local = uv - center;
  vec2 p = local * 2.4 + vec2(seed, seed * 0.7);
  vec2 w = vec2(
    fbm(p + vec2(t * 0.18, -t * 0.12)),
    fbm(p + vec2(4.3, 1.9) + vec2(-t * 0.14, t * 0.16))
  );
  vec2 r = vec2(
    fbm(p + turb * 2.2 * w + vec2(1.7, 9.2) + t * 0.08),
    fbm(p + turb * 2.2 * w + vec2(8.3, 2.8) - t * 0.08)
  );
  float dens = fbm(p + turb * 2.4 * r);
  float shape = exp(-dot(local, local) / (radius * radius));
  return smoothstep(0.38, 0.85, dens) * shape;
}

// 공기 중에 천천히 떠다니며 반짝이는 작은 빛 알갱이 (일부 칸에만)
float sparkles(vec2 uv, float t) {
  vec2 g = uv * 16.0 + vec2(t * 0.25, t * 0.18);
  vec2 id = floor(g);
  vec2 f = fract(g) - 0.5;
  float dist = length(f - hash(id) * 0.35);
  float twinkle = 0.5 + 0.5 * sin(t * 2.0 + dot(id, vec2(12.9, 78.2)));
  float on = step(0.8, fract(sin(dot(id, vec2(41.3, 289.1))) * 43758.5453));
  return smoothstep(0.07, 0.0, dist) * twinkle * on;
}

void main() {
  vec2 uv = (gl_FragCoord.xy - 0.5 * uRes) / uRes.y;

  float maxW = 0.0;
  for (int i = 0; i < 4; i++) {
    maxW = max(maxW, uWeights[i]);
  }

  // 향마다 연기 구름 하나. 가운데 주변을 아주 천천히 돌며 서로 겹치고 섞인다
  vec3 acc = vec3(0.0);
  float total = 0.0;
  for (int i = 0; i < 4; i++) {
    float w = uWeights[i];
    if (w > 0.0001) {
      float fi = float(i);
      float rel = w / maxW;
      // 일렁임 진행도 (화면 쪽에서 향마다 누적 → 속도가 바뀌어도 무늬가 튀지 않는다)
      float t = uPhases[i];
      // 피어나는 정도: 새 연기는 작고 옅게 시작해 커지고 짙어진다
      float a = uAlphas[i] * uAlphas[i] * (3.0 - 2.0 * uAlphas[i]);
      // 자리 각도는 화면 쪽에서 고르게 나눈 뒤 부드럽게 옮겨 준다 (여기선 전체가 천천히 돈다)
      float angle = uAngles[i] + uTime * 0.09;
      vec2 center = vec2(cos(angle), sin(angle * 1.3)) * 0.13;
      // 숨 쉬듯 천천히 커졌다 작아진다
      float breathe = 1.0 + 0.12 * sin(uTime * 0.5 + fi * 2.1);
      float radius = (0.16 + 0.1 * rel) * breathe * (0.65 + 0.35 * a);
      float d = cloud(uv, center, radius, t, fi * 7.31 + 1.3, uTurbs[i]);
      d *= (0.5 + 0.7 * rel) * a;
      acc += vivid(uColors[i]) * (d + d * d * 1.2);
      total += d;
    }
  }

  // 겹친 곳은 색이 섞이고, 한 연기가 짙은 곳은 그 향의 색이 그대로 보인다
  vec3 col = acc / max(total, 0.0001);
  // 가운데에 모이고 가장자리는 비운다 (아우라 테두리가 일렁이게 노이즈를 섞는다)
  float edge = fbm(uv * 3.0 + uTime * 0.12);
  total *= exp(-dot(uv, uv) * (3.2 - edge * 1.6));
  float alpha = 1.0 - exp(-total * 1.7);

  // 가운데에서 은은하게 퍼지는 빛이 연기를 안쪽에서 비춘다
  float light = exp(-dot(uv, uv) * 5.0);
  vec3 warm = vec3(1.0, 0.86, 0.68);
  // 별빛 알갱이는 연기가 없어도 늘 보이고, 연기 속에서는 더 밝게 반짝인다
  float sparkle = sparkles(uv, uTime) * (0.5 + 0.5 * smoothstep(0.05, 0.5, alpha));

  vec3 c = col * alpha * 1.45 * (1.0 + 0.8 * light);
  c += mix(warm, col, 0.5) * light * 0.12 * alpha;
  c += mix(warm, col, 0.4) * sparkle * 1.2;
  // 아주 밝은 곳이 하얗게 날아가지 않도록 부드럽게 누른다
  c = 1.0 - exp(-c * 1.35);

  vec3 bg = vec3(0.05, 0.032, 0.022) + vec3(0.05, 0.03, 0.018) * (1.0 - length(uv) * 1.3);
  gl_FragColor = vec4(bg * (1.0 - alpha * 0.6) + c, 1.0);
}
`

const canvasRef = ref(null)
const supported = ref(true)

const hexToRgb = (hex) => {
  const v = parseInt((hex || DEFAULT_COLOR).replace('#', ''), 16)
  return [((v >> 16) & 255) / 255, ((v >> 8) & 255) / 255, (v & 255) / 255]
}

/** 계열 목록 → 셰이더에 넘길 값 (앞에서부터 4개, 비중 합 1) */
const TWO_PI = Math.PI * 2
/** 연기 구름이 새 각도로 옮겨 가는 속도 (프레임마다, 1~2초) */
const ANGLE_EASE = 0.03

/** 처음부터 고른 향이 없을 때 */
const emptyMix = () => ({
  colors: Array(MAX_FAMILIES * 3).fill(0),
  weights: Array(MAX_FAMILIES).fill(0),
  speeds: Array(MAX_FAMILIES).fill(1),
  turbs: Array(MAX_FAMILIES).fill(1),
  active: Array(MAX_FAMILIES).fill(false)
})

/**
 * 칸(0~3)마다 들어 있는 향 코드. 향은 빠질 때까지 같은 칸(같은 연기 모양)을 쓴다.
 * 칸은 위치가 아니다 — 위치(각도)는 고른 개수에 맞춰 고르게 나눈다.
 */
const slots = Array(MAX_FAMILIES).fill(null)
/** 칸마다 마지막 색 · 성격 (향이 빠져도 자기 모습 그대로 옅어지며 사라지게) */
const slotColors = Array.from({ length: MAX_FAMILIES }, () => hexToRgb(DEFAULT_COLOR))
const slotCharacters = Array(MAX_FAMILIES).fill(DEFAULT_CHARACTER)

const normAngle = (a) => ((a % TWO_PI) + TWO_PI) % TWO_PI

/** 다른 연기들 사이에서 가장 넓게 빈 각도 (새 연기가 피어날 곳) */
const widestGap = (others) => {
  if (!others.length) return 0
  const sorted = others.map(normAngle).sort((a, b) => a - b)
  let best = { size: -1, mid: 0 }
  sorted.forEach((a, k) => {
    const next = k + 1 < sorted.length ? sorted[k + 1] : sorted[0] + TWO_PI
    if (next - a > best.size) best = { size: next - a, mid: a + (next - a) / 2 }
  })
  return best.mid
}

/** 빠진 향의 칸을 비우고, 새 향은 옛 연기가 가장 많이 사라진 빈 칸에 넣는다 */
const assignSlots = (top) => {
  slots.forEach((code, s) => {
    if (code && !top.some((f) => f.code === code)) slots[s] = null
  })
  top.forEach((f) => {
    if (slots.includes(f.code)) return
    // 옛 연기가 가장 많이 사라진 칸을 쓴다 (아직 보이는 옛 연기를 새 향이 물려받지 않게)
    const alphaNow = (s) => (current ? current.alphas[s] : 0)
    const free = slots
      .map((code, s) => (code ? -1 : s))
      .filter((s) => s >= 0)
      .sort((a, b) => alphaNow(a) - alphaNow(b))
    const s = free[0]
    slots[s] = f.code
    if (current) {
      const others = slots
        .map((code, k) => (code && k !== s ? current.angles[k] : null))
        .filter((a) => a !== null)
      current.angles[s] = widestGap(others)
    }
  })
}

/** 고른 향들의 목표 각도: 지금 놓인 순서를 지키며 고르게 나눈다 (가장 가까운 쪽으로 이동) */
const angleGoals = (angles, active) => {
  const goals = [...angles]
  const on = active.map((a, s) => (a ? s : -1)).filter((s) => s >= 0)
  if (!on.length) return goals
  on.sort((a, b) => normAngle(angles[a]) - normAngle(angles[b]))
  const base = angles[on[0]]
  on.forEach((s, k) => {
    let goal = base + (TWO_PI * k) / on.length
    while (goal - angles[s] > Math.PI) goal -= TWO_PI
    while (angles[s] - goal > Math.PI) goal += TWO_PI
    goals[s] = goal
  })
  return goals
}

/** 마지막으로 그린 조합 (향을 모두 끄면 이 모양 그대로 옅어지며 사라진다) */
let lastMix = null

const target = computed(() => {
  const top = props.families.filter((f) => f.pct > 0).slice(0, MAX_FAMILIES)
  const sum = top.reduce((a, f) => a + f.pct, 0)
  if (!sum) {
    // 마지막 모양은 그대로 두고 모두 '빠진 향' 으로 → 향마다 옅어지며 사라진다
    return lastMix ? { ...lastMix, active: Array(MAX_FAMILIES).fill(false) } : emptyMix()
  }
  assignSlots(top)
  const colors = []
  const weights = []
  const speeds = []
  const turbs = []
  const active = []
  for (let s = 0; s < MAX_FAMILIES; s++) {
    const f = top.find((item) => item.code === slots[s])
    if (f) {
      slotColors[s] = hexToRgb(f.color)
      slotCharacters[s] = CHARACTER[f.code] || DEFAULT_CHARACTER
    }
    colors.push(...slotColors[s])
    weights.push(f ? f.pct / sum : 0)
    speeds.push(slotCharacters[s].speed)
    turbs.push(slotCharacters[s].turb)
    active.push(Boolean(f))
  }
  lastMix = { colors, weights, speeds, turbs, active }
  return lastMix
})

/** 현재 그리고 있는 값 (target 쪽으로 조금씩 다가간다) */
let current = null
let gl = null
let program = null
let uniforms = {}
let frameId = 0
let startTime = 0
/** 지난 프레임 시각 (초) — 프레임 사이 시간으로 일렁임 진행도를 누적한다 */
let lastSeconds = 0
let resizeObserver = null
const reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches

const compile = (type, source) => {
  const shader = gl.createShader(type)
  gl.shaderSource(shader, source)
  gl.compileShader(shader)
  if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
    throw new Error(gl.getShaderInfoLog(shader))
  }
  return shader
}

const setup = () => {
  gl = canvasRef.value.getContext('webgl', { antialias: false, premultipliedAlpha: false })
  if (!gl) return false
  program = gl.createProgram()
  gl.attachShader(program, compile(gl.VERTEX_SHADER, VERTEX_SHADER))
  gl.attachShader(program, compile(gl.FRAGMENT_SHADER, FRAGMENT_SHADER))
  gl.linkProgram(program)
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
    throw new Error(gl.getProgramInfoLog(program))
  }
  gl.useProgram(program)

  // 화면 전체를 덮는 사각형 하나 (점마다 셰이더가 색을 정한다)
  const buffer = gl.createBuffer()
  gl.bindBuffer(gl.ARRAY_BUFFER, buffer)
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, 1, 1]), gl.STATIC_DRAW)
  const aPos = gl.getAttribLocation(program, 'aPos')
  gl.enableVertexAttribArray(aPos)
  gl.vertexAttribPointer(aPos, 2, gl.FLOAT, false, 0, 0)

  uniforms = {
    res: gl.getUniformLocation(program, 'uRes'),
    time: gl.getUniformLocation(program, 'uTime'),
    colors: gl.getUniformLocation(program, 'uColors'),
    weights: gl.getUniformLocation(program, 'uWeights'),
    phases: gl.getUniformLocation(program, 'uPhases'),
    alphas: gl.getUniformLocation(program, 'uAlphas'),
    turbs: gl.getUniformLocation(program, 'uTurbs'),
    angles: gl.getUniformLocation(program, 'uAngles')
  }
  return true
}

const resize = () => {
  const canvas = canvasRef.value
  if (!canvas || !gl) return
  const ratio = Math.min(window.devicePixelRatio || 1, MAX_PIXEL_RATIO)
  canvas.width = Math.max(1, Math.round(canvas.clientWidth * ratio))
  canvas.height = Math.max(1, Math.round(canvas.clientHeight * ratio))
  gl.viewport(0, 0, canvas.width, canvas.height)
  if (reducedMotion) draw(12)
}

/** 60fps 기준 비율 k 를 실제 프레임 시간에 맞춘다 (느린 기기에서도 같은 시간에 옮겨 가게) */
const rate = (k, dt) => 1 - Math.pow(1 - k, dt * 60)

const ease = (from, to, k) => from.map((v, i) => v + (to[i] - v) * k)

const draw = (seconds) => {
  const goal = target.value
  // 지난 프레임과의 시간 (탭을 오래 비웠다 돌아와도 한 번에 튀지 않게 최대 0.1초)
  const dt = Math.min(Math.max(seconds - lastSeconds, 0), 0.1)
  lastSeconds = seconds

  if (!current || reducedMotion) {
    const angles = current
      ? current.angles
      : Array.from({ length: MAX_FAMILIES }, (_, k) => (TWO_PI * k) / MAX_FAMILIES)
    current = {
      colors: [...goal.colors],
      weights: [...goal.weights],
      speeds: [...goal.speeds],
      turbs: [...goal.turbs],
      angles: angleGoals(angles, goal.active),
      // 처음 열 때도 연기가 피어나며 나타나게 0 에서 시작 (움직임 줄이기 설정이면 바로 표시)
      alphas: reducedMotion
        ? goal.active.map((on) => (on ? 1 : 0))
        : Array(MAX_FAMILIES).fill(0),
      phases: current ? current.phases : Array(MAX_FAMILIES).fill(0)
    }
  } else {
    const k = rate(EASE, dt)
    current.colors = ease(current.colors, goal.colors, k)
    current.weights = ease(current.weights, goal.weights, k)
    current.speeds = ease(current.speeds, goal.speeds, k)
    current.turbs = ease(current.turbs, goal.turbs, k)
    const goals = angleGoals(current.angles, goal.active)
    current.angles = ease(current.angles, goals, rate(ANGLE_EASE, dt))
    // 새 연기는 천천히 피어나고, 빠진 연기는 옅어지기만 한다 (다시 살아나지 않음)
    const grow = rate(GROW, dt)
    const fade = rate(FADE, dt)
    current.alphas = current.alphas.map((a, s) =>
      goal.active[s] ? a + (1 - a) * grow : a - a * fade
    )
    current.phases = current.phases.map((ph, s) => ph + dt * current.speeds[s] * SWIRL)
  }
  // 거의 안 보이는 칸은 색을 바로 바꾼다 (새 향이 다른 색에서 물들며 나타나지 않게)
  for (let s = 0; s < MAX_FAMILIES; s++) {
    if (current.alphas[s] < 0.02) {
      current.colors.splice(s * 3, 3, ...goal.colors.slice(s * 3, s * 3 + 3))
      if (!goal.active[s]) current.alphas[s] = 0
    }
  }
  gl.uniform2f(uniforms.res, gl.canvas.width, gl.canvas.height)
  gl.uniform1f(uniforms.time, seconds)
  gl.uniform3fv(uniforms.colors, current.colors)
  gl.uniform1fv(uniforms.weights, current.weights)
  gl.uniform1fv(uniforms.phases, current.phases)
  gl.uniform1fv(uniforms.alphas, current.alphas)
  gl.uniform1fv(uniforms.turbs, current.turbs)
  gl.uniform1fv(uniforms.angles, current.angles)
  gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4)
}

const loop = (now) => {
  draw((now - startTime) / 1000)
  frameId = requestAnimationFrame(loop)
}

// 움직임 줄이기 설정이면 멈춘 한 장면만 다시 그린다
watch(target, () => {
  if (reducedMotion && gl) draw(12)
})

/** WebGL 이 안 될 때: 같은 색 그라데이션 */
const fallbackStyle = computed(() => {
  const stops = props.families.slice(0, MAX_FAMILIES).map((f) => f.color || DEFAULT_COLOR)
  if (!stops.length) return { background: '#120b07' }
  const [a, b, c] = [...stops, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR]
  const gradient = `${a}cc 0%, ${b}66 35%, ${c}22 60%, #120b07 80%`
  return { background: `radial-gradient(ellipse at 50% 50%, ${gradient})` }
})

onMounted(() => {
  try {
    supported.value = setup()
  } catch (err) {
    console.log(err)
    supported.value = false
  }
  if (!supported.value) return
  resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(canvasRef.value)
  resize()
  if (!reducedMotion) {
    startTime = performance.now()
    frameId = requestAnimationFrame(loop)
  }
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frameId)
  resizeObserver?.disconnect()
  gl?.getExtension('WEBGL_lose_context')?.loseContext()
})
</script>

<template>
  <div class="aura" role="img" :aria-label="label">
    <canvas v-show="supported" ref="canvasRef" class="aura-canvas" aria-hidden="true" />
    <div v-if="!supported" class="aura-fallback" :style="fallbackStyle" aria-hidden="true" />
  </div>
</template>

<style scoped>
.aura {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #120b07;
}

.aura-canvas,
.aura-fallback {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
