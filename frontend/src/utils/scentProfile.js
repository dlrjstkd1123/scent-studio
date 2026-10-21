/**
 * 향수 노트 · 어코드 → 향 프로필(계열 비율 · 분위기) 계산과 추천 점수.
 * 노트는 DB 향료 사전(별칭 포함)으로, 메인 어코드는 어코드→계열 표로 계열에 매핑해서
 * 어떤 향수든 같은 기준(계열 비율)으로 비교한다.
 */

/**
 * weight: 계열 비율 계산 가중치. 금방 날아가는 탑보다 오래 남는 미들·베이스가 향의 인상을 더 좌우한다.
 */
export const NOTE_TYPES = [
  { key: 'top', label: 'TOP', name: '탑 노트', desc: '첫인상 · 뿌린 직후', weight: 1 },
  { key: 'middle', label: 'MIDDLE', name: '미들 노트', desc: '향의 중심', weight: 1.3 },
  { key: 'base', label: 'BASE', name: '베이스 노트', desc: '오래 남는 잔향', weight: 1.6 }
]

/**
 * 메인 어코드 강도(Main Accords Percentage)별 가중치.
 * 노트 10여 개의 가중치 합과 어코드 가중치 합이 비슷한 크기가 되도록 정했다.
 * 강도 값이 없는 어코드는 Moderate 로 본다.
 */
const ACCORD_WEIGHTS = { Dominant: 2, Prominent: 1.5, Moderate: 1, Subtle: 0.5 }

export const OTHER_COLOR = '#C9BFB4'
export const UNCLASSIFIED_COLOR = '#D9D2C8'

/** 계열이 이 수를 넘으면 상위 (MAX_SEGMENTS - 1)개만 따로 보여주고 나머지는 "기타" */
const MAX_SEGMENTS = 6

/** 계열별 분위기 표현. and: 연결형(~하고), mod: 수식형(~한) */
const MOOD_BY_FAMILY = {
  CITRUS: { and: '상쾌하고', mod: '상쾌한', words: ['상쾌한', '밝은', '산뜻한'] },
  FRUITY: { and: '발랄하고', mod: '발랄한', words: ['발랄한', '달콤한', '생기 있는'] },
  FLORAL: { and: '우아하고', mod: '우아한', words: ['우아한', '로맨틱한', '화사한'] },
  GREEN: { and: '싱그럽고', mod: '싱그러운', words: ['싱그러운', '자연스러운', '풋풋한'] },
  AROMATIC: { and: '청량하고', mod: '청량한', words: ['청량한', '깔끔한', '허브 같은'] },
  AQUATIC: { and: '시원하고', mod: '시원한', words: ['시원한', '맑은', '투명한'] },
  SPICY: { and: '강렬하고', mod: '강렬한', words: ['강렬한', '이국적인', '따뜻한'] },
  WOODY: { and: '차분하고', mod: '차분한', words: ['차분한', '깊은', '안정적인'] },
  AMBER: { and: '따뜻하고', mod: '따뜻한', words: ['따뜻한', '관능적인', '깊은'] },
  GOURMAND: { and: '달콤하고', mod: '달콤한', words: ['달콤한', '포근한', '부드러운'] },
  MUSK: { and: '포근하고', mod: '포근한', words: ['포근한', '깨끗한', '부드러운'] },
  POWDERY: { and: '보송하고', mod: '보송한', words: ['보송한', '클래식한', '부드러운'] },
  LEATHER: { and: '묵직하고', mod: '묵직한', words: ['묵직한', '세련된', '시크한'] }
}

const key = (name) => (name || '').trim().toLowerCase()

/** 합이 정확히 100 이 되도록 반올림 (최대 나머지 방식) */
const toPercents = (counts, total) => {
  const raw = counts.map((c) => (c / total) * 100)
  const floors = raw.map(Math.floor)
  let rest = 100 - floors.reduce((a, b) => a + b, 0)
  raw
    .map((r, i) => [r - floors[i], i])
    .sort((a, b) => b[0] - a[0])
    .forEach(([, i]) => {
      if (rest > 0) {
        floors[i] += 1
        rest -= 1
      }
    })
  return floors
}

/**
 * @param perfume { notes: { top, middle, base }, accords: [{ name, level }] }
 * @param dict    { familyByCode: Map, ingredientByName: Map(별칭 포함), accordByName: Map }
 */
export const buildProfile = (perfume, dict) => {
  const notesByType = {}
  const weights = new Map() // 계열 → 가중치 합
  const noteCounts = new Map() // 계열 → 노트 수 (표시용)
  const accordCounts = new Map() // 계열 → 어코드 수 (표시용)
  // 같은 향료 비교용 키: 사전에 있으면 정식 영문명(별칭도 같은 키), 없으면 노트 이름 그대로
  const noteKeys = new Set()
  let unclassified = 0

  const add = (familyCode, weight, counts) => {
    weights.set(familyCode, (weights.get(familyCode) || 0) + weight)
    counts.set(familyCode, (counts.get(familyCode) || 0) + 1)
  }

  NOTE_TYPES.forEach(({ key: type, weight }) => {
    notesByType[type] = (perfume.notes?.[type] || []).map((nameEn) => {
      const ingredient = dict.ingredientByName.get(key(nameEn))
      const family = ingredient?.familyCode ? dict.familyByCode.get(ingredient.familyCode) : null
      noteKeys.add(key(ingredient?.ingredientNameEn || nameEn))
      if (family) {
        add(family.familyCode, weight, noteCounts)
      } else {
        unclassified += 1
      }
      return {
        nameEn,
        nameKo: ingredient?.ingredientNameKo || '',
        family
      }
    })
  })

  const accords = perfume.accords || []
  accords.forEach(({ name, level }) => {
    const family = dict.familyByCode.get(dict.accordByName?.get(key(name)))
    const weight = ACCORD_WEIGHTS[level] || ACCORD_WEIGHTS.Moderate
    if (family) add(family.familyCode, weight, accordCounts)
  })

  const total = [...weights.values()].reduce((a, b) => a + b, 0)
  const sorted = [...weights.entries()]
    .map(([code, weight]) => ({
      family: dict.familyByCode.get(code),
      weight,
      noteCount: noteCounts.get(code) || 0,
      accordCount: accordCounts.get(code) || 0
    }))
    .sort((a, b) => b.weight - a.weight || a.family.sortOrder - b.family.sortOrder)
  const percents = total ? toPercents(sorted.map((f) => f.weight), total) : []
  const families = sorted.map((f, i) => ({ ...f, pct: percents[i] }))

  // 차트: 계열이 많으면 상위 몇 개 + 기타
  const shown = families.length > MAX_SEGMENTS ? MAX_SEGMENTS - 1 : families.length
  const segments = families.slice(0, shown).map((f) => ({
    code: f.family.familyCode,
    label: f.family.familyNameKo || f.family.familyNameEn,
    color: f.family.hexColorCode,
    pct: f.pct,
    noteCount: f.noteCount,
    accordCount: f.accordCount
  }))
  const rest = families.slice(shown)
  if (rest.length) {
    segments.push({
      code: 'OTHER',
      label: '기타',
      color: OTHER_COLOR,
      pct: rest.reduce((a, f) => a + f.pct, 0),
      noteCount: rest.reduce((a, f) => a + f.noteCount, 0),
      accordCount: rest.reduce((a, f) => a + f.accordCount, 0),
      members: rest.map((f) => f.family.familyNameKo)
    })
  }

  return {
    notesByType,
    families,
    segments,
    familyCodes: new Set(families.map((f) => f.family.familyCode)),
    noteKeys,
    unclassified,
    mainColor: families[0]?.family.hexColorCode || OTHER_COLOR,
    mood: buildMood(families)
  }
}

const buildMood = (families) => {
  const top = families.map((f) => f.family.familyCode).filter((code) => MOOD_BY_FAMILY[code])
  if (!top.length) return { headline: '', words: [] }
  const first = MOOD_BY_FAMILY[top[0]]
  const second = top[1] ? MOOD_BY_FAMILY[top[1]] : null
  const headline = second ? `${first.and} ${second.mod} 분위기` : `${first.mod} 분위기`
  const candidates = top.slice(0, 3).flatMap((code) => MOOD_BY_FAMILY[code].words.slice(0, 2))
  const words = [...new Set(candidates)].slice(0, 5)
  return { headline, words }
}

/**
 * 검색한 향수와 같은 향료가 많은 순.
 * 동점이면 계열이 많이 겹치는 순 → Fragella 유사도 순 → 평점 순.
 */
export const rankSimilar = (base, baseProfile, candidates, profileOf, limit = 10) =>
  candidates
    .filter((p) => p.id !== base.id)
    .map((p) => {
      const profile = profileOf(p)
      const sharedNotes = [...profile.noteKeys].filter((k) => baseProfile.noteKeys.has(k))
      const sharedFamilies = [...profile.familyCodes].filter((c) => baseProfile.familyCodes.has(c))
      return { perfume: p, profile, sharedNotes, sharedFamilies }
    })
    .sort(
      (a, b) =>
        b.sharedNotes.length - a.sharedNotes.length ||
        b.sharedFamilies.length - a.sharedFamilies.length ||
        (b.perfume.similarity || 0) - (a.perfume.similarity || 0) ||
        (b.perfume.rating || 0) - (a.perfume.rating || 0)
    )
    .slice(0, limit)

/**
 * 선택한 계열/계절/상황이 많이 겹치는 순. 동점이면 선택한 계열 비율 합이 큰 순 → 평점 순.
 */
export const rankByFilters = (candidates, profileOf, { families, seasons, occasions }) => {
  const total = families.length + seasons.length + occasions.length
  return candidates
    .map((p) => {
      const profile = profileOf(p)
      const matchedFamilies = families.filter((c) => profile.familyCodes.has(c))
      const matchedSeasons = seasons.filter((s) => p.seasons?.includes(s))
      const matchedOccasions = occasions.filter((o) => p.occasions?.includes(o))
      const share = profile.families
        .filter((f) => families.includes(f.family.familyCode))
        .reduce((a, f) => a + f.pct, 0)
      return {
        perfume: p,
        profile,
        matchedFamilies,
        matched: matchedFamilies.length + matchedSeasons.length + matchedOccasions.length,
        total,
        share
      }
    })
    .filter((r) => total === 0 || r.matched > 0)
    .sort(
      (a, b) =>
        b.matched - a.matched ||
        b.share - a.share ||
        (b.perfume.rating || 0) - (a.perfume.rating || 0)
    )
}
