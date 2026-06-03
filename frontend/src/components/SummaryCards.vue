<template>
  <el-row :gutter="16" class="summary-row">
    <el-col :xs="8" :sm="8">
      <div class="stats-card">
        <div class="card-icon">📥</div>
        <div class="card-label">总收入</div>
        <div class="card-value text-income">¥{{ fmt(summary.totalIncome) }}</div>
      </div>
    </el-col>
    <el-col :xs="8" :sm="8">
      <div class="stats-card">
        <div class="card-icon">📤</div>
        <div class="card-label">总支出</div>
        <div class="card-value text-expense">¥{{ fmt(summary.totalExpense) }}</div>
      </div>
    </el-col>
    <el-col :xs="8" :sm="8">
      <div class="stats-card">
        <div class="card-icon">💰</div>
        <div class="card-label">结余</div>
        <div class="card-value" :class="balanceClass">¥{{ fmt(summary.balance) }}</div>
      </div>
    </el-col>
  </el-row>
  <div class="period-label" v-if="summary.periodLabel">{{ summary.periodLabel }} · 共 {{ summary.recordCount }} 笔记录</div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  summary: { type: Object, default: () => ({}) }
})

const balanceClass = computed(() => {
  const bal = parseFloat(props.summary.balance || 0)
  return bal >= 0 ? 'text-income' : 'text-expense'
})

const fmt = (val) => {
  return Number(val || 0).toFixed(2)
}
</script>

<style scoped>
.summary-row {
  margin-bottom: 20px;
}

.period-label {
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
  margin-bottom: 12px;
  padding: 6px 16px;
  background: var(--white);
  border-radius: 20px;
  display: inline-block;
  box-shadow: var(--shadow-sm);
}

/* 卡片颜色 */
.stats-card:first-child {
  background: linear-gradient(135deg, #f0faf4 0%, #e8f8f0 100%);
  border-color: #c8e6d0;
}
.stats-card:nth-child(2) {
  background: linear-gradient(135deg, #fef5f5 0%, #fdedec 100%);
  border-color: #f5c6cb;
}
.stats-card:nth-child(3) {
  background: linear-gradient(135deg, #FFFDF5 0%, #FFF8DC 100%);
  border-color: #f5e6c8;
}
</style>
