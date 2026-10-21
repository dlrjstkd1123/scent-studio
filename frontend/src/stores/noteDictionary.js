import { defineStore } from 'pinia'
import { getNoteDictionary } from '@/api/perfume'

const LOAD_ERROR = '향 계열 정보를 불러오지 못했어요. 노트는 색상 없이 표시됩니다.'

/**
 * 노트 사전 (DB 의 향 계열 색상 + 향료→계열 + 향료 별칭 + 어코드→계열). 앱 전체에서 한 번만 불러온다.
 * ingredientByName 은 정식 영문명과 별칭 모두 소문자 키로 같은 향료를 가리킨다.
 */
export const useNoteDictionaryStore = defineStore('noteDictionary', {
  state: () => ({
    families: [],
    familyByCode: new Map(),
    ingredientByName: new Map(),
    accordByName: new Map(),
    loaded: false,
    error: ''
  }),
  actions: {
    load() {
      if (this.loaded) return
      getNoteDictionary()
        .then((res) => {
          const { code, response } = res.data
          switch (parseInt(code)) {
          case 100: {
            const { families, ingredients, aliases, accords } = response
            this.families = families
            this.familyByCode = new Map(
              families.map((family) => [family.familyCode, family])
            )
            const byName = new Map(
              ingredients.map((ingredient) => [
                ingredient.ingredientNameEn.toLowerCase(),
                ingredient
              ])
            )
            aliases.forEach(({ aliasName, ingredientNameEn }) => {
              const ingredient = byName.get(ingredientNameEn.toLowerCase())
              if (ingredient) byName.set(aliasName.toLowerCase(), ingredient)
            })
            this.ingredientByName = byName
            this.accordByName = new Map(
              accords.map((accord) => [accord.accordName, accord.familyCode])
            )
            this.loaded = true
            this.error = ''
            break
          }
          default:
            this.error = LOAD_ERROR
            break
          }
        })
        .catch((err) => {
          this.error = LOAD_ERROR
          console.log(err)
        })
    }
  }
})
