<script setup>
import { onMounted, ref } from 'vue'
import { getHealth } from '@/api/health'

const health = ref(null)
const error = ref('')
const loading = ref(false)

const check = () => {
  loading.value = true
  error.value = ''
  getHealth()
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        health.value = response
        break
      case 102:
        // DB 연결 실패여도 서버 상태는 보여준다
        health.value = response
        break
      default:
        health.value = null
        error.value = message
        break
      }
    })
    .catch((err) => {
      health.value = null
      error.value = '백엔드에 연결할 수 없습니다. Spring Boot(8090)가 실행 중인지 확인하세요.'
      console.log(err)
    })
    .finally(() => {
      loading.value = false
    })
}

onMounted(check)
</script>

<template>
  <section class="card">
    <h2>연결 상태</h2>
    <p v-if="loading">확인 중...</p>
    <p v-else-if="error" class="down">{{ error }}</p>
    <ul v-else-if="health">
      <li>
        Backend:
        <b :class="health.server === 'UP' ? 'up' : 'down'">{{ health.server }}</b>
      </li>
      <li>
        Database:
        <b :class="health.db === 'UP' ? 'up' : 'down'">{{ health.db }}</b>
      </li>
      <li v-if="health.dbVersion" class="muted">{{ health.dbVersion }}</li>
      <li v-if="health.error" class="down">{{ health.error }}</li>
    </ul>
    <button :disabled="loading" @click="check">다시 확인</button>
  </section>
</template>

<style scoped>
.card {
  max-width: 960px;
  margin: 40px auto;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px 24px;
}

ul {
  padding-left: 18px;
  line-height: 1.8;
}

.up {
  color: #1a7f37;
}

.down {
  color: #cf222e;
}

.muted {
  color: #888;
  font-size: 13px;
}

button {
  padding: 6px 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
}
</style>
