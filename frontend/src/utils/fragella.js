/**
 * Fragella 향수 응답(Fragrance Object) → 화면용 형태로 변환.
 * 응답 형식: https://api.fragella.com/docs.html (GET /fragrances, /fragrances/similar)
 * 백엔드(/api/perfume/fragrances...)가 Fragella 필드명 그대로 돌려준다.
 */

/** Main Accords Percentage 값(단어) → 4단계 강도 */
export const ACCORD_LEVELS = {
  Dominant: { step: 4, label: '매우 강함' },
  Prominent: { step: 3, label: '강함' },
  Moderate: { step: 2, label: '보통' },
  Subtle: { step: 1, label: '은은함' }
}

/** Season Ranking / Occasion Ranking 의 name 값 */
export const SEASON_OPTIONS = [
  { value: 'spring', label: '봄' },
  { value: 'summer', label: '여름' },
  { value: 'fall', label: '가을' },
  { value: 'winter', label: '겨울' }
]

export const OCCASION_OPTIONS = [
  { value: 'casual', label: '데일리' },
  { value: 'professional', label: '오피스' },
  { value: 'night out', label: '저녁 외출' }
]

/**
 * 계절/상황은 모든 항목이 점수 순위로 온다 → 상위 몇 개를 "잘 어울린다"로 본다.
 * (계절 4개 중 상위 2개, 상황 3개 중 1위)
 */
const TOP_SEASONS = 2
const TOP_OCCASIONS = 1

const noteNames = (list) => (list || []).map((n) => n.name)

const topNames = (ranking, count) =>
  [...(ranking || [])]
    .sort((a, b) => b.score - a.score)
    .slice(0, count)
    .map((r) => r.name)

export const fromFragella = (f) => ({
  id: f._id,
  name: f.Name,
  brand: f.Brand,
  year: f.Year,
  concentration: f.OilType,
  rating: f.rating ? Number(f.rating) : null,
  image: f['Image URL Transparent'] || f['Image URL'] || '',
  notes: {
    top: noteNames(f.Notes?.Top),
    middle: noteNames(f.Notes?.Middle),
    base: noteNames(f.Notes?.Base)
  },
  accords: (f['Main Accords'] || []).map((name) => ({
    name,
    level: f['Main Accords Percentage']?.[name] || ''
  })),
  seasons: topNames(f['Season Ranking'], TOP_SEASONS),
  occasions: topNames(f['Occasion Ranking'], TOP_OCCASIONS),
  // 비슷한 향수 응답에만 있음
  similarity: f.SimilarityScore ?? null
})
