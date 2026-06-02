<template>
  <div class="page-container">
    <div class="form-card">
      <!-- 顶部装饰 -->
      <div class="form-header">
        <div class="form-icon">✏️</div>
        <h2 class="form-title">记一笔账</h2>
        <p class="form-subtitle">{{ currentMemberName }} · 记录你的每一笔</p>
      </div>

      <!-- 收入/支出切换 -->
      <div class="type-switch">
        <div
          :class="['type-tab', { active: form.type === 'EXPENSE' }]"
          @click="form.type = 'EXPENSE'; onTypeChange()"
        >
          <span class="type-tab-icon">📤</span>
          <span>支出</span>
        </div>
        <div
          :class="['type-tab', { active: form.type === 'INCOME' }]"
          @click="form.type = 'INCOME'; onTypeChange()"
        >
          <span class="type-tab-icon">📥</span>
          <span>收入</span>
        </div>
      </div>

      <!-- 金额输入 -->
      <div class="amount-section">
        <div class="amount-label">金额</div>
        <div class="amount-input-wrap">
          <span class="currency-sign">¥</span>
          <input
            ref="amountInput"
            v-model="form.amount"
            type="text"
            inputmode="decimal"
            class="amount-input"
            placeholder="0.00"
            @input="onAmountInput"
          />
        </div>
      </div>

      <!-- 类别选择 -->
      <div class="category-section">
        <div class="section-label">类别</div>
        <div class="category-grid">
          <div
            v-for="cat in filteredCategories"
            :key="cat.id"
            :class="['category-chip', { selected: form.categoryId === cat.id }]"
            @click="form.categoryId = cat.id"
          >
            <span class="chip-icon">{{ cat.icon }}</span>
            <span class="chip-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>

      <!-- 日期选择 -->
      <div class="field-row">
        <span class="field-label">📅 日期</span>
        <el-date-picker
          v-model="form.recordDate"
          type="date"
          placeholder="选择日期"
          size="large"
          style="width: 100%"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </div>

      <!-- 备注 -->
      <div class="field-row">
        <span class="field-label">📝 备注</span>
        <el-input
          v-model="form.note"
          placeholder="写点什么..."
          size="large"
          maxlength="200"
          show-word-limit
        />
      </div>

      <!-- 保存按钮 -->
      <el-button
        type="primary"
        size="large"
        :loading="saving"
        @click="handleSave"
        class="save-btn"
      >
        💾 保存记录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { addRecord } from '../api/record'
import { getCategories } from '../api/category'
import { getMembers } from '../api/member'

const saving = ref(false)
const categories = ref([])
const amountInput = ref(null)
const currentMemberName = ref('')

const form = ref({
  type: 'EXPENSE',
  amount: '',
  categoryId: null,
  recordDate: new Date().toISOString().split('T')[0],
  familyMember: '',
  note: ''
})

const filteredCategories = computed(() => {
  return categories.value.filter(c => c.type === form.value.type)
})

const onTypeChange = () => {
  form.value.categoryId = null
}

const onAmountInput = (value) => {
  let val = value.replace(/[^\d.]/g, '')
  const parts = val.split('.')
  if (parts.length > 2) val = parts[0] + '.' + parts.slice(1).join('')
  if (parts.length === 2 && parts[1].length > 2) {
    val = parts[0] + '.' + parts[1].slice(0, 2)
  }
  form.value.amount = val
}

const handleSave = async () => {
  if (!form.value.amount || parseFloat(form.value.amount) <= 0) {
    ElMessage.warning('请输入有效金额')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请选择类别')
    return
  }

  saving.value = true
  try {
    await addRecord({
      type: form.value.type,
      amount: parseFloat(form.value.amount),
      categoryId: form.value.categoryId,
      recordDate: form.value.recordDate,
      familyMember: form.value.familyMember,
      note: form.value.note || ''
    })
    ElMessage.success('保存成功！')
    resetForm()
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  form.value.amount = ''
  form.value.categoryId = null
  form.value.recordDate = new Date().toISOString().split('T')[0]
  form.value.note = ''
  nextTick(() => {
    if (amountInput.value) amountInput.value.focus()
  })
}

onMounted(async () => {
  try {
    const currentUserId = Number(localStorage.getItem('userId')) || null
    const [catRes, memRes] = await Promise.all([
      getCategories(),
      getMembers()
    ])
    categories.value = catRes.data

    // 从家庭成员列表中查找当前用户对应的身份标签
    const members = memRes.data || []
    const myMember = members.find(m => m.userId === currentUserId)
    if (myMember) {
      currentMemberName.value = myMember.name
      form.value.familyMember = myMember.name
    } else {
      // fallback: 使用用户名
      currentMemberName.value = localStorage.getItem('username') || '我'
      form.value.familyMember = currentMemberName.value
    }
  } catch (e) {
    // handled by interceptor
  }
})
</script>

<style scoped>
.form-card {
  max-width: 480px;
  margin: 0 auto;
  background: var(--white);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  padding: 36px 32px 32px;
}

/* 顶部 */
.form-header {
  text-align: center;
  margin-bottom: 28px;
}
.form-icon {
  font-size: 40px;
  margin-bottom: 8px;
}
.form-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}
.form-subtitle {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 类型切换 */
.type-switch {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 4px;
}
.type-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #999;
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
}
.type-tab.active {
  background: var(--white);
  color: var(--text-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.type-tab:first-child.active {
  color: var(--expense, #E74C3C);
}
.type-tab:last-child.active {
  color: var(--income, #27AE60);
}
.type-tab-icon {
  font-size: 18px;
}

/* 金额 */
.amount-section {
  margin-bottom: 24px;
}
.amount-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.amount-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 2px solid #eee;
  border-radius: 12px;
  transition: border-color 0.25s;
}
.amount-input-wrap:focus-within {
  border-color: var(--accent);
}
.currency-sign {
  font-size: 28px;
  font-weight: 300;
  color: #ccc;
}
.amount-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  background: transparent;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  min-width: 0;
  padding: 4px 0;
}
.amount-input::placeholder {
  color: #ddd;
  font-weight: 400;
}

/* 类别网格 */
.category-section {
  margin-bottom: 24px;
}
.section-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.category-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  border: 2px solid #eee;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.category-chip:hover {
  border-color: var(--accent-light, #FFD699);
  background: #FFFDF7;
}
.category-chip.selected {
  border-color: var(--accent);
  background: var(--accent-light, #FFF0D4);
  box-shadow: 0 0 0 2px rgba(255, 165, 0, 0.15);
}
.chip-icon {
  font-size: 22px;
}
.chip-name {
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
}

/* 字段行 */
.field-row {
  margin-bottom: 18px;
}
.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.save-btn {
  width: 100%;
  margin-top: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .form-card {
    border-radius: 0;
    box-shadow: none;
    padding: 24px 20px;
  }
  .amount-input {
    font-size: 28px;
  }
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
