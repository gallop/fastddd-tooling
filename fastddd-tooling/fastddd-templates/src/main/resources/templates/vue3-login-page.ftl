<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>${projectName}</h2>
          <p>${msg.vue.login.welcome}</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="${msg.vue.login.usernamePlaceholder}"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="${msg.vue.login.passwordPlaceholder}"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">${msg.vue.login.rememberMe}</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            {{ loading ? '${msg.vue.login.loggingIn}' : '${msg.vue.login.loginButton}' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>${msg.vue.login.footerHint}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

// ${msg.vue.login.formComment}
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// ${msg.vue.login.rulesComment}
const loginRules: FormRules = {
  username: [
    { required: true, message: '${msg.vue.login.usernamePlaceholder}', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '${msg.vue.login.passwordPlaceholder}', trigger: 'blur' },
    { min: 6, message: '${msg.vue.login.passwordMinLength}', trigger: 'blur' }
  ]
}

// ${msg.vue.login.handleComment}
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm.username, loginForm.password)
      ElMessage.success('${msg.vue.login.success}')
      router.push('/')
    } catch (error: any) {
      ElMessage.error(error.message || '${msg.vue.login.failure}')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.login-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.login-footer p {
  margin: 0;
  color: #909399;
  font-size: 12px;
}
</style>
